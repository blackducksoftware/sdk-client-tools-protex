package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.BomRefreshMode;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
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
 */
public class SampleAddBomComponentVersion extends BDProtexSample {

    private static ProjectApi projectApi = null;

    private static ComponentApi componentApi = null;

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleAddBomComponentVersion.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project name>");
        parameters.add("<component name>");
        parameters.add("<version name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project name",
                "The name of an analyzed project, i.e. \"My Example Project\" (include the quotes, if the name contains spaces)"));
        paramDescriptions.add(formatUsageDetail("component name",
                "The name for the component to be added, i.e. \"Apache Tomcat\" (include the quotes, if the name contains spaces)"));
        paramDescriptions.add(formatUsageDetail("version name",
                "The name for the version of the component to be added, i.e. \"6.0.24\" (include the quotes, if the name contains spaces)"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 6) {
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
        String versionName = args[5];

        ProtexServerProxy myProtexServer = null;

        // get service and service port
        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                componentApi = myProtexServer.getComponentApi();
                projectApi = myProtexServer.getProjectApi();
                bomApi = myProtexServer.getBomApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            String projectId = null;

            try {
                Project project = projectApi.getProjectByName(projectName);
                projectId = project.getProjectId();
            } catch (SdkFault e) {
                System.err.println("ProjectApi.getProjectByName() failed");
                throw new RuntimeException(e);
            }

            Component version = null;

            try {
                List<Component> versions = componentApi.getComponentsByName(componentName, versionName);
                if (versions != null && versions.size() == 1) {
                    version = versions.get(0);
                } else {
                    throw new RuntimeException("ComponentApi.getComponentsByName() failed - " + (versions != null ? "multiple" : "no") + " versions found");
                }
            } catch (SdkFault e) {
                System.err.println("ComponentApi.getComponentsByName() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("Component Version: " + componentName + " "
                    + versionName + " ==> " + version.getComponentKey().getComponentId()
                    + "#" + version.getComponentKey().getVersionId());

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
