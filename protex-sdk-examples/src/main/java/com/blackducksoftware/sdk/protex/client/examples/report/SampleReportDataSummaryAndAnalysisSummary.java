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
package com.blackducksoftware.sdk.protex.client.examples.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.CaptureOptions;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCount;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.AnalysisCodeTreeInfo;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.AnalysisEnvironmentInfo;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.AnalysisInfo;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.user.User;
import com.blackducksoftware.sdk.protex.user.UserApi;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

public class SampleReportDataSummaryAndAnalysisSummary extends BDProtexSample {

    private static UserApi userApi = null;

    private static ProjectApi projectApi = null;

    private static DiscoveryApi discoveryApi = null;

    private static CodeTreeApi codeTreeApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleReportDataSummaryAndAnalysisSummary.class.getSimpleName();

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

                userApi = myProtexServer.getUserApi();
                codeTreeApi = myProtexServer.getCodeTreeApi();
                projectApi = myProtexServer.getProjectApi();
                discoveryApi = myProtexServer.getDiscoveryApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // get CodeTree
            String root = "/";

            Project project = null;

            try {
                // For Summary Section
                project = projectApi.getProjectById(projectId);
            } catch (SdkFault e) {
                System.err.println("getProjectById failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            AnalysisEnvironmentInfo analysisEnvironmentInfo = null;

            try {
                analysisEnvironmentInfo = discoveryApi.getLastAnalysisEnvironmentInfo(projectId);
            } catch (SdkFault e) {
                System.err.println("getLastAnalysisEnvironmentInfo failed: " + e.getMessage());
                throw new RuntimeException(e);
            }


            System.out.println();
            System.out.println("--Summary--");
            System.out.println("Name: " + project.getName());
            User createBy = null;
            try {
                createBy = userApi.getUserByEmail(project.getCreatedBy());
            } catch (SdkFault e) {
                System.err.println("getUserByEmail failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
            System.out.println("Project Creator: " + createBy.getLastName() + ", " + createBy.getFirstName());
            System.out.println("License: " + project.getLicenseId());
            System.out.println("Description: " + (project.getDescription() == null ? "" : project.getDescription()));

            List<CodeTreeNode> nodes = null;

            try {
                CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
                codeTreeParameters.getIncludedNodeTypes().addAll(CodeTreeUtilities.ALL_CODE_TREE_NODE_TYPES);
                codeTreeParameters.setDepth(CodeTreeUtilities.SINGLE_NODE);
                codeTreeParameters.setIncludeParentNode(true);
                codeTreeParameters.getCounts().add(NodeCountType.FILES);
                codeTreeParameters.getCounts().add(NodeCountType.PENDING_ID_ALL);
                codeTreeParameters.getCounts().add(NodeCountType.VIOLATIONS);

                nodes = codeTreeApi.getCodeTreeNodes(projectId, root, codeTreeParameters);
            } catch (SdkFault e) {
                System.err.println("getFileCount failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            long fileIdentifiedCount = 0;
            long totalFileCount = 0;

            for (CodeTreeNode node : nodes) {
                Map<NodeCountType, Long> countMap = CodeTreeUtilities.getNodeCountMap(node);
                // this code tree has only a single node (the root)
                totalFileCount = countMap.get(NodeCountType.FILES);
                fileIdentifiedCount = countMap.get(NodeCountType.PENDING_ID_ALL);
            }

            System.out.println("Number of Files(): " + totalFileCount);
            int fileIdentifiedPercent = 100;
            if (totalFileCount != 0) {
                fileIdentifiedPercent = Math.round(((float) fileIdentifiedCount / (float) totalFileCount) * 100);
            }

            System.out.println("Files Pending Identification:  " + fileIdentifiedCount + " ("
                    + fileIdentifiedPercent + "%)");

            // GETTING Files with Violations
            int fileViolationsCount = 0;

            for (CodeTreeNode node : nodes) {
                for (NodeCount entry : node.getNodeCounts()) {
                    if (NodeCountType.VIOLATIONS.equals(entry.getCountType())) {
                        fileViolationsCount += entry.getCount();
                        break;
                    }
                }
            }

            int fileViolationsPercent = 100;
            if (totalFileCount != 0) {
                fileViolationsPercent = Math.round(((float) fileViolationsCount / (float) totalFileCount) * 100);
            }

            System.out.println("Files with Violations: " + fileViolationsCount + " (" + fileViolationsPercent + "%)");
            System.out.println("Server: " + analysisEnvironmentInfo.getHostname());

            AnalysisInfo analysisInfo = discoveryApi.getLastAnalysisInfo(projectId);

            try {
                analysisInfo = discoveryApi.getLastAnalysisInfo(projectId);
            } catch (SdkFault e) {
                System.err.println("getLastAnalysisInfo() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            AnalysisCodeTreeInfo analysisCodeTreeInfo = discoveryApi.getLastAnalysisCodeTreeInfo(projectId);
            try {
                analysisCodeTreeInfo = discoveryApi.getLastAnalysisCodeTreeInfo(projectId);
            } catch (SdkFault e) {
                System.err.println("getLastAnalysisCodeTreeInfo() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            CaptureOptions captureOptions = projectApi.getCaptureOptions(projectId);
            try {
                captureOptions = projectApi.getCaptureOptions(projectId);
            } catch (SdkFault e) {
                System.err.println("getCaptureOptions() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println();
            System.out.println("--ANALYSIS SUMMARY--");
            // System.out.println(String.format("Last Updated: %1$tm/%1$te/%1$tY %1$tH %1$tM"
            // ));
            String dateFormat = "%1$tB %1$te, %1$tY %1$tH:%1$tM %1$Tp";
            System.out.println(String.format("Analysis Started: " + dateFormat,
                    analysisInfo.getAnalysisStartedDate().getTime()));
            System.out.println(String.format("Analysis Finished: " + dateFormat,
                    analysisInfo.getAnalysisFinishedDate().getTime()));
            System.out.println("Files analyzed: " + analysisCodeTreeInfo.getAnalyzedFileCount());
            System.out.println(String.format("Bytes analyzed: %1$d KiB (%2$,d bytes)",
                    analysisCodeTreeInfo.getAnalyzedBytes() / 1024, analysisCodeTreeInfo.getAnalyzedBytes()));
            System.out.println("Files skipped: " + analysisCodeTreeInfo.getSkippedFileCount() + " Files");
            System.out.println("Bytes skipped: " + analysisCodeTreeInfo.getSkippedBytes() + " Bytes");
            System.out.println("Analysis Release Description: "
                    + analysisEnvironmentInfo.getAnalysisReleaseDescription());
            System.out.println("Analyzed From Host: " + analysisEnvironmentInfo.getHostname());
            System.out.println("Analyzed By: " + analysisInfo.getAnalyzedBy());
            System.out.println("Analyzed With OS: " + analysisEnvironmentInfo.getOsName());
            System.out.println("Analyzed With Locale: " + analysisEnvironmentInfo.getOsUserLanguage());
            System.out.println("---Analyzed with options---");
            System.out.println("\tFile Matches: " + convertToYesNo(captureOptions.getFileMatchesOption().isOption()));
            System.out.println("\tSnippet Matches: "
                    + convertToYesNo(captureOptions.getSnippetMatchesOption().isOption()));
            System.out.println("\tJava Import Statements: "
                    + convertToYesNo(captureOptions.getJavaImportOrCIncludeDependenciesOption().isOption()));
            System.out.println("\tJava Package Statements: "
                    + convertToYesNo(captureOptions.getJavaPackageStatementDependenciesOption().isOption()));
            System.out.println("\tBinary Dependencies: "
                    + convertToYesNo(captureOptions.getBinaryDependenciesOption().isOption()));
            System.out.println("\tString Searches: "
                    + convertToYesNo(captureOptions.getStringSearchesOption().isOption()));
            System.out.println("\tAllow wild cards (*) in string search queries: "
                    + convertToYesNo(captureOptions.getStringSearchWildcardsOption().isOption()));
            System.out.println("\tAllow regular expressions in string search queries: "
                    + convertToYesNo(captureOptions.getStringSearchRegularExpressionOption().isOption()));
            System.out.println("\tDecompress Compressed Files:  "
                    + convertToYesNo(captureOptions.getDecompressCompressedFilesOption().isOption()));
            System.out.println("\tExpand Archive files: "
                    + captureOptions.getExpandArchivesOption().getValue());
            System.out.println("\tEnable Multi User File Comparison: "
                    + convertToYesNo(captureOptions.getMultiUserFileComparisonOption().isOption()));
            System.out.println("\tBlock count Threshold: "
                    + captureOptions.getBlockCountThresholdOption().getOption());
            System.out.println("\tStore non-precision code matches: "
                    + convertToYesNo(captureOptions.getStoreNonPrecisionDiscoveriesOption().isOption()));
        } catch (Exception e) {
            System.err.println("SampleReportDataSummaryAndAnalysisSummary failed");
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

    private static final String YES = "Yes";

    private static final String NO = "No";

    private static String convertToYesNo(boolean val) {
        return val ? YES : NO;
    }

}
