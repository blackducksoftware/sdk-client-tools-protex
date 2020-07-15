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

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ComponentKbRankInfo;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.component.ComponentApi;

/**
 * This sample demonstrates how to get the kb rank
 *
 */
public class SampleGetComponentKbRankByKey extends BDProtexSample {

    private static ComponentApi componentApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetComponentKbRankByKey.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<component ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("component ID",
                "The id of the component to get kb rank 'apache-avalonmerlin'"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 4) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String componentId = args[3];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                componentApi = myProtexServer.getComponentApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            ComponentKbRankInfo componentrank = null;

            ComponentKey componentKey = new ComponentKey();
            componentKey.setComponentId(componentId);

            try {
                componentrank = componentApi.getComponentWithKbRankByKey(componentKey);
            } catch (SdkFault e) {
                System.err.println("getComponentVersions() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (componentrank == null) {
                System.err.println("No Component returned");
            } else {
                System.out.println("Component Rank:" + componentrank.getKbRank());
            }
        } catch (Exception e) {
            System.err.println("SampleGetComponentKbRankByKey failed");
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
}
