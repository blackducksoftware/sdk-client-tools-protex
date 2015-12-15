package com.blackducksoftware.sdk.protex.client.examples.report;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomLicenseInfo;

/**
 * This sample gathers the data to generate the report section "Licenses in Effect"
 * 
 * It demonstrates:
 * - How to get the license info for a Bill of Material (BOM)
 * - How to retrieve a license by ID (including the license text)
 */
public class SampleReportDataLicensesInEffect extends BDProtexSample {

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleReportDataLicensesInEffect.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID of the project to get data for"));

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

                bomApi = myProtexServer.getBomApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            List<BomLicenseInfo> bomLicenseInfos = null;

            try {
                bomLicenseInfos = bomApi.getBomLicenseInfo(projectId);
            } catch (SdkFault e) {
                System.err.println("getBomLicenseInfo() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (bomLicenseInfos != null && !bomLicenseInfos.isEmpty()) {
                // The SDK does not support custom fields at this time
                System.out.println("License Name       \tShip Status\tCustom Fields");
                for (BomLicenseInfo licenseInfo : bomLicenseInfos) {
                    System.out.println(licenseInfo.getName() + "\t" + licenseInfo.getShippingStatus());
                }
            }
        } catch (Exception e) {
            System.err.println("SampleReportDataLicensesInEffect failed");
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
