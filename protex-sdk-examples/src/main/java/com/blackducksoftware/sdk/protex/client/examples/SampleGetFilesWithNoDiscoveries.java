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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;
import com.blackducksoftware.sdk.protex.util.CodeTreeNodeComparator;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

/**
 * This sample displays a project tree for files with no discoveries, with a total count for each folder.
 *
 * It demonstrates:
 * - How to generate a CodeTree starting at the ROOT = "/"
 * - How to generate a CodeTree with infinite depth
 * - How to generate a CodeTree to calculate a node count
 * - How to use a CodeTree and node counts to output a meaningful tree
 */
public class SampleGetFilesWithNoDiscoveries extends BDProtexSample {

    private static CodeTreeApi codetreeApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetFilesWithNoDiscoveries.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID",
                "The ID of the project to get the list of files with no discoveries, i.e. \"c_newsampleproject\""));

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

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                codetreeApi = myProtexServer.getCodeTreeApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            String root = "/";

            List<CodeTreeNode> codeTree = null;

            try {
                CodeTreeNodeRequest nodeRequest = new CodeTreeNodeRequest();
                nodeRequest.setDepth(CodeTreeUtilities.INFINITE_DEPTH);
                nodeRequest.setIncludeParentNode(true);
                nodeRequest.getIncludedNodeTypes().addAll(Arrays.asList(CodeTreeNodeType.values()));
                nodeRequest.getCounts().add(NodeCountType.NO_DISCOVERIES);

                codeTree = codetreeApi.getCodeTreeNodes(projectId, root, nodeRequest);
            } catch (SdkFault e) {
                System.err.println("getCodeTree failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (codeTree != null) {
                Collections.sort(codeTree, CodeTreeNodeComparator.getInstance());

                String emptyPath = null;

                for (CodeTreeNode node : codeTree) {
                    final long count = node.getNodeCounts().get(0).getCount();
                    final String path = node.getName();

                    if (node.getNodeType() == CodeTreeNodeType.FILE) {
                        if (count != 0) {
                            System.out.printf("        %s\r\n", path.replaceAll(".*/", ""));
                        }
                    } else {
                        if ((emptyPath == null) || !path.startsWith(emptyPath)) {
                            System.out.printf("[%3d] %s/\r\n", count, path);
                            if (count == 0) {
                                if (path.isEmpty()) {
                                    break;
                                }
                                emptyPath = path + "/";
                            }
                        }
                    }
                }

            } else {
                System.out.println("No code tree nodes found in project " + projectId);
            }

        } catch (Exception e) {
            System.err.println("SampleGetFilesWithNoDiscoveries failed");
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
