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
import com.blackducksoftware.sdk.protex.common.CaptureOptions;
import com.blackducksoftware.sdk.protex.common.UploadSourceCodeOption;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;

/**
 * This sample creates new project
 * 
 * It demonstrates:
 * - How to create project
 */
public class SampleCreateProjectWithSourceCodeUpload extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCreateProjectWithSourceCodeUpload.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project name",
                "The name of the project to create, i.e. \"New Sample Project\" (include the quotes, if the name contains spaces)"));

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

            // Create the project Request
            ProjectRequest p = new ProjectRequest();
            p.setName(projectName);

            String projectId = null;
            try {
                projectId = projectApi.createProject(p, LicenseCategory.OPEN_SOURCE);
            } catch (SdkFault e) {
                System.err.println("createProject() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            try {
                CaptureOptions captureOptions = projectApi.getCaptureOptions(projectId);

                if (captureOptions.getUploadSourceCodeOption().isForced()) {
                    System.err.println("Can't change the Upload source code option it is forced by global policy");
                } else {
                    captureOptions.getUploadSourceCodeOption().setValue(UploadSourceCodeOption.ENABLED_NOT_ENCRYPTED);
                    projectApi.updateCaptureOptions(projectId, captureOptions);
                }
            } catch (SdkFault e) {
                System.err.println("createProject() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // Print some information returned
            System.out.println();
            System.out.println("New project '" + projectName + "' with project ID '" + projectId + "'");
        } catch (Exception e) {
            System.err.println("SampleCreateProjectWithSourceCodeUpload failed");
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
