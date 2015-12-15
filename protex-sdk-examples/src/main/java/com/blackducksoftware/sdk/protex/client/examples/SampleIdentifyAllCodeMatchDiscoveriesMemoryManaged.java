package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.BomRefreshMode;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;
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
 * - How to walk the code tree folder by folder in order to keep the amount of objects manageable (to avoid out of
 * memory situations)
 * - How to refresh the BOM, before gathering the next round of discoveries
 */
public class SampleIdentifyAllCodeMatchDiscoveriesMemoryManaged extends BDProtexSample {

    private static final String ROOT = "/";

    private static DiscoveryApi discoveryApi = null;

    private static CodeTreeApi codeTreeApi = null;

    private static IdentificationApi identificationApi = null;

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleIdentifyAllCodeMatchDiscoveriesMemoryManaged.class.getSimpleName();

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

            List<CodeMatchType> precisionOnly = new ArrayList<CodeMatchType>(1);
            precisionOnly.add(CodeMatchType.PRECISION);

            // Gather all files
            Stack<String> folders = new Stack<String>();
            folders.push(ROOT);
            List<CodeTreeNode> thisLevel = null;

            do {
                try {
                    CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
                    codeTreeParameters.getIncludedNodeTypes().addAll(CodeTreeUtilities.ALL_CODE_TREE_NODE_TYPES);
                    codeTreeParameters.setDepth(1);
                    codeTreeParameters.setIncludeParentNode(false);
                    // Get pending ID code match count to filter nodes discoveries are retreived for to nodes with
                    // pending discoveries
                    codeTreeParameters.getCounts().add(NodeCountType.PENDING_ID_CODE_MATCH);

                    thisLevel = codeTreeApi.getCodeTreeNodes(projectId, folders.pop(), codeTreeParameters);
                } catch (SdkFault e) {
                    System.err.println("getCodeTree failed() " + e.getMessage());
                    throw new RuntimeException(e);
                }

                List<CodeTreeNode> files = new ArrayList<CodeTreeNode>();

                if (thisLevel != null) {
                    for (CodeTreeNode node : thisLevel) {
                        String path = node.getName();
                        Map<NodeCountType, Long> counts = CodeTreeUtilities.getNodeCountMap(node);
                        // Filter nodes which discoveries are request for to nodes which are files with pending matches
                        if (CodeTreeNodeType.FILE.equals(node.getNodeType())) {
                            if (counts.get(NodeCountType.PENDING_ID_CODE_MATCH) != null && counts.get(NodeCountType.PENDING_ID_CODE_MATCH) > 0) {
                                System.out.println("Adding File: " + node.getName() + "(" + counts.get(NodeCountType.PENDING_ID_CODE_MATCH)
                                        + " pending code matches)");
                                files.add(node);
                            } else {
                                System.out.println("Skipping File: " + node.getName() + " (No pending code matches)");
                            }
                        } else {
                            System.out.println("Adding Folder: " + node.getName());
                            folders.push(path);
                        }
                    }
                }

                if (files.size() > 0) {
                    System.out.println("identify discoveries for " + files.size() + " files in folder ");
                    identifyAllCodeMatchDiscoveries(projectId, files, precisionOnly);
                }
            } while (!folders.isEmpty());

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
            System.err.println("SampleIdentifyAllCodeMatchDiscoveriesMemoryManaged failed");
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

    private static void identifyAllCodeMatchDiscoveries(String projectId, List<CodeTreeNode> codeTree,
            List<CodeMatchType> codeMatchTypes) throws SdkFault {
        // Lets start with the intent to check discoveries for pending Identifications
        boolean checkForDiscoveriesPendingID = true;
        while (checkForDiscoveriesPendingID) {
            List<CodeMatchDiscovery> discoveries = null;

            try {
                discoveries = discoveryApi.getCodeMatchDiscoveries(projectId, codeTree, codeMatchTypes);
            } catch (SdkFault e) {
                System.err.println("getCodeMatchDiscoveries() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // add identification
            if (discoveries == null || discoveries.isEmpty()) {
                System.out.println("No code matches in the discoveries");
                checkForDiscoveriesPendingID = false;
            } else {
                Set<String> filesIdentified = new LinkedHashSet<String>();
                for (CodeMatchDiscovery discovery : discoveries) {
                    if ((IdentificationStatus.PENDING_IDENTIFICATION.equals(discovery.getIdentificationStatus()))
                            && !filesIdentified.contains(discovery.getFilePath())) {

                        CodeMatchIdentificationRequest identificationRequest = acceptDiscoveryAsIdentification(discovery);

                        try {
                            // Identify at the file lever where the discovery originates from. Skip BOM refresh for now,
                            // as all files can be refreshed after IDs are made
                            identificationApi.addCodeMatchIdentification(projectId, discovery.getFilePath(),
                                    identificationRequest, BomRefreshMode.SKIP);

                            // mark file as identified in this run. Because each CM identification does potentially
                            // make other discoveries for the same file obsolete, so we need to check first the
                            // discoveries again before doing more identifications on the same file
                            filesIdentified.add(discovery.getFilePath());

                            System.out.println("Identified: " + discovery.getFilePath() + " ==> "
                                    + identificationRequest.getIdentifiedComponentKey().getComponentId() + "#"
                                    + identificationRequest.getIdentifiedComponentKey().getVersionId());
                        } catch (SdkFault e) {
                            System.err.println("addCodeMatchIdentification failed: " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                }

                // Run a BOM Refresh to propagate the identified discoveries
                bomApi.refreshBom(projectId, Boolean.TRUE, Boolean.FALSE);

                // keep on checking if we have identified any new discovery, because this could have changed the
                // state of one or more pending IDs
                checkForDiscoveriesPendingID = filesIdentified.size() > 0;
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
                + (identificationRequest.getIdentifiedLicenseInfo() == null ? "" : identificationRequest
                        .getIdentifiedLicenseInfo().getLicenseId()) + ")");
        return identificationRequest;
    }

}
