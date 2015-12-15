package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.StringSearchPatternOriginType;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.Highlight;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.StringSearchDiscovery;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.StringSearchDiscoveryWithMatches;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.StringSearchMatch;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

/**
 * This sample gathers all search discoveries for a given path.
 * 
 * CAUTION Limit your codeTree appropriately! The amount of discoveries can become very large very quickly for large
 * code trees, overwhelming memory on the server or client.
 * 
 * It demonstrates:
 * - How to generate a CodeTree that has only a single folder/file as node
 * - How to retrieve all search discoveries for a given folder/file
 */

public class SampleGetStringSearchDiscoveriesWithMatches extends BDProtexSample {

    private static DiscoveryApi discoveryApi = null;

    private static CodeTreeApi codeTreeApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetStringSearchDiscoveriesWithMatches.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");
        parameters.add("<path>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID for the project that you want to know the status off, i.e. c_test-project"));
        paramDescriptions.add(formatUsageDetail("path", "The path in the code tree of the project, i.e. \"/\" or \"/My Folder/Abc.java\""));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
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

            List<StringSearchDiscovery> discoveries = null;

            try {
                discoveries = discoveryApi.getStringSearchDiscoveries(projectId, codeTree, Arrays.asList(StringSearchPatternOriginType.values()));
            } catch (SdkFault e) {
                System.err.println("getStringSearchDiscoveries() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (discoveries == null || discoveries.isEmpty()) {
                System.out.println("No string search discoveries for '" + codeTreePath + "'");
            } else {
                StringSearchDiscoveryWithMatches discoveredMatches = null;

                for (StringSearchDiscovery ssd : discoveries) {
                    try {
                        discoveredMatches = discoveryApi.getStringSearchMatches(projectId, ssd, 15);
                    } catch (SdkFault e) {
                        System.err.println("getStringSearchMatches() failed: " + e.getMessage());
                        System.exit(-1);
                    }

                    if (discoveredMatches == null) {
                        System.err.println("getStringSearchMatches() returned Unexpected value '" + discoveredMatches + "'");
                        System.exit(-1);
                    }
                    if (discoveredMatches.getMatches().size() == 0) {
                        System.out.println("No matches for discovery for '" + codeTreePath + "' + '" + discoveredMatches.getStringSearchId() + "'");
                    } else {
                        System.out.println("===============   String Search ID: " + discoveredMatches.getStringSearchId());
                        for (StringSearchMatch match : discoveredMatches.getMatches()) {
                            System.out.println("..............................");
                            String m = new String(match.getMatch(), "UTF-8");
                            String c = new String(match.getContext(), "UTF-8");
                            int firstPos = Integer.MAX_VALUE;
                            int lastPos = 0;
                            for (Highlight h : match.getContextHighlights()) {
                                firstPos = Math.min(h.getFirstIndex(), firstPos);
                                lastPos = Math.max(h.getLastIndex(), lastPos);
                            }
                            System.out.println("      '" + c.substring(0, firstPos) + "<<<" + m + ">>>" + c.substring(lastPos) + "'");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("SampleGetStringSearchDiscoveriesDiscoveriesWithMatches failed");
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
