/*
 * Black Duck Software Suite SDK
 * Copyright (C) 2015  Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.blackducksoftware.sdk.protex.client.examples.test.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.testng.Assert;

import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;

/**
 * General utilities around the test harness used to run SDK example tests
 */
public final class Tests {

    /**
     * TestNG group for tests which change settings on a server which could affect other operations on the server. For
     * example, a test which changes server-side scanning policies would be placed into this group
     */
    public static final String OPERATION_AFFECTING_TEST = "OPERATION_AFFECTING_TEST";

    /**
     * TestNG group for tests which require either configured source or permission to upload source for use in a scan
     */
    public static final String SOURCE_DEPENDENT_TEST = "SOURCE_DEPENDENT_TEST";

    private static final String SERVER_URL_PROPERTY = "test.urlbase";

    private static final String SERVER_USERNAME_PROPERTY = "test.username";

    private static final String SERVER_PASSWORD_PROPERTY = "test.password";

    /** Roles the test user must have for examples to behave as expected */
    private static final Collection<String> REQUIRED_TEST_USER_ROLES;

    /** Roles the test user must NOT have for examples to behave as expected */
    private static final Collection<String> DISALLOWED_TEST_USER_ROLES;

    static {
        List<String> requiredRoles = new ArrayList<String>();
        requiredRoles.add("universaladministrator");
        requiredRoles.add("attorney");
        requiredRoles.add("codeprintmanager");
        requiredRoles.add("basicdeveloper");
        requiredRoles.add("universalmanager");
        requiredRoles.add("powerdeveloper");
        requiredRoles.add("projectleader");

        List<String> disallowedRoles = new ArrayList<String>();
        disallowedRoles.add("readonly");
        disallowedRoles.add("identifier");

        REQUIRED_TEST_USER_ROLES = requiredRoles;
        DISALLOWED_TEST_USER_ROLES = disallowedRoles;
    }

    /**
     * Prevent instantiation of utility class
     */
    private Tests() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate instance of utility class '" + getClass().getName() + "'");
    }

    /**
     * @return Roles the test user must have for examples to behave as expected
     */
    public static Collection<String> getRequiredTestRoles() {
        return REQUIRED_TEST_USER_ROLES;
    }

    /**
     * @return Roles the test user must NOT have for examples to behave as expected
     */
    public static Collection<String> getDisallowedTestRoles() {
        return DISALLOWED_TEST_USER_ROLES;
    }



    /**
     * @return A server proxy configured to communicate with the server specified via system property
     */
    public static ProtexServerProxy createProxy() {
        String serverUrl = getServerUrl();
        String username = getServerUsername();
        String password = getServerPassword();

        // Validate required test arguments were provided
        Assert.assertTrue(serverUrl != null && !serverUrl.isEmpty(), "Server URL is required for tests - set '" + SERVER_URL_PROPERTY + "'");
        Assert.assertTrue(username != null && !username.isEmpty(), "Username is required for tests - set '" + SERVER_USERNAME_PROPERTY + "'");
        Assert.assertTrue(password != null && !password.isEmpty(), "Password is required for tests - set '" + SERVER_PASSWORD_PROPERTY + "'");

        return new ProtexServerProxy(serverUrl, username, password);
    }

    /**
     * @return The user name provided by the test arguments for the Protex server
     */
    public static String getServerUsername() {
        String username = System.getProperty(SERVER_USERNAME_PROPERTY);

        username = (username != null ? username.trim() : null);

        return username;
    }

    /**
     * @return The URL provided by the test arguments for the Protex server
     */
    public static String getServerUrl() {
        String serverUrl = System.getProperty(SERVER_URL_PROPERTY);

        serverUrl = (serverUrl != null ? serverUrl.trim() : null);

        return serverUrl;
    }

    /**
     * @return The password provided by the test arguments for the Protex server
     */
    public static String getServerPassword() {
        String password = System.getProperty(SERVER_PASSWORD_PROPERTY);

        password = (password != null ? password.trim() : null);

        return password;
    }

}
