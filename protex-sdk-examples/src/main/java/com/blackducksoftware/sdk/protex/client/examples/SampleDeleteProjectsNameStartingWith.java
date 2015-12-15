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
import com.blackducksoftware.sdk.protex.project.ProjectColumn;
import com.blackducksoftware.sdk.protex.project.ProjectPageFilter;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample deletes all projects where the name starts with a certain string. The string is a input
 * parameter.
 * 
 * It demonstrates:
 * - How to get a list of projects (assigned to the authenticating user)
 * - How to use PageFilters and the utility class(es) for sorting columns
 * - How to delete a project
 */
public class SampleDeleteProjectsNameStartingWith extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleDeleteProjectsNameStartingWith.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<name starts with>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("name starts with",
                "A string the project name starts with, i.e. \"Copy \""));

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
        String nameStartsWith = args[3];

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

            // Get a list of project I'm assigned to
            List<Project> projects = null;
            try {
                ProjectPageFilter pageFilter = PageFilterFactory.getAllRows(ProjectColumn.PROJECT_NAME);
                projects = projectApi.getProjects(pageFilter);
            } catch (SdkFault e) {
                System.err.println("\ngetProjects() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("\nDeleting Projects:");
            for (Project p : projects) {
                if (p.getName().startsWith(nameStartsWith)) {
                    System.out.println("\nDeleting: '" + p.getName() + "'");
                    try {
                        projectApi.deleteProject(p.getProjectId());
                    } catch (SdkFault f) {
                        System.err.println("\ndeleteProject() failed: " + f.getMessage());
                        // Don't exit just report the failure
                    }
                }
            }
            System.out.println("--------------------");
        } catch (Exception e) {
            System.err.println("SampleDeleteProjectsNameStartingWith failed");
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
