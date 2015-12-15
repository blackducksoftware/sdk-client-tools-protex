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
import com.blackducksoftware.sdk.protex.user.UserApi;
import com.blackducksoftware.sdk.protex.user.UserRequest;

/**
 * This sample creates a user
 * 
 * It demonstrates:
 * - How to create a user
 */
public class SampleCreateUser extends BDProtexSample {

    private static UserApi userApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCreateUser.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<email>");
        parameters.add("<first name>");
        parameters.add("<last name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("email", "A unique e-mail address for the user, i.e. \"test@example.com\""));
        paramDescriptions.add(formatUsageDetail("first name", "The first name of the user, i.e. \"Charles\""));
        paramDescriptions.add(formatUsageDetail("last name", "The last name of the user, i.e. \"Miller\""));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 6) {
            System.err.println("\nNot enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String email = args[3];
        String firstName = args[4];
        String lastName = args[5];

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

            UserRequest userRequest = new UserRequest();
            userRequest.setEmail(email);
            userRequest.setFirstName(firstName);
            userRequest.setLastName(lastName);

            String userId = null;

            // Creates a user with the same password as the authenticated user
            try {
                userId = userApi.createUser(userRequest, password);
            } catch (SdkFault e) {
                System.err.println("createUser() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("\n New User created with ID = " + userId);
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
