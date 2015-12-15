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
import com.blackducksoftware.sdk.protex.common.ComponentPageFilter;
import com.blackducksoftware.sdk.protex.common.ComponentType;
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample deletes all custom components where the name starts with a certain string. The string is a input
 * parameter
 * 
 * It demonstrates:
 * - How to get a list of custom components
 * - How to delete a custom component
 * - How to use PageFilters and the utility class(es) for sorting columns
 */
public class SampleDeleteCustomComponentsNameStartingWith extends BDProtexSample {

    private static ComponentApi componentApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleDeleteCustomComponentsNameStartingWith.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<name starts with>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("name starts with",
                "A string the custom component name starts with, i.e. \"Copy \""));

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
        String nameStartsWith = args[3];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                componentApi = myProtexServer.getComponentApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Get a list of project I'm assigned to
            List<Component> customComponents = null;

            try {
                ComponentPageFilter pageFilter = PageFilterFactory.getAllRows(ComponentColumn.NAME);
                pageFilter.getComponentTypes().clear();
                pageFilter.getComponentTypes().add(ComponentType.CUSTOM);

                customComponents = componentApi.getComponents(pageFilter);
            } catch (SdkFault e) {
                System.err.println("getCustomComponents() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("Deleting Custom Components:");
            for (Component cc : customComponents) {
                if (cc.getComponentName().startsWith(nameStartsWith)) {
                    System.out.println("Deleting: '" + cc.getComponentName() + "'");
                    try {
                        componentApi.deleteComponent(cc.getComponentKey());
                    } catch (SdkFault f) {
                        System.err.println("Delete failed: " + f.getMessage());
                        // Don't exit just report the failure
                    }
                }
            }

            System.out.println("--------------------");
        } catch (Exception e) {
            System.err.println("SampleCreateProject failed");
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
