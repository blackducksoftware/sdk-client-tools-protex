/*
 * Black Duck Software Suite SDK
 * Copyright (C) 2015  Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.BomRefreshMode;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchDiscovery;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.IdentificationStatus;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeMatchIdentificationDirective;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeMatchIdentificationRequest;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationApi;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

/**
 * This sample demonstrates how to gradually identify all remaining code match discoveries
 * 
 * It demonstrates:
 * - How to generate a CodeTree for the root node only
 * - How to gather files with discoveries pending identification
 * - How to identify a discovery to its discovered code match (always choosing the first one in the list)
 * - How to refresh the BOM, before gathering the next round of discoveries
 * 
 * This samples demonstrates how it can be done straight forward. However for large projects this method will require
 * large amounts of memory in the client as well as in the server and could lead to "Out of Memory" exception on both
 * sides.
 * See SampleIdentifyAllCodeMatchDiscoveriesMemoryManaged for a sample how to avoid this problem.
 */
public class SampleIdentifyAllCodeMatchDiscoveries extends BDProtexSample {

    private static final String ROOT = "/";

    private static DiscoveryApi discoveryApi = null;

    private static CodeTreeApi codeTreeApi = null;

    private static IdentificationApi identificationApi = null;

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleIdentifyAllCodeMatchDiscoveries.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID for the project, i.e. c_test-project"));

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
        String projectId = args[3];

        Long connectionTimeout = 240 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                discoveryApi = myProtexServer.getDiscoveryApi();
                codeTreeApi = myProtexServer.getCodeTreeApi();
                identificationApi = myProtexServer.getIdentificationApi();
                bomApi = myProtexServer.getBomApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<CodeTreeNode> root = null;

            try {
                CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
                codeTreeParameters.getIncludedNodeTypes().addAll(CodeTreeUtilities.ALL_CODE_TREE_NODE_TYPES);
                codeTreeParameters.setDepth(0);
                codeTreeParameters.setIncludeParentNode(true);

                root = codeTreeApi.getCodeTreeNodes(projectId, ROOT, codeTreeParameters);
            } catch (SdkFault e) {
                System.err.println("getCodeTree failed() " + e.getMessage());
                throw new RuntimeException(e);
            }

            Set<String> filesIdentified = new LinkedHashSet<String>();

            do {
                List<CodeMatchDiscovery> discoveries = null;

                try {
                    discoveries = discoveryApi.getCodeMatchDiscoveries(projectId, root, Arrays.asList(CodeMatchType.PRECISION));
                } catch (SdkFault e) {
                    System.err.println("getCodeMatchDiscoveries() failed: " + e.getMessage());
                    throw new RuntimeException(e);
                }

                filesIdentified = new LinkedHashSet<String>();

                if (discoveries == null || discoveries.isEmpty()) {
                    System.out.println("No code matches in the discoveries");
                } else {
                    for (CodeMatchDiscovery discovery : discoveries) {
                        if (IdentificationStatus.PENDING_IDENTIFICATION.equals(discovery.getIdentificationStatus())) {
                            if (!filesIdentified.contains(discovery.getFilePath())) {
                                CodeMatchIdentificationRequest identificationRequest = acceptDiscoveryAsIdentification(discovery);
                                try {
                                    // Identify at the file level where the discovery originates from. Can skip BOM refresh,
                                    // as it will performed afterwards
                                    identificationApi.addCodeMatchIdentification(projectId, discovery.getFilePath(),
                                            identificationRequest, BomRefreshMode.SKIP);
                                    // mark file as identified in this run. Because each CM identification does
                                    // potentially make other discoveries for the same file obsolete, so we need to
                                    // check first the discoveries again before doing more identifications on the same
                                    // file
                                    filesIdentified.add(discovery.getFilePath());
                                    System.out.println("Identified: " + discovery.getFilePath() + " ==> "
                                            + identificationRequest.getIdentifiedComponentKey().getComponentId() + "#"
                                            + identificationRequest.getIdentifiedComponentKey().getVersionId());
                                } catch (SdkFault e) {
                                    System.err.println("addCodeMatchIdentification failed: " + e.getMessage());
                                    System.exit(-1);
                                }
                            } else {
                                // skip all further discoveries for this file, because identifying a codematch could
                                // have changed the identification status to not pending anymore
                            }
                        }
                    }

                    // Run a BOM Refresh to propagate the identified discoveries
                    bomApi.refreshBom(projectId, Boolean.TRUE, Boolean.FALSE);
                }
                // keep on checking if we have identified any new discovery in this loop over the discoveries, because
                // this could have changed the
                // state of one or more pending IDs, eventually all files have no pending IDs anymore and we have not
            } while (filesIdentified.size() > 0);

            System.out.println();
            System.out.println();
            // list the BOM
            List<BomComponent> bom = null;

            try {
                bom = bomApi.getBomComponents(projectId);
            } catch (SdkFault e) {
                System.err.println("getBomComponents() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println();

            // Print the results
            if (bom == null || bom.isEmpty()) {
                System.out.println("No components in the BOM");
            } else {
                for (BomComponent component : bom) {
                    System.out.println(component.getComponentName() + "//"
                            + component.getVersionName() + " has "
                            + component.getFileCounts().getIdentified() + " identified files.");
                }
            }
        } catch (Exception e) {
            System.err.println("SampleIdentifyAllCodeMatchDiscoveries failed");
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

    private static CodeMatchIdentificationRequest acceptDiscoveryAsIdentification(CodeMatchDiscovery discovery) {
        CodeMatchIdentificationRequest identificationRequest = new CodeMatchIdentificationRequest();
        identificationRequest.setDiscoveredComponentKey(discovery.getDiscoveredComponentKey());
        identificationRequest.setIdentifiedComponentKey(discovery.getDiscoveredComponentKey());
        identificationRequest.setCodeMatchIdentificationDirective(CodeMatchIdentificationDirective.SNIPPET_AND_FILE);
        identificationRequest.setIdentifiedUsageLevel(UsageLevel.COMPONENT_MODULE);
        identificationRequest.setIdentifiedLicenseInfo(discovery.getMatchingLicenseInfo());

        System.out.println(discovery.getFilePath() + ": accepting " + discovery.getDiscoveredComponentKey().getComponentId() + "//"
                + discovery.getDiscoveredComponentKey().getVersionId() + " (" + identificationRequest.getIdentifiedUsageLevel() + "::"
                + identificationRequest.getIdentifiedLicenseInfo().getLicenseId() + ")");
        return identificationRequest;
    }

}
