package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DependencyDiscovery;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryType;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

/**
 * This sample gathers all dependency discoveries for a given path.
 * 
 * CAUTION Limit your codeTree appropriately! The amount of discoveries can
 * become very large very quickly for large code trees, overwhelming memory on the server or client.
 * 
 * It demonstrates:
 * - How to generate a CodeTree that has only a single folder/file as node
 * - How to retrieve all dependency discoveries for a given folder/file
 */

public class SampleGetDependencyDiscoveries extends BDProtexSample {

    private static DiscoveryApi discoveryApi = null;

    private static CodeTreeApi codeTreeApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetDependencyDiscoveries.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");
        parameters.add("<path>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The project ID of the project to list file info for, i.e. c_testproject"));
        paramDescriptions.add(formatUsageDetail("path", "The path in the code tree of the project, i.e. \"/\" or \"/My Folder/Abc.java\""));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 5) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectId = args[3];
        String codeTreePath = args[4];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                discoveryApi = myProtexServer.getDiscoveryApi();
                codeTreeApi = myProtexServer.getCodeTreeApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            List<CodeTreeNode> codeTree = null;

            try {
                CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
                codeTreeParameters.getIncludedNodeTypes().addAll(CodeTreeUtilities.ALL_CODE_TREE_NODE_TYPES);
                codeTreeParameters.setDepth(CodeTreeUtilities.SINGLE_NODE);
                codeTreeParameters.setIncludeParentNode(true);

                codeTree = codeTreeApi.getCodeTreeNodes(projectId, codeTreePath, codeTreeParameters);
            } catch (SdkFault e) {
                System.err.println("getCodeTree() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            List<DependencyDiscovery> discoveries = null;

            try {
                discoveries = discoveryApi.getDependencyDiscoveries(projectId, codeTree);
            } catch (SdkFault e) {
                System.err.println("getDependencyDiscoveries() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (discoveries == null || discoveries.isEmpty()) {
                System.out.println("No dependency discoveries for '" + codeTreePath + "'");
            } else {
                // Grouping the different type of dependency
                List<DependencyDiscovery> listDependencyJavaImport = new ArrayList<DependencyDiscovery>();
                List<DependencyDiscovery> listDependencyJavaPackage = new ArrayList<DependencyDiscovery>();
                List<DependencyDiscovery> listDependencyOtherLanguages = new ArrayList<DependencyDiscovery>();
                for (DependencyDiscovery discovery : discoveries) {
                    if (discovery.getDiscoveryType() == DiscoveryType.DEPENDENCY_JAVA_IMPORT) {
                        listDependencyJavaImport.add(discovery);
                    } else if (discovery.getDiscoveryType() == DiscoveryType.DEPENDENCY_JAVA_PACKAGE) {
                        listDependencyJavaPackage.add(discovery);
                    } else if (discovery.getDiscoveryType() == DiscoveryType.DEPENDENCY_NON_JAVA_SOURCE) {
                        listDependencyOtherLanguages.add(discovery);
                    }
                }
                // Displaying back dependencies for Jave Import
                System.out.println("Java Import dependencies");
                for (DependencyDiscovery dependency : listDependencyJavaImport) {
                    System.out.println(dependency.getFilePath() + " : " + dependency.getFirstLine());
                }
                // Displaying back dependencies for Jave Import
                System.out.println("Java Package dependencies");
                for (DependencyDiscovery dependency : listDependencyJavaPackage) {
                    System.out.println(dependency.getFilePath() + " : " + dependency.getFirstLine());
                }
                // Displaying back dependencies for Jave Import
                System.out.println("Non Java Source dependencies");
                for (DependencyDiscovery dependency : listDependencyOtherLanguages) {
                    System.out.println(dependency.getFilePath() + " : " + dependency.getFirstLine());
                }
            }
        } catch (Exception e) {
            System.err.println("SampleGetDependencyDiscoveries failed");
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
