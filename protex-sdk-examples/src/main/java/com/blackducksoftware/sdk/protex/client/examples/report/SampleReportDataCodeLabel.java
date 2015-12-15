package com.blackducksoftware.sdk.protex.client.examples.report;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomShippingCodeInfo;

/**
 * This sample retrieves the relevant information for the Code Label report of a given project
 * 
 * It demonstrates:
 * - How to retrieve the ShippingCodeInfo from a rpoject
 * 
 */
public class SampleReportDataCodeLabel extends BDProtexSample {

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleReportDataCodeLabel.class.getSimpleName();

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

            BomShippingCodeInfo shippingCodeInfo = null;

            try {
                shippingCodeInfo = bomApi.getBomShippingCodeInfo(projectId);
            } catch (SdkFault e) {
                System.err.println("getBomShippingCodeInfo() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("-- Raw data for Code Lable - Shipping Code Info --");
            System.out.println("Total Open Source Bytes: " + shippingCodeInfo.getOpenSourceComponentBytes());
            System.out.println("Owned Open Source Bytes that is your code: "
                    + shippingCodeInfo.getOwnedOpenSourceBytes());
            System.out.println("Pending Identification Bytes: " + shippingCodeInfo.getPendingIdentificationBytes());
            System.out.println("Permissive Open Source Component Bytes: "
                    + shippingCodeInfo.getPermissiveOpenSourceComponentsBytes());
            System.out.println("Proprietary Bytes: " + shippingCodeInfo.getProprietaryBytes());
            System.out.println("Proprietary Owned Bytes: " + shippingCodeInfo.getProprietaryOwnedBytes());
            System.out.println("Proprietary bytes from Third Parties: "
                    + shippingCodeInfo.getProprietaryThirdPartyBytes());
            System.out.println("Total Reciprocal Bytes: " + shippingCodeInfo.getReciprocalOpenSourceComponentBytes());
            System.out.println("Reciprocal Open Source File Bytes: "
                    + shippingCodeInfo.getReciprocalOpenSourceFileBytes());
            System.out.println("Total Bytes: " + shippingCodeInfo.getTotalBytes());
            System.out.println("###");
            // Calculating MBs and percentages and formatting has not been implemented as part of this Sample
        } catch (Exception e) {
            System.err.println("SampleReportDataCodeLabel failed");
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
