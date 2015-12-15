package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.license.LicenseApi;

/**
 * This sample creates a global license
 * 
 * It requires:
 * - A custom license on the system which it is ok to delete
 * 
 * It demonstrates:
 * - How to delete a global license
 */
public class SampleDeleteLicenseById extends BDProtexSample {

    private static LicenseApi licenseApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleDeleteLicenseById.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<license ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("license ID", "ID of the license to delete i.e. \"c_mysamplelicense\""));

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
        String licenseId = args[3];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                licenseApi = myProtexServer.getLicenseApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            try {
                licenseApi.deleteLicense(licenseId);
                System.out.println("Deleted LicenseId: " + licenseId);
            } catch (SdkFault e) {
                System.err.println("deleteLicense() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.err.println("SampleDeleteLicenseById failed");
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
