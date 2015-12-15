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
import com.blackducksoftware.sdk.protex.obligation.ObligationCategory;
import com.blackducksoftware.sdk.protex.project.CloneOption;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectApi;

/**
 * This sample clones a given Project to a new name &quot;Clone of [project name]&quot;
 *
 * It requires:
 * - An existing project on the system
 *
 * It demonstrates:
 * - How to retrieve a project by its ID
 * - How to clone the project
 */
public class SampleCloneProject extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCloneProject.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID",
                "The ID of the project to clone, i.e. \"c_newsampleproject\""));

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
        Project clonedProject = null;
        String clonedProjectId = null;
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

            String clonedProjectName = "Copy of " + project.getName();
            System.out.println("Clone the project to '" + clonedProjectName + "'");

            List<ObligationCategory> resetAllFulfillments = new ArrayList<ObligationCategory>(0);
            List<CloneOption> analysisAndWork = new ArrayList<CloneOption>();
            analysisAndWork.add(CloneOption.ANALYSIS_RESULTS);
            analysisAndWork.add(CloneOption.ASSIGNED_USERS);
            analysisAndWork.add(CloneOption.COMPLETED_WORK);

            try {
                clonedProjectId = projectApi.cloneProject(projectId, clonedProjectName, analysisAndWork, resetAllFulfillments);
                System.out.println("Cloned project ID: " + clonedProjectId);
            } catch (SdkFault e) {
                System.err.println("cloneProject() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // check to see if it actually did get cloned regardless of any exceptions...

            try {
                clonedProject = projectApi.getProjectById(clonedProjectId);
            } catch (Exception e) {
                System.err.println("Failed to clone project " + clonedProject.getName() + " to " + clonedProjectName);
                System.exit(1);
            }
            if (clonedProject == null) {
                System.err.println("Failed to clone project " + clonedProject.getName() + " to " + clonedProjectName);
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("SampleCloneProject failed");
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
