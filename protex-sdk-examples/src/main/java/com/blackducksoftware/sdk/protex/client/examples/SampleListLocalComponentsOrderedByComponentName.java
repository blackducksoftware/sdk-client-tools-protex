package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.license.License;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalComponent;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalComponentApi;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalComponentColumn;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalComponentPageFilter;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how list all local components for a project
 * 
 * It demonstrates:
 * - How to get a list local components used in a project
 * - How to get the local license and its text if applicable
 */
public class SampleListLocalComponentsOrderedByComponentName extends BDProtexSample {

    private static LocalComponentApi localComponentApi = null;

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleListLocalComponentsOrderedByComponentName.class.getSimpleName();

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

                localComponentApi = myProtexServer.getLocalComponentApi();
                projectApi = myProtexServer.getProjectApi();
            } catch (RuntimeException e) {
                System.err.println("\nConnection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<LocalComponent> localComponents = null;
            LocalComponentPageFilter pageFilter = PageFilterFactory.getAllRows(LocalComponentColumn.COMPONENT_NAME);

            try {
                localComponents = localComponentApi.getLocalComponents(projectId, pageFilter);
            } catch (SdkFault e) {
                System.err.println("getLocalComponents() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (localComponents == null || localComponents.isEmpty()) {
                System.out.println("No LocalComponents returned");
            } else {
                for (LocalComponent localComponent : localComponents) {
                    try {
                        String licenseId = getLicenseId(localComponent);
                        if (licenseId != null && !licenseId.isEmpty()) {
                            License license = projectApi.getLicenseById(projectId, getLicenseId(localComponent));
                            System.out.println("LocalComponent: " + localComponent.getComponentName() + ", "
                                    + localComponent.getComponentKey().getComponentId() + " ("
                                    + (license.getText() != null ? new String(license.getText()) : "")
                                    + ")");
                        }
                    } catch (SdkFault e) {
                        System.err.println("getLicenseById() failed: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("SampleListLocalComponentsOrderedByComponentName failed");
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

    private static String getLicenseId(LocalComponent localComponent) {
        if (localComponent == null || localComponent.getLicenses() == null || localComponent.getLicenses().isEmpty()) {
            return "";
        }

        return localComponent.getLicenses().get(0).getLicenseId();
    }
}
