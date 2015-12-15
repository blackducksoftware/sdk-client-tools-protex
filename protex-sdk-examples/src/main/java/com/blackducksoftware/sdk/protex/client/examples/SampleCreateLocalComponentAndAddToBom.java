package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.BomRefreshMode;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponentRequest;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalComponentApi;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalComponentRequest;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalLicenseRequest;

/**
 * This sample creates a local component and adds it to the bill of material (BOM)
 * 
 * It demonstrates:
 * - How to create a local component (only visible in the context of that project)
 * - How to add a component to the BOM
 */
public class SampleCreateLocalComponentAndAddToBom extends BDProtexSample {

    private static LocalComponentApi localComponentApi = null;

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCreateLocalComponentAndAddToBom.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");
        parameters.add("<local component name>");
        parameters.add("<license ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The project ID of the project to create a local component, i.e. c_testproject"));
        paramDescriptions.add(formatUsageDetail("local component name", "The name of component, i.e.\"Test Local Component\""));
        paramDescriptions.add(formatUsageDetail("license ID", "The license ID of the license to create a component, i.e. Apache20"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 6) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectId = args[3];
        String name = args[4];
        String licenseId = args[5];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                localComponentApi = myProtexServer.getLocalComponentApi();
                bomApi = myProtexServer.getBomApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // Create the local component
            ComponentKey localComponentKey = null;

            LocalComponentRequest localComponentRequest = new LocalComponentRequest();
            localComponentRequest.setName(name);

            LocalLicenseRequest localLicenseRequest = new LocalLicenseRequest();
            localLicenseRequest.setBasedOnLicenseId(licenseId);

            try {
                localComponentKey = localComponentApi.createLocalComponent(projectId, localComponentRequest, localLicenseRequest);
            } catch (SdkFault e) {
                System.err.println("createLocalComponent() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("Local Component Id: " + localComponentKey);

            BomComponentRequest bomComponentRequest = new BomComponentRequest();
            bomComponentRequest.setComponentKey(localComponentKey);
            bomComponentRequest.setLicenseId(localLicenseRequest.getBasedOnLicenseId());
            bomComponentRequest.setUsageLevel(UsageLevel.COMPONENT);

            try {
                bomApi.addBomComponent(projectId, bomComponentRequest, BomRefreshMode.SYNCHRONOUS);
            } catch (SdkFault e) {
                System.err.println("addBomComponent() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            System.err.println("SampleCreateLocalComponentAndAddToBom failed");
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
