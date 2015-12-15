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

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectScanLog;

/**
 * This sample retrieves all the scan logs for a scanned project
 * 
 * It demonstrates:
 * - How to get the scan logs for a project
 */
public class SampleGetAllProjectScanLogs extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetAllProjectScanLogs.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project name", "The name of the project to get scan logs for, i.e. \"New Sample Project\""));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 4) {
            System.err.println("\n\nNot enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectName = args[3];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                projectApi = myProtexServer.getProjectApi();
            } catch (RuntimeException e) {
                System.err.println("\nConnection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            String projectId = null;

            try {
                projectId = projectApi.getProjectByName(projectName).getProjectId();
            } catch (SdkFault e) {
                System.err.println("getProject() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (projectId != null) {
                List<ProjectScanLog> scanLogs = null;

                try {
                    scanLogs = projectApi.getAnalysisLogs(projectId, null, null);
                } catch (SdkFault e) {
                    System.err.println("getAnalysisLogs() failed: " + e.getMessage());
                    throw new RuntimeException(e);
                }

                if ((scanLogs != null) && (!scanLogs.isEmpty())) {
                    for (ProjectScanLog scanLog : scanLogs) {
                        System.out.println(scanLog.getSeverity().name() + ": " + scanLog.getScanTime() + ": " + scanLog.getScanMessage());
                    }
                } else {
                    System.out.println("No scan logs found for project '" + projectName + "'");
                }
            }
        } catch (Exception e) {
            System.err.println("SampleGetAllProjectScanLogs failed");
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
