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

import com.blackducksoftware.sdk.fault.ErrorCode;
import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.client.util.ServerAuthenticationException;
import com.blackducksoftware.sdk.protex.user.UserApi;

/**
 * This sample demonstrates how validate a user's authentication credentials (most effectively)
 * 
 * Remember, Protex SDK Web Services are WS-I Basic profile compliant and therefore stateless. That means every request
 * sends the authentication (as opposed to login/ do something (and maintain state to the login) / logout
 * 
 * However in some situations you want to know if a username/password or other user supplied authentication is valid.
 * 
 * It demonstrates:
 * - How to validate username/password against a Protex server.
 */
public class SampleValidateUserCredentials extends BDProtexSample {

    private static UserApi userApi;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleValidateUserCredentials.class.getSimpleName();
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


            try {
                userApi.getCurrentUserHasServerFileAccess();
                System.out.println("Username/Password is valid");
            } catch (SdkFault e) {
                if (ErrorCode.INSUFFICIENT_PERMISSION.equals(e.getFaultInfo().getErrorCode())) {
                    System.err.println("Invalid credentials for server");
                } else {
                    System.err.println("getRoleByName() failed: " + e.getMessage());
                }

                throw new RuntimeException(e);
            }

            // Another way to do this if using the server proxy
            try {
                myProtexServer.validateCredentials();
            } catch (ServerAuthenticationException e) {
                System.err.println("Invalid credentials for server");
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.err.println("SampleValidateUserCredentials failed");
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
