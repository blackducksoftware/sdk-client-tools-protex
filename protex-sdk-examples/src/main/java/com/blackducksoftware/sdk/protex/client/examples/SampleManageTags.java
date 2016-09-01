/*
 * Black Duck Software Suite SDK
 * Copyright (C) 2015  Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.BomRefreshMode;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.license.GlobalLicense;
import com.blackducksoftware.sdk.protex.license.LicenseApi;
import com.blackducksoftware.sdk.protex.license.LicenseInfo;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponentRequest;

/**
 * This sample adds a (standard) component with a specific version to the bill of material (BOM) of a given project
 * 
 * It requires:
 * - An analyzed project which already exists on the server
 * 
 * It demonstrates:
 * - How to retrieve a project by its name
 * - How to retrieve a standard component version by its name
 * - How to add the component version to the BOM of a project
 * - How to add and remove tags to projects, components, and licenses
 */
public class SampleManageTags extends BDProtexSample {

    private static ProjectApi projectApi = null;

    private static ComponentApi componentApi = null;

    private static LicenseApi licenseApi = null;

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleManageTags.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project name>");
        parameters.add("<component name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project name",
                "The name of an analyzed project, i.e. \"My Example Project\" (include the quotes, if the name contains spaces)"));
        paramDescriptions.add(formatUsageDetail("component name",
                "The name for the component to be added, i.e. \"Apache Tomcat\" (include the quotes, if the name contains spaces)"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 5) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        Long connectionTimeout = 120 * 1000L;

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectName = args[3];
        String componentName = args[4];

        ProtexServerProxy myProtexServer = null;

        // get service and service port
        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                componentApi = myProtexServer.getComponentApi();
                projectApi = myProtexServer.getProjectApi();
                bomApi = myProtexServer.getBomApi();
                licenseApi = myProtexServer.getLicenseApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            String projectId = null;
            String projectLicenseId = null;

            try {
                Project project = projectApi.getProjectByName(projectName);
                projectId = project.getProjectId();
                projectLicenseId = project.getLicenseId();
            } catch (SdkFault e) {
                System.err.println("ProjectApi.getProjectByName() failed");
                throw new RuntimeException(e);
            }

            Component version = null;

            try {
                List<Component> versions = componentApi.getComponentsByName(componentName, null);
                version = versions.get(0);
            } catch (SdkFault e) {
                System.err.println("ComponentApi.getComponentByName() failed");
                throw new RuntimeException(e);
            }

            System.out.println("Component: " + componentName + " ==> " + version.getComponentKey().getComponentId());

            BomComponentRequest bomCompRequest = new BomComponentRequest();
            bomCompRequest.setComponentKey(version.getComponentKey());
            bomCompRequest.setUsageLevel(UsageLevel.COMPONENT_DYNAMIC_LIBRARY);

            // Get the first license of the component
            LicenseInfo firstLicense = version.getLicenses().get(0);
            if (firstLicense != null) {
                // If any license exists use it as the license to be used.
                bomCompRequest.setLicenseId(firstLicense.getLicenseId());
            }

            try {
                bomApi.addBomComponent(projectId, bomCompRequest, BomRefreshMode.ASYNCHRONOUS);
            } catch (SdkFault e) {
                System.err.println("BomApi.addBomComponent failed");
                throw new RuntimeException(e);
            }

            // Add a tag to the project, the project license, and the added BOM component
            try {
                projectApi.addTag(username, projectId, "SampleGeneratedTag");
                componentApi.addTag(username, version.getComponentKey().getComponentId(), "SampleGeneratedTag");
                licenseApi.addTag(username, projectLicenseId, "SampleGeneratedTag");
            } catch (SdkFault e) {
                System.err.println("Adding tags failed");
                throw new RuntimeException(e);
            }

            List<String> projectTags = null;
            List<String> componentTags = null;
            List<GlobalLicense> taggedLicenses = null;

            try {
                projectTags = projectApi.getTags(username, projectId);
                componentTags = componentApi.getTags(username, version.getComponentKey().getComponentId());
                taggedLicenses = licenseApi.getTaggedLicenses(username, "SampleGeneratedTag");
            } catch (SdkFault e) {
                System.err.println("Retrieving tags failed");
                throw new RuntimeException(e);
            }

            StringBuilder projectTagBuilder = new StringBuilder();
            for (String projectTag : projectTags) {
                if (projectTagBuilder.length() != 0) {
                    projectTagBuilder.append(", ");
                }

                projectTagBuilder.append(projectTag);
            }

            projectTagBuilder.insert(0, "Tags on Project " + projectName + ": ");

            StringBuilder componentTagBuilder = new StringBuilder();
            for (String componentTag : componentTags) {
                if (componentTagBuilder.length() != 0) {
                    componentTagBuilder.append(", ");
                }

                componentTagBuilder.append(componentTag);
            }

            componentTagBuilder.insert(0, "Tags on Component " + componentName + ": ");

            StringBuilder taggedLicenseBuilder = new StringBuilder();
            for (GlobalLicense license : taggedLicenses) {
                if (taggedLicenseBuilder.length() != 0) {
                    taggedLicenseBuilder.append(", ");
                }

                taggedLicenseBuilder.append(license.getName());
            }

            taggedLicenseBuilder.insert(0, "Licenses with tag 'SampleGeneratedTag': ");

            System.out.println(projectTagBuilder.toString());
            System.out.println(componentTagBuilder.toString());
            System.out.println(taggedLicenseBuilder.toString());
        } catch (Exception e) {
            System.err.println("SampleAddBomComponentVersion failed");
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
