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
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectApi;

/**
 * This sample deletes a project
 * 
 * It requires:
 * - An existing project on the system (will be deleted by this sample)
 * 
 * It demonstrates:
 * - How to get a project by its ID
 * - How to delete a project
 */
public class SampleDeleteProject extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleDeleteProject.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID",
                "The ID of the project to delete, i.e. \"c_newsampleproject\""));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 4) {
            System.err.println("\nNot enough parameters!");
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
                System.err.println("\nConnection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            Project project = null;

            // Get the project first so we can use it's name for more user friendly error reporting
            try {
                project = projectApi.getProjectById(projectId);
            } catch (SdkFault e) {
                System.err.println("\ngetProjectById() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }


            try {
                projectApi.deleteProject(projectId);
                System.out.println("\nProject '" + project.getName() + "' deleted");
            } catch (SdkFault e) {
                System.err.println("\ndeleteProject() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.err.println("SampleDeleteProject failed");
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
