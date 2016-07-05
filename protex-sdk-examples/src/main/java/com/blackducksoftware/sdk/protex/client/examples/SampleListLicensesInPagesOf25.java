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
 * This sample demonstrates how to list licenses in pages of 25 at a time
 * 
 * It demonstrates:
 * - How to get a list of global (standard) licenses
 * - How to use the page filter to get the licenses in pages of 25 licenses
 */
public class SampleListLicensesInPagesOf25 extends BDProtexSample {

    private static LicenseApi licenseApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleListLicensesInPagesOf25.class.getSimpleName();

        outputUsageDetails(className, getDefaultUsageParameters(), getDefaultUsageParameterDetails());
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 3) {
            System.err.println("\nNot enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                licenseApi = myProtexServer.getLicenseApi();
            } catch (RuntimeException e) {
                System.err.println("\nConnection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            int pageSize = 25;
            List<LicenseInfo> licenses = null;
            List<LicenseOriginType> typeFilter = new ArrayList<LicenseOriginType>();
            typeFilter.add(LicenseOriginType.STANDARD);
            LicenseInfoPageFilter pageFilter = PageFilterFactory.getFirstPage(pageSize, LicenseInfoColumn.LICENSE_NAME, true);

            do {
                try {
                    licenses = licenseApi.getLicenses(typeFilter, pageFilter);
                } catch (SdkFault e) {
                    System.err.println("\ngetLicenses failed: " + e.getMessage());
                    throw new RuntimeException(e);
                }

                System.out.println();
                if (licenses == null || licenses.size() == 0) {
                    System.out.println("No Licenses returned");
                } else {
                    System.out.println("=== Page " + pageFilter.getLastRowIndex() / pageSize);
                    for (LicenseInfo license : licenses) {
                        System.out.println("License: " + license.getName() + " (" + license.getLicenseId() + ")");
                    }
                }
                pageFilter = PageFilterFactory.getNextPage(pageFilter);
            } while ((licenses != null) && (licenses.size() >= pageSize - 1));
        } catch (Exception e) {
            System.err.println("SampleListLicensesInPagesOf25 failed");
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
