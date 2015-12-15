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

import java.util.Date;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.user.UserApi;

/**
 * This sample creates a user
 * 
 * It demonstrates:
 * - How to create a user
 */
public class SampleGetLastUserSignIn extends BDProtexSample {

    private static UserApi userApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetLastUserSignIn.class.getSimpleName();

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

                userApi = myProtexServer.getUserApi();
            } catch (RuntimeException e) {
                System.err.println("\nConnection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Gets the last sign in time of the current user
            Date lastSignIn = null;

            try {
                lastSignIn = userApi.getLastSignIn(username);
            } catch (SdkFault e) {
                System.err.println("getLastSignIn() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("\nuser last signed in: " + lastSignIn);
        } catch (Exception e) {
            System.err.println("SampleCreateUser failed");
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
