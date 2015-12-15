package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.component.ComponentRequest;
import com.blackducksoftware.sdk.protex.license.LicenseInfo;

/**
 * This sample demonstrates how to modify a standard component, overwriting some attributes for this server.
 * 
 * It demonstrates:
 * - How to modify a standard component within the scope of the Protex instance
 */
public class SampleModifyStandardComponentLocally extends BDProtexSample {

    private static ComponentApi componentApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleModifyStandardComponentLocally.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<component ID>");
        parameters.add("<license ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("component ID", "The component ID of the component to modify , for example 'nist_419784'"));
        paramDescriptions.add(formatUsageDetail("license ID", "The license ID to set for this component , for example 'acme_proprietary'"));

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
        String componentId = args[3];
        String licenseId = args[4];

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

            ComponentKey componentKey = new ComponentKey();
            componentKey.setComponentId(componentId);

            Component standardComponent = null;

            try {
                standardComponent = componentApi.getComponentByKey(componentKey);
            } catch (SdkFault e) {
                System.err.println("getStandardComponentById() failed: " + e.getMessage());
                System.exit(-1);
            }

            if (standardComponent == null) {
                System.err.println("getStandardComponentById() returned unexpected value " + standardComponent);
                throw new RuntimeException("Component with ID '" + componentId + "' not found");
            }

            ComponentRequest scModificationRequest = new ComponentRequest();

            // add the new license as the primary one (making it the declared license)
            scModificationRequest.setPrimaryLicenseId(licenseId);
            scModificationRequest.getLicenseIds().add(licenseId);

            // copy all existing licenses
            for (LicenseInfo info : standardComponent.getLicenses()) {
                scModificationRequest.getLicenseIds().add(info.getLicenseId());
            }

            try {
                componentApi.updateComponent(componentKey, scModificationRequest);
            } catch (SdkFault e) {
                System.err.println("modifyStandardComponentLocally() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("StandardComponent '" + standardComponent.getComponentName()
                    + "' successfully modified declared license.");
        } catch (Exception e) {
            System.err.println("SampleModifyStandardComponentLocally failed");
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
