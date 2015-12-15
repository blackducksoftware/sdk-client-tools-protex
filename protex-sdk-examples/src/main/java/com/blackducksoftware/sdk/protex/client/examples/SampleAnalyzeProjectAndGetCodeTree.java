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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.sdk.fault.ErrorCode;
import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.AnalysisStatus;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceRepository;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

/**
 * This sample creates a project with a given source tree and runs the protex analysis
 * 
 * It demonstrates:
 * - How to create a project
 * - How to set the projects source location on the server
 * - How to start the analysis of the source code
 * - How to monitor the progress of the analysis and wait until it is done
 * - How to retrieve the resulting code tree in the project
 */
public class SampleAnalyzeProjectAndGetCodeTree extends BDProtexSample {

    private static CodeTreeApi codeTreeApi = null;

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleAnalyzeProjectAndGetCodeTree.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project name>");
        parameters.add("<server source directory>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project name",
                "The name for the project to be created and analyzed, i.e. \"My Example Project\" (include the quotes, if the name contains spaces)"));
        paramDescriptions.add(formatUsageDetail("server source directory", "the directory on the server where the source "
                + "to be analyzed can be found, i.e. \"my-example\" (include the quotes, if the name contains spaces). The "
                + "source directory is relative to the \"blackduck.serverFileURL\", by default this is /home/blackduck, if "
                + "that does not exist it is the home dir of the user running the protex-tomcat. This is the same location where "
                + "the UI opens the tree for selecting a source dir. You can also find this location under http://<your protex server>/protex/monitor?v=1"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 5) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        Long connectionTimeout = 120 * 1000L;

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectName = args[3];
        String serverSourceDir = args[4];

        ProtexServerProxy myProtexServer = null;

        try {
            // get service and service port
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                codeTreeApi = myProtexServer.getCodeTreeApi();
                projectApi = myProtexServer.getProjectApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Create project
            ProjectRequest projectRequest = new ProjectRequest();
            projectRequest.setName(projectName);
            projectRequest.setDescription("A Sample project");

            // Set Source Location
            AnalysisSourceLocation sourceLocation = new AnalysisSourceLocation();
            URL hostUrl = new URL(serverUri);
            sourceLocation.setHostname(hostUrl.getHost());
            sourceLocation.setRepository(AnalysisSourceRepository.REMOTE_SERVER);
            // Source path relative to the source root set on the server, i.e. "my-example" if the source is in
            // /home/blackduck/my-example
            sourceLocation.setSourcePath(serverSourceDir);

            projectRequest.setAnalysisSourceLocation(sourceLocation);

            String projectId = null;

            try {
                projectId = projectApi.createProject(projectRequest, LicenseCategory.PROPRIETARY);
            } catch (SdkFault e) {
                System.err.println("ProjectApi.createProject() failed");
                throw new RuntimeException(e);
            }

            System.out.println("Project '" + projectName + "' created with source path '" + serverSourceDir + "'");

            // Start Analysis
            try {
                projectApi.startAnalysis(projectId, Boolean.FALSE);
            } catch (SdkFault e) {
                System.err.println("ProjectApi.startAnalysis() failed");
                throw new RuntimeException(e);
            }

            // Wait until Analysis is finished
            waitForScanToComplete(projectId);

            // get code tree generated during scan, and the number of files analyzed (files count themselves)
            String rootPath = "/";

            List<CodeTreeNode> nodes = null;

            try {
                CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
                codeTreeParameters.getIncludedNodeTypes().addAll(CodeTreeUtilities.ALL_CODE_TREE_NODE_TYPES);
                codeTreeParameters.setDepth(CodeTreeUtilities.INFINITE_DEPTH);
                codeTreeParameters.setIncludeParentNode(true);
                codeTreeParameters.getCounts().add(NodeCountType.FILES);

                nodes = codeTreeApi.getCodeTreeNodes(projectId, rootPath, codeTreeParameters);
            } catch (SdkFault e) {
                System.err.println("CodeTreeApi.getCodeTreeNodes() failed");
                throw new RuntimeException(e);
            }

            if (nodes == null) {
                System.err.println("CodeTreeApi.getCodeTreeNodes() returned no nodes");
                throw new RuntimeException("CodeTreeApi.getCodeTreeNodes() returned no nodes");
            }

            // Print the number of files analyzed. Files count themselves
            for (CodeTreeNode node : nodes) {
                Map<NodeCountType, Long> countMap = CodeTreeUtilities.getNodeCountMap(node);
                System.out.println(rootPath + node.getName() + " (" + node.getNodeType() + ")" + " [" + countMap.get(NodeCountType.FILES) + " files analyzed]");
            }
        } catch (Exception e) {
            System.err.println("SampleAnalyzeProjectAndGetCodeTree failed");
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

    /**
     * Waits until a project scan is complete
     * 
     * @param projectId
     *            The ID of the project to wait for scan completion on
     */
    public static void waitForScanToComplete(String projectId) {
        boolean finished = false;
        long start = System.currentTimeMillis();

        while (!finished) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

            try {
                AnalysisStatus status = projectApi.getAnalysisStatus(projectId);
                System.out.println("Phase: " + status.getAnalysisPhase() + " (" + status.getCurrentPhasePercentCompleted() + "%)");
                System.out.println("Files analyzed: " + status.getAnalyzedFileCount()
                        + " ( pending Analysis: " + status.getAnalysisPendingFileCount() + ")");
                finished = status.isFinished();
            } catch (SdkFault e) {
                if (ErrorCode.NO_ANALYSIS_RUNNING.equals(e.getFaultInfo().getErrorCode())) {
                    // This means the scan is complete
                    finished = true;
                } else {
                    System.err.println("ProjectApi.getAnalysisStatus failed");
                    throw new RuntimeException(e);
                }
            }
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Finished analysis in " + duration / 1000 + " sec.");
    }

}
