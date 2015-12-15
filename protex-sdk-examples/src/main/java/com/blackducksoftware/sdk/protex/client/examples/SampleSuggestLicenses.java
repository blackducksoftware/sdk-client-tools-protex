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

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.license.LicenseApi;
import com.blackducksoftware.sdk.protex.license.LicenseInfo;
import com.blackducksoftware.sdk.protex.license.LicenseInfoColumn;
import com.blackducksoftware.sdk.protex.license.LicenseInfoPageFilter;
import com.blackducksoftware.sdk.protex.license.LicenseOriginType;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how to suggest licenses with a pattern
 * 
 * It demonstrates:
 * - How to suggest licenses
 */
public class SampleSuggestLicenses extends BDProtexSample {

    private static LicenseApi licenseApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleSuggestLicenses.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<any word starting with>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("any word starting with", "A few initial letters of pattern you want to search or suggest , i.e. \"ja\""));

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
        String searchPattern = args[3];

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

            List<LicenseOriginType> licenseTypes = new ArrayList<LicenseOriginType>();
            for (LicenseOriginType t : LicenseOriginType.values()) {
                if (t != LicenseOriginType.PROJECT_LOCAL) {
                    licenseTypes.add(t);
                }
            }
            LicenseInfoPageFilter pageFilter = PageFilterFactory.getAllRows(LicenseInfoColumn.LICENSE_NAME);

            List<LicenseInfo> licenses = null;

            try {
                licenses = licenseApi.suggestLicenses(searchPattern, licenseTypes, pageFilter);
            } catch (SdkFault e) {
                System.err.println("suggestLicenses() failed: " + e.getMessage());
                System.exit(-1);
            }

            if (licenses == null || licenses.isEmpty()) {
                System.out.println("No licenses returned");
            } else {
                for (LicenseInfo li : licenses) {
                    System.out.println("" + li.getName() + "  (" + li.getLicenseId() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("SampleSuggestLicenses failed");
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
