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
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectInfo;
import com.blackducksoftware.sdk.protex.project.ProjectInfoColumn;
import com.blackducksoftware.sdk.protex.project.ProjectInfoPageFilter;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how to list all projects, even those not assigned to the current user
 * 
 * It demonstrates:
 * - How to get a list of all projects (requires admin privileges for the user authenticating)
 */
public class SampleListAllProjectsWithAdminPrivileges extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleListAllProjectsWithAdminPrivileges.class.getSimpleName();
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
                throw new RuntimeException(e);
            }

            // Call the Api
            List<ProjectInfo> projects = null;

            ProjectInfoPageFilter pageFilter = PageFilterFactory.getAllRows(ProjectInfoColumn.PROJECT_NAME);
            try {
                // special case, suggestProjects with "" returns the list of all projects if the user has Admin
                // privileges
                projects = projectApi.suggestProjects("", pageFilter);
            } catch (SdkFault e) {
                System.err.println("suggestProjects() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (projects == null || projects.isEmpty()) {
                System.out.println("No projects found");
            } else {
                int i = 0;
                for (ProjectInfo project : projects) {
                    System.out.println("project (" + ++i + "): " + project.getName() + " (" + project.getProjectId() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("SampleListAllProjectsWithAdminPrivileges failed");
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
