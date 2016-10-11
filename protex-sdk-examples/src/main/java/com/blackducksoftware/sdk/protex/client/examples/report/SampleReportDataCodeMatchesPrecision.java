package com.blackducksoftware.sdk.protex.client.examples.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ComponentInfo;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.common.Snippet;
import com.blackducksoftware.sdk.protex.license.LicenseInfo;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.codetree.CharEncoding;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;
import com.blackducksoftware.sdk.protex.project.codetree.SourceFileInfoNode;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchDiscovery;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchLocation;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryType;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

public class SampleReportDataCodeMatchesPrecision extends BDProtexSample {

    private static ProjectApi projectApi = null;

    private static DiscoveryApi discoveryApi = null;

    private static CodeTreeApi codetreeApi = null;

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleReportDataCodeMatchesPrecision.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID of the project to get data for"));

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
                projectApi = myProtexServer.getProjectApi();
                discoveryApi = myProtexServer.getDiscoveryApi();
                bomApi = myProtexServer.getBomApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // For Summary Section
            Project project = projectApi.getProjectById(projectId);
            List<CodeTreeNode> nodes = null;

            System.out.println("--Summary--");
            System.out.println("Name: " + project.getName());
            System.out.println("License: " + project.getLicenseId());
            System.out.println("Description: " + (project.getDescription() == null ? "" : project.getDescription()));

            // Getting Number Of Files
            try {
                CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
                codeTreeParameters.getIncludedNodeTypes().addAll(CodeTreeUtilities.ALL_CODE_TREE_NODE_TYPES);
                codeTreeParameters.setDepth(CodeTreeUtilities.SINGLE_NODE);
                codeTreeParameters.setIncludeParentNode(true);
                codeTreeParameters.getCounts().add(NodeCountType.FILES);

                nodes = codetreeApi.getCodeTreeNodes(projectId, "/", codeTreeParameters);
            } catch (SdkFault e) {
                System.err.println("getFileCount() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            for (CodeTreeNode node : nodes) {
                Map<NodeCountType, Long> countMap = CodeTreeUtilities.getNodeCountMap(node);
                System.out.println("Number of Files(): " + countMap.get(NodeCountType.FILES));
            }

            // Getting Code Match Discovery List
            List<CodeMatchDiscovery> codeMatchDiscoveries = null;

            try {
                codeMatchDiscoveries = discoveryApi.getCodeMatchDiscoveries(projectId, nodes, Arrays.asList(CodeMatchType.PRECISION));
            } catch (SdkFault e) {
                System.err.println("getCodeMatchDiscoveries() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println();
            ComponentKey componentKey = null;
            ComponentInfo generalComponent = null;

            final String rowFormat = "%1$-80s | %2$10s | %3$11s | %4$11s | "
                    + "%5$-40s | %6$-15s | %7$-20s | %8$-20s | %9$4s%% | %10$-80s | %11$40s | %12$40s";
            System.out.println("");
            System.out.println(String.format(rowFormat, "File / Folder", "Size",
                    "File Line", "Total Lines", "Component", "Version", "License", "Status", "", "Matched File",
                    "Matched File Line", "File Comment", "Component Commment"));

            for (CodeMatchDiscovery codeMatchDiscovery : codeMatchDiscoveries) {
                if ((codeMatchDiscovery.getDiscoveryType() == DiscoveryType.SNIPPET)
                        || (codeMatchDiscovery.getDiscoveryType() == DiscoveryType.FILE)) {

                    componentKey = codeMatchDiscovery.getDiscoveredComponentKey();
                    try {
                        generalComponent = projectApi.getComponentByKey(projectId, componentKey);
                    } catch (SdkFault e) {
                        System.err.println("getComponentById() failed: " + e.getMessage());
                        throw new RuntimeException(e);
                    }

                    String componentComment = null;
                    try {
                        componentComment = bomApi.getComponentComment(projectId, componentKey);
                        componentComment = (componentComment == null ? "" : componentComment);
                    } catch (SdkFault e) {
                        System.err.println("getComponentComment() failed: " + e.getMessage());
                        throw new RuntimeException(e);
                    }

                    String versionName = (generalComponent.getVersionName() != null ? generalComponent.getVersionName() : "");

                    LicenseInfo licenseInfo = codeMatchDiscovery.getMatchingLicenseInfo();
                    String licenseName;
                    if (licenseInfo != null) {
                        licenseName = licenseInfo.getName();
                    } else {
                        licenseName = "<none>";
                    }

                    CodeMatchLocation codeMatchLocation = codeMatchDiscovery.getMatchingFileLocation();
                    Snippet matchedSnippet = codeMatchLocation.getSnippet();
                    List<SourceFileInfoNode> thisFileInfoTree = null;
                    try {
                        thisFileInfoTree = codetreeApi.getFileInfo(projectId, codeMatchDiscovery.getFilePath(), 0, true, CharEncoding.NONE);
                    } catch (SdkFault e) {
                        System.err.println("getComponentVersionById() failed: " + e.getMessage());
                        throw new RuntimeException(e);
                    }

                    SourceFileInfoNode thisFilesInfo = (thisFileInfoTree != null & !thisFileInfoTree.isEmpty() ? thisFileInfoTree.get(0) : null);

                    String fileComment = codetreeApi.getFileOrFolderComment(projectId, codeMatchDiscovery.getFilePath());
                    fileComment = fileComment == null ? "" : fileComment;
                    String status = null;

                    switch (codeMatchDiscovery.getIdentificationStatus()) {
                    case CODE_MATCH_IDENTIFIED_FILE:
                        status = "Identified";
                        break;
                    case IDENTIFIED_SIDE_EFFECT:
                        status = "Rejected";
                        break;
                    default:
                        status = "Unknown";
                    }

                    System.out.println(String.format(rowFormat, codeMatchDiscovery.getFilePath(),
                            thisFilesInfo != null ? thisFilesInfo.getLength() : "",
                            codeMatchDiscovery.getSourceFileLocation().getSnippet().getFirstLine(),
                            matchedSnippet.getFirstLine(), generalComponent.getComponentName(), versionName, licenseName,
                            status,
                            codeMatchDiscovery.getMatchRatioAsPercent(), codeMatchDiscovery.getMatchingFileLocation()
                            .getFilePath(), codeMatchDiscovery.getMatchingFileLocation().getSnippet()
                            .getFirstLine(), fileComment, componentComment));
                }
            }

        } catch (Exception e) {
            System.err.println("SampleReportDataCodeMatchesPrecision failed");
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
