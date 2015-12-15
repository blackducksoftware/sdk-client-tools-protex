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
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.component.ComponentApi;

/**
 * This sample retrieves a custom component by its label
 * 
 * It demonstrates:
 * - How to get a custom component by its label
 */
public class SampleGetCustomComponentByName extends BDProtexSample {

    private static ComponentApi componentApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetCustomComponentByName.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<custom component name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("custom component name", "The name of the Custom Component to get, i.e. \"My Code Printed Component\""));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String ccName = args[3];

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

            Component customComponent = null;

            try {
                List<Component> components = componentApi.getComponentsByName(ccName, null);
                if (components != null || components.size() == 1) {
                    customComponent = components.get(0);
                } else {
                    throw new RuntimeException("getCustomComponentByName() failed - multiple versions found with same name");
                }
            } catch (SdkFault e) {
                System.err.println("getCustomComponentByName() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println(customComponent.getComponentName() + " (" + customComponent.getComponentKey().getComponentId() + ") ");
            System.out.println("--------------------");
        } catch (Exception e) {
            System.err.println("SampleGetCustomComponentByName failed");
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
