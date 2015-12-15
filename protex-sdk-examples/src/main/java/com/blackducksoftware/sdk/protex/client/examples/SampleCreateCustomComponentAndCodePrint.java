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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.component.ComponentRequest;
import com.blackducksoftware.sdk.protex.component.custom.CustomComponentManagementApi;
import com.blackducksoftware.sdk.protex.component.custom.CustomComponentSettings;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceRepository;

/**
 * This sample creates a project with a given source tree and runs the protex analysis
 * 
 * It demonstrates:
 * - How to create a project
 * - How to set the projects source location on the server
 * - How to start the analysis of the source code
 * - How to monitor the progress of the analysis and wait until it is done
 * - How to retrieve the resulting code tree in the project
 * 
 */
public class SampleCreateCustomComponentAndCodePrint extends BDProtexSample {

    private static ComponentApi componentApi = null;

    private static CustomComponentManagementApi customComponentManagementApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCreateCustomComponentAndCodePrint.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<component name>");
        parameters.add("<server source directory>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project name",
                "The name for the component to be created and codeprinted, i.e. \"My Example Project\" (include the quotes, if the name contains spaces)"));
        paramDescriptions.add(formatUsageDetail("server source directory", "the directory on the server where the source "
                + "to be analyzed can be found, i.e. \"my-example\" (include the quotes, if the name contains spaces). The "
                + "source directory is relative to the \"blackduck.serverFileURL\", by default this is /home/blackduck, if "
                + "that does not exist it is the home dir of the user running the protex-tomcat. This is the same location where "
                + "the UI opens the tree for selecting a source dir. You can also find this location under http://<your protex server>/protex/monitor?v=1"));

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
        String projectName = args[3];
        String serverSourceDir = args[4];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            // get service and service port
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);
                componentApi = myProtexServer.getComponentApi();
                customComponentManagementApi = myProtexServer.getCustomComponentManagementApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Create project
            ComponentRequest componentRequest = new ComponentRequest();
            componentRequest.setComponentName(projectName);
            componentRequest.setDescription("A Sample custom Component");
            componentRequest.getLicenseIds().add("gpl20");

            ComponentKey componentKey = null;

            try {
                componentKey = componentApi.createComponent(componentRequest);
            } catch (SdkFault e) {
                System.err.println("createCustomComponent() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("Custom Component ID: " + componentKey);

            CustomComponentSettings settings = customComponentManagementApi.getCustomComponentSetting(componentKey);

            // Set Source Location
            AnalysisSourceLocation sourceLocation = new AnalysisSourceLocation();
            URL hostUrl = new URL(serverUri);
            // Host name is just a string for documentation purposes. It does not determine from which host the
            // source
            // code is scanned
            sourceLocation.setHostname(hostUrl.getHost());
            sourceLocation.setRepository(AnalysisSourceRepository.REMOTE_SERVER);
            // Source path relative to the source root set on the server, i.e. "my-example" if the source is in
            // /home/blackduck/my-example
            sourceLocation.setSourcePath(serverSourceDir);
            settings.setAnalysisSourceLocation(sourceLocation);

            try {
                customComponentManagementApi.updateCustomComponentSettings(Arrays.asList(settings));
                customComponentManagementApi.startCodeprinting(componentKey, Boolean.TRUE);
            } catch (SdkFault e) {
                System.err.println("updateCustomComponent() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.err.println("SampleCreateCustomComponentAndCodePrint failed");
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
