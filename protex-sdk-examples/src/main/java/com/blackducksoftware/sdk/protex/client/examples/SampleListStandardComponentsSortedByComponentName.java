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
import com.blackducksoftware.sdk.protex.common.ComponentColumn;
import com.blackducksoftware.sdk.protex.common.ComponentPageFilter;
import com.blackducksoftware.sdk.protex.common.ComponentType;
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how to suggest a list of standard components matching a suggested string
 * 
 * It demonstrates:
 * - How to get a list of potential components matching a suggest pattern
 * - How to use the PageFilter in oder to influence the number of returned elements and the sort order
 * - How to use the component type filter to only return standard components
 * - How to get more detail about every component suggested
 */
public class SampleListStandardComponentsSortedByComponentName extends BDProtexSample {

    private static ComponentApi componentApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleListStandardComponentsSortedByComponentName.class.getSimpleName();
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

                componentApi = myProtexServer.getComponentApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            List<Component> components = null;

            try {
                ComponentPageFilter pageFilter = PageFilterFactory.getFirstPage(20, ComponentColumn.NAME, Boolean.TRUE);
                pageFilter.setIncludeDeprecated(false);
                pageFilter.getComponentTypes().clear();
                pageFilter.getComponentTypes().add(ComponentType.STANDARD);
                pageFilter.getComponentTypes().add(ComponentType.STANDARD_MODIFIED);

                components = componentApi.getComponents(pageFilter);
            } catch (SdkFault e) {
                System.err.println("getStandardComponents() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (components == null || components.isEmpty()) {
                System.err.println("No Components returned");
            } else {
                for (Component component : components) {
                    System.out.println("Name: " + component.getComponentName() + ";   Id: " + component.getComponentKey().getComponentId()
                            + ";   Homepage: "
                            + component.getHomePage() + ";   Description: " + component.getDescription());
                }
            }
        } catch (Exception e) {
            System.err.println("SampleListStandardComponentsSortedByComponentName failed");
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
