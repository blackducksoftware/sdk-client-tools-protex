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
import com.blackducksoftware.sdk.protex.common.ComponentColumn;
import com.blackducksoftware.sdk.protex.common.ComponentInfo;
import com.blackducksoftware.sdk.protex.common.ComponentPageFilter;
import com.blackducksoftware.sdk.protex.common.ComponentType;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how to suggest a list of standard components matching a suggested string
 * 
 * It demonstrates:
 * - How to get a list of potential components matching a suggest pattern
 * - How to use the PageFilter in oder to influence the number of returned elements and the sort order
 * - How to use the component type filter to only return standard components
 * - How to get more detail about every component suggested
 * 
 */
public class SampleSuggestStandardComponents extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleSuggestStandardComponents.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");
        parameters.add("<anyWordStartsWith>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID",
                "The project ID of the project to list, i.e. c_testproject"));
        paramDescriptions.add(formatUsageDetail("anyWordStartsWith",
                "A string that matches the start of any word in the Component's name, for example 'Jakarta'"));

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
        String anyWordStartsWith = args[4];

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

            List<ComponentInfo> components = null;

            // Make the actual call
            try {
                ComponentPageFilter pageFilter = PageFilterFactory.getAllRows(ComponentColumn.NAME);
                pageFilter.getComponentTypes().clear();
                pageFilter.getComponentTypes().add(ComponentType.STANDARD);
                pageFilter.setIncludeDeprecated(false);

                components = projectApi.suggestComponents(projectId, anyWordStartsWith, pageFilter);
            } catch (SdkFault e) {
                System.err.println("suggestComponentsFiltered() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (components == null || components.size() == 0) {
                System.err.println("No Components returned");
            } else {
                for (ComponentInfo ci : components) {
                    System.out.println("Name: " + ci.getComponentName() + ";   Id: " + ci.getComponentKey().getComponentId()
                            + ";   Approval: " + ci.getApprovalState());
                }
            }
        } catch (Exception e) {
            System.err.println("SampleSuggestStandardComponents failed");
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
