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

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.ErrorCode;
import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.license.LicenseApi;
import com.blackducksoftware.sdk.protex.obligation.AssignedObligation;
import com.blackducksoftware.sdk.protex.obligation.ObligationApi;
import com.blackducksoftware.sdk.protex.obligation.ObligationCategory;

/**
 * This sample demonstrates how to list all obligations for a global license.
 * 
 * It demonstrates:
 * - How to get all obligations for a given license ID
 */
public class SampleListLicenseObligations extends BDProtexSample {

    private static LicenseApi licenseApi = null;

    private static ObligationApi obligationApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleListLicenseObligations.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<license IDs>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("license IDs",
                "A comma-separated list of license IDs to get obligation data for"));

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
        String csvLicenseIds = args[3];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                licenseApi = myProtexServer.getLicenseApi();
                obligationApi = myProtexServer.getObligationApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            for (String licenseId : csvLicenseIds.split(",")) {
                listLicenseObligations(licenseId.trim());
            }
        } catch (Exception e) {
            System.err.println("SampleListLicenseObligations failed");
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

    /**
     * Lists the license obligations for a specific license
     * 
     * @param licenseId
     *            The ID of the license to list obligation data for
     */
    private static void listLicenseObligations(String licenseId) {
        // Call the Api
        List<AssignedObligation> obligations = null;

        try {
            obligations = licenseApi.getLicenseObligations(licenseId);
        } catch (SdkFault e) {
            if (ErrorCode.LICENSE_NOT_FOUND.equals(e.getFaultInfo().getErrorCode())) {
                System.out.println(e.getMessage());
                return;
            } else {
                System.err.println("getLicenseObligations failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        if (obligations == null || obligations.isEmpty()) {
            System.out.println("\nNo obligations assigned to license '" + licenseId + "'");
        } else {
            for (AssignedObligation obligation : obligations) {
                ObligationCategory category = null;
                try {
                    category = obligationApi.getObligationCategoryById(obligation.getObligationCategoryId());
                } catch (SdkFault e) {
                    System.err.println("getObligationCategoryById failed: " + e.getMessage());
                    throw new RuntimeException(e);
                }

                System.out.println("\n" + licenseId + ",\"" + obligation.getName() + "\",\"" + category.getName()
                        + "\",\""
                        + obligation.isFulfilled() + "\",\""
                        + (obligation.getDescription() != null ? obligation.getDescription().replace('"', '\'') : "")
                        + "\"");
            }
        }
    }
}
