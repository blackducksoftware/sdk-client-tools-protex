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

import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectColumn;
import com.blackducksoftware.sdk.protex.project.ProjectPageFilter;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how to list the projects assigned to the authenticated user
 * 
 * It demonstrates:
 * - How to get a list of projects (for the authenticated user)
 */
public class SampleListProjects extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleListProjects.class.getSimpleName();
        outputUsageDetails(className, getDefaultUsageParameters(), getDefaultUsageParameterDetails());
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 3) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                projectApi = myProtexServer.getProjectApi(0L);
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<Project> projects = null;

            try {
                ProjectPageFilter pageFilter = PageFilterFactory.getAllRows(ProjectColumn.PROJECT_NAME);
                projects = projectApi.getProjects(pageFilter);
            } catch (SdkFault e) {
                System.err.println("getProjects() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // List returns can be null due to how apache CXF handles empty lists
            if (projects == null || projects.size() == 0) {
                System.out.println("No Projects assigned to user '" + username + "'");
            } else {
                for (Project project : projects) {
                    System.out.println("Project: " + project.getName() + " (" + project.getProjectId() + ") License: "
                            + project.getLicenseId());
                }
            }
        } catch (Exception e) {
            System.err.println("SampleListProjects failed");
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
