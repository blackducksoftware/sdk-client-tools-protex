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
import com.blackducksoftware.sdk.protex.common.StringSearchPattern;
import com.blackducksoftware.sdk.protex.project.ProjectApi;

/**
 * This sample demonstrates how to list string search patterns for a project
 * 
 * It demonstrates:
 * - How to get a list of String Search Patterns for a project
 * 
 */
public class SampleProjectListStringSearchPatterns extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleProjectListStringSearchPatterns.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID of the project , i.e. \"c_newsampleproject\""));

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

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                projectApi = myProtexServer.getProjectApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<StringSearchPattern> patterns = null;

            try {
                patterns = projectApi.getStringSearchPatterns(projectId);
            } catch (SdkFault e) {
                System.err.println("getStringSearchPatterns() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (patterns == null || patterns.isEmpty()) {
                System.out.println("No patterns found for project: '" + projectId + "'");
            } else {
                for (StringSearchPattern pattern : patterns) {
                    projectApi.getStringSearchPatternById(projectId, pattern.getStringSearchPatternId());
                    System.out.println("Pattern: " + pattern.getName() + " (" + pattern.getAdvancedPattern() + ") License: "
                            + pattern.getAssociatedLicenseId());
                }
            }
        } catch (Exception e) {
            System.err.println("SampleProjectListStringSearchPatterns failed");
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
