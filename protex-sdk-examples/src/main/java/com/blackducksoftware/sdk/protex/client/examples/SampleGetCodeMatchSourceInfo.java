package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ComponentInfo;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoverySourceInfo;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

/**
 * This sample gathers all code match source match info for a given path.
 * 
 * CAUTION Limit your codeTree appropriately! The amount of discoveries can become very large very quickly for large
 * code trees, overwhelming memory on the server or client.
 * 
 * It demonstrates:
 * - How to generate a CodeTree that has only a single folder/file as node
 * - How to retrieve all precision code match discovery sources for a given folder/file
 * - How to translate the ID's of components and versions into names that humans are more familiar with
 */
public class SampleGetCodeMatchSourceInfo extends BDProtexSample {

    private static DiscoveryApi discoveryApi = null;

    private static CodeTreeApi codeTreeApi = null;

    private static ProjectApi projectApi = null;

    private static Map<ComponentKey, ComponentInfo> componentInfos = new HashMap<ComponentKey, ComponentInfo>();

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetCodeMatchSourceInfo.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");
        parameters.add("<path>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The project ID of the project to list, i.e. c_testproject"));
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
                projectApi = myProtexServer.getProjectApi();
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

            List<DiscoverySourceInfo> matchedSource = null;

            try {
                matchedSource = discoveryApi.getCodeMatchSourceInfo(projectId, codeTree, Arrays.asList(CodeMatchType.PRECISION));
            } catch (SdkFault e) {
                System.err.println("getCodeMatchDiscoveries() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // Print the results
            if (matchedSource == null || matchedSource.isEmpty()) {
                System.out.println("No code matches discoveries for '" + codeTreePath + "'");
            } else {
                try {
                    for (DiscoverySourceInfo sourceInfo : matchedSource) {
                        ComponentInfo info = getComponentInfo(projectId, sourceInfo.getDiscoveredComponentKey());
                        System.out.println("Matched source of " + info.getComponentName() + " served at " + sourceInfo.getComponentSourceUrl());
                    }
                } catch (SdkFault e) {
                    System.err.println("getComponentById() or getComponentVersionById() failed: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            System.err.println("SampleGetCodeMatchSourceInfo failed");
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

    private static ComponentInfo getComponentInfo(String projectId, ComponentKey componentKey) throws SdkFault {
        if (componentKey == null) {
            return null;
        }

        ComponentInfo componentInfo = componentInfos.get(componentKey);

        if (componentInfo == null) {
            componentInfo = projectApi.getComponentByKey(projectId, componentKey);
            componentInfos.put(componentInfo.getComponentKey(), componentInfo);
        }

        return componentInfo;
    }
}
