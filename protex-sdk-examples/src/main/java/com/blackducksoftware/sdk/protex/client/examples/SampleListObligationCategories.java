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

import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.obligation.ObligationApi;
import com.blackducksoftware.sdk.protex.obligation.ObligationCategory;

/**
 * This sample demonstrates how to list obligation categories
 * 
 * It demonstrates:
 * - How to get a list of all the obligation categories on a system
 * 
 * In a freshly installed (and most other) Protex systems there are only two categories
 */
public class SampleListObligationCategories extends BDProtexSample {

    private static ObligationApi obligationApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleListObligationCategories.class.getSimpleName();

        outputUsageDetails(className, getDefaultUsageParameters(), getDefaultUsageParameterDetails());
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 3) {
            System.err.println("Not enough parameters!");
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

                obligationApi = myProtexServer.getObligationApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<ObligationCategory> categories = null;

            try {
                categories = obligationApi.suggestObligationCategories("");
            } catch (SdkFault e) {
                System.err.println("suggestObligationCategories() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (categories == null || categories.size() == 0) {
                System.out.println("No obligation categories returned");
            } else {
                for (ObligationCategory category : categories) {
                    System.out.println("obligation category: " + category.getName() + " ("
                            + category.getObligationCategoryId() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("SampleCreateProject failed");
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
