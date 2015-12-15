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
import com.blackducksoftware.sdk.protex.user.User;
import com.blackducksoftware.sdk.protex.user.UserApi;
import com.blackducksoftware.sdk.protex.user.UserColumn;
import com.blackducksoftware.sdk.protex.user.UserPageFilter;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how to list users in the system
 * 
 * It demonstrates:
 * - How to get a list of users
 */
public class SampleListUsers extends BDProtexSample {

    private static UserApi userApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleListUsers.class.getSimpleName();

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
            // get service and service port
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                userApi = myProtexServer.getUserApi();
            } catch (RuntimeException e) {
                System.err.println("\nConnection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<User> users = null;
            System.out.println();

            try {
                UserPageFilter pageFilter = PageFilterFactory.getAllRows(UserColumn.USER_ID);
                users = userApi.getUsers(pageFilter);
            } catch (SdkFault e) {
                System.err.println("getUsers() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (users == null || users.size() == 0) {
                System.out.println("No Users returned");
            } else {
                for (User user : users) {
                    System.out.println("User: " + user.getLastName() + ", " + user.getFirstName() + " ("
                            + user.getEmail() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("SampleListUsers failed");
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
