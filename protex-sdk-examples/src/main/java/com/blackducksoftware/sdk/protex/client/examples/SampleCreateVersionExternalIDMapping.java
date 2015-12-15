/*
 * Copyright (C) 2009, 2010 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.ErrorCode;
import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.policy.externalid.ComponentVersionObjectKey;
import com.blackducksoftware.sdk.protex.policy.externalid.ExternalIdApi;
import com.blackducksoftware.sdk.protex.policy.externalid.ExternalIdMapping;
import com.blackducksoftware.sdk.protex.policy.externalid.ExternalNamespace;
import com.blackducksoftware.sdk.protex.policy.externalid.ExternalNamespaceRequest;
import com.blackducksoftware.sdk.protex.policy.externalid.ProtexObjectKey;
import com.blackducksoftware.sdk.protex.policy.externalid.ProtexObjectType;

/**
 * This sample creates a mapping between an external ID and a protex object in a
 * particular namespace
 *
 * It demonstrates:
 * - How validate the namespace and how to create it if it does not exist
 * - How to create an external ID mapping
 */
public class SampleCreateVersionExternalIDMapping extends BDProtexSample {

    private static ExternalIdApi externalIdApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCreateVersionExternalIDMapping.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<namespace>");
        parameters.add("<protex object ID>");
        parameters.add("<protex object version ID>");
        parameters.add("<external ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("namespace", "Some string in reverse domain format, i.e. \"com.example.great_app\""));
        paramDescriptions.add(formatUsageDetail("protex object ID", "The ID of the protex component to map, i.e. \"c_greatapp11_5123\""));
        paramDescriptions.add(formatUsageDetail("protex object Version ID", "The version ID of the protex component to map, i.e. \"c_greatapp11_5123\""));
        paramDescriptions.add(formatUsageDetail("external ID", "The ID for this object used by the external system, i.e. \"com.example:great-app:1.1\""));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 7) {
            System.err.println("\nNot enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String namespace = args[3];
        String protexObjectId = args[4];
        String protexObjectVersionId = args[5];
        String externalId = args[6];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                externalIdApi = myProtexServer.getExternalIdApi();
            } catch (RuntimeException e) {
                System.err.println("\nConnection to server '" + serverUri
                        + "' failed: " + e.getMessage());
                throw e;
            }

            // see if the namespace already exists, if not create it
            try {
                ExternalNamespace nameSpaceKey = externalIdApi.getExternalNamespace(namespace);
                System.out.println("\nNamespace: '" + nameSpaceKey.getName() + "' found.");
            } catch (SdkFault e) {
                if (e.getFaultInfo().getErrorCode() == ErrorCode.EXTERNAL_NAMESPACE_NOT_FOUND) {
                    // namespace does not exist ==> create it
                    ExternalNamespaceRequest request = new ExternalNamespaceRequest();
                    request.setExternalNamespaceKey(namespace);
                    request.setName(namespace);

                    try {
                        externalIdApi.createExternalNamespace(request);
                        System.out.println("\nNamespace: " + namespace + " created.");
                    } catch (SdkFault e1) {
                        System.err.println("\ncreateExternalNamespace() failed: " + e1.getMessage());
                        throw new RuntimeException(e1);
                    }
                } else {
                    System.err.println("\ngetExternalNamespace() failed: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            // create the mapping
            try {
                ExternalIdMapping mapping = new ExternalIdMapping();
                mapping.setExternalObjectId(externalId);
                mapping.setProtexObjectKey(createProtexObjectKey(protexObjectId, protexObjectVersionId));

                externalIdApi.createExternalIdMapping(namespace, mapping);
                System.out.println("\nExternal ID '" + externalId
                        + "' mapped to Protex "
                        + mapping.getProtexObjectKey().getObjectType() + " '"
                        + protexObjectId + "'");

                ProtexObjectKey objectKey = externalIdApi.getObjectAndVersionIdByExternalId(namespace, mapping.getExternalObjectId(),
                        ProtexObjectType.COMPONENT);

                if (ProtexObjectType.COMPONENT.equals(objectKey.getObjectType())) {
                    ComponentVersionObjectKey componentKey = (ComponentVersionObjectKey) objectKey;

                    System.out.println("Retrieved object key (" + componentKey.getObjectId() + ", " + componentKey.getObjectVersionId() + ")");
                } else {
                    throw new RuntimeException("Wrong type returned");
                }
            } catch (SdkFault e) {
                System.err.println("\ncreateExternalIdMapping() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.err.println("SampleCreateExternalIDMapping failed");
            e.printStackTrace(System.err);
            System.exit(-1);
        } finally {
            // This is optional - it causes the proxy to overwrite the stored password with null characters, increasing
            // security
            if (myProtexServer != null) {
                myProtexServer.close();
            }
        }

    }

    private static ProtexObjectKey createProtexObjectKey(String id, String versionId) {
        ComponentVersionObjectKey protexObjectKey = new ComponentVersionObjectKey();
        protexObjectKey.setObjectType(ProtexObjectType.COMPONENT);
        protexObjectKey.setObjectId(id);
        protexObjectKey.setObjectVersionId(versionId);

        return protexObjectKey;
    }
}
