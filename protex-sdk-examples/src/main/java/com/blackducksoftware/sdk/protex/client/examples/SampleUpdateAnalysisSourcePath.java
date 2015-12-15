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
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;

/**
 * This sample demonstrates how to update the source directory of a project.
 * 
 * Remember source directories can be client or server related and different rules apply. However only projects with
 * sources located on the server can be analyzed via the SDK.
 * 
 * It demonstrates:
 * - How to update the analysis source location of a project
 */
public class SampleUpdateAnalysisSourcePath extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleUpdateAnalysisSourcePath.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");
        parameters.add("<new source directory>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The project ID of the project to update, i.e. c_testproject"));
        paramDescriptions.add(formatUsageDetail("new source directory",
                "The new source directory on the remote server (relative to the server source dir, typically /home/blackduck), i.e. TestProject"));

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
        String newSourceDir = args[4];

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
                if (ErrorCode.PROJECT_NOT_FOUND.equals(e.getFaultInfo().getErrorCode())) {
                    System.err.println("Did not find project with ID " + projectId);
                } else {
                    System.err.println("getProjectById() failed: " + e.getMessage());
                }

                throw new RuntimeException(e);
            }

            AnalysisSourceLocation sourceLocation = project.getAnalysisSourceLocation();
            // *** don't change these attributes (unless you really want to), only the directory
            // sourceLocation.setRepository(ProjectSourceRepository.REMOTE_SERVER);
            // sourceLocation.setHost(hostName);
            sourceLocation.setSourcePath(newSourceDir);

            System.out.println(" SourceLocation: " + sourceLocation.getRepository() + "::"
                    + sourceLocation.getHostname()
                    + "::" + sourceLocation.getSourcePath());

            ProjectRequest updateRequest = new ProjectRequest();
            updateRequest.setAnalysisSourceLocation(sourceLocation);

            try {
                projectApi.updateProject(project.getProjectId(), updateRequest);
            } catch (SdkFault e) {
                System.err.println("updateProject() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("Project source path updated successfully.");
        } catch (Exception e) {
            System.err.println("SampleUpdateAnalysisSourcePath failed");
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
