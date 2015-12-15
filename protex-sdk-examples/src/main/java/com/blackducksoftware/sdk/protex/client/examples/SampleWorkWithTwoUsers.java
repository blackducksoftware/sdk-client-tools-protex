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
import com.blackducksoftware.sdk.protex.project.ProjectColumn;
import com.blackducksoftware.sdk.protex.project.ProjectPageFilter;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how to work with two users authenticated to the same server.
 * 
 * It demonstrates:
 * - How to authenticate each user against the server
 * - How to use the services for each user separately
 * - How to retrieve projects assigned to the authenticated user
 */
public class SampleWorkWithTwoUsers extends BDProtexSample {

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleWorkWithTwoUsers.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<username 2>");
        parameters.add("<password 2>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("username 2", "A second username for this server, i.e. tester2@example.com"));
        paramDescriptions.add(formatUsageDetail("password 2", "The password for the second user, i.e. simplepassword2"));

        outputUsageDetails(className, parameters, paramDescriptions);

    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 5) {
            System.err.println("\nNot enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String username2 = args[3];
        String password2 = args[4];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer1 = null;
        ProtexServerProxy myProtexServer2 = null;

        try {
            try {
                myProtexServer1 = new ProtexServerProxy(serverUri, username, password, connectionTimeout);
                myProtexServer2 = new ProtexServerProxy(serverUri, username2, password2, connectionTimeout);
            } catch (RuntimeException e) {
                System.err.println("\nConnection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<Project> projectByUser1 = null;
            ProjectPageFilter pageFilter = PageFilterFactory.getAllRows(ProjectColumn.PROJECT_NAME);

            System.out.println();
            System.out.println("Projects for user 1:" + username);
            System.out.println("===================");

            try {
                projectByUser1 = myProtexServer1.getProjectApi().getProjects(pageFilter);
            } catch (SdkFault e) {
                System.err.println("getProjects() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (projectByUser1 == null || projectByUser1.isEmpty()) {
                System.out.println("No Projects returned");
            } else {
                for (Project project : projectByUser1) {
                    System.out.println("  Project: " + project.getName() + " (" + project.getProjectId()
                            + ") - created By: "
                            + project.getCreatedBy());
                }
            }
            System.out.println();
            System.out.println();

            List<Project> projectsByUser2 = null;

            try {
                projectsByUser2 = myProtexServer2.getProjectApi().getProjects(pageFilter);
            } catch (SdkFault e) {
                System.err.println("getProjects() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println();
            System.out.println("Projects for user 2:" + username2);
            System.out.println("===================");

            if (projectsByUser2 == null || projectsByUser2.isEmpty()) {
                System.out.println("No Projects returned");
            } else {
                for (Project project : projectsByUser2) {
                    System.out.println("  Project: " + project.getName() + " (" + project.getProjectId()
                            + ") - created By: "
                            + project.getCreatedBy());
                }
            }
        } catch (Exception e) {
            System.err.println("SampleWorkWithTwoUsers failed");
            e.printStackTrace(System.err);
            System.exit(-1);
        } finally {
            // This is optional - it causes the proxy to overwrite the stored password with null characters, increasing
            // security
            if (myProtexServer1 != null) {
                myProtexServer1.close();
            }
            if (myProtexServer2 != null) {
                myProtexServer2.close();
            }
        }
    }

}
