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

import com.blackducksoftware.sdk.fault.ErrorCode;
import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.AnalysisStatus;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectApi;

/**
 * This sample checks on a projects status, weather it is an analysis or has been analyzed at all
 * 
 * It demonstrates:
 * - How to monitor the progress of the analysis and wait until it is done
 * - How to read if a project has ever been analyzed read the last analyzed date
 * 
 */
public class SampleGetProjectStatus extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetProjectStatus.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID for the project that you want to know the status off, i.e. c_test-project"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
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
                projectApi = myProtexServer.getProjectApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            Project project = null;

            try {
                project = projectApi.getProjectById(projectId);
            } catch (SdkFault e) {
                System.err.println("getProjectById() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out
            .println("Project: " + project.getName() + ", created on - " + project.getCreationDate() + ", last Analyzed " + project.getLastAnalyzedDate());

            if (project.getLastAnalyzedDate() == null) {
                System.out.println("No analysis has ever been run for project " + project.getName());
            } else {
                // Wait until Analysis is finished
                waitTilDone(projectId);
            }
        } catch (Exception e) {
            System.err.println("SampleGetProjectStatus failed");
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
     * Waits until the Analysis is done
     * 
     * @param projectId
     *            The ID of the project to wait on
     */
    private static void waitTilDone(String projectId) {
        boolean finished = false;
        long start = System.currentTimeMillis();

        while (!finished) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

            AnalysisStatus status;
            try {
                status = projectApi.getAnalysisStatus(projectId);
                System.out.println("Phase: " + status.getAnalysisPhase() + " ("
                        + status.getCurrentPhasePercentCompleted() + "%)");
                System.out.println("Files analyzed: " + status.getAnalyzedFileCount() + " ( pending Analysis: "
                        + status.getAnalysisPendingFileCount() + ")");
                finished = status.isFinished();
            } catch (SdkFault e) {
                if (ErrorCode.NO_ANALYSIS_RUNNING.equals(e.getFaultInfo().getErrorCode())) {
                    System.out.println(e.getMessage());
                    return; // end the loop as there is nothing to wait for
                } else {
                    System.err.println(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Finished analysis in " + duration / 1000 + " sec.");
    }

}
