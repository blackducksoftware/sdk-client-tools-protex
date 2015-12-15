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
package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * Black Duck Protex SDK Sample program
 */
public abstract class BDProtexSample {

    /**
     * Get the usage parameters for printing a usage command line as they are processed by this class
     *
     * @return the String to use in a usage command line
     */
    @Deprecated
    public static String getUsageServiceParameters() {
        return "<serverUri> <username> <password>";
    }

    /**
     * Print the detailed description of usage for the parameters processed by this class
     */
    @Deprecated
    public static void printUsageServiceParameterDetails() {
        System.out.println("  serverUri - the base Uri for the server, i.e. http://protex.example.com:<port>/");
        System.out.println("  username  - the username for this server, i.e. tester@example.com");
        System.out.println("  password  - the passowrd for this user, i.e. simplepassword");
    }

    /**
     * @return An ordered list of parameters accepted/required by all samples
     */
    protected static List<String> getDefaultUsageParameters() {
        List<String> defaultParams = new ArrayList<String>();

        defaultParams.add("<serverUri>");
        defaultParams.add("<username>");
        defaultParams.add("<password>");

        return defaultParams;
    }

    /**
     * @return A list of detailed descriptions for command line parameters accepted/required by all samples
     */
    protected static List<String> getDefaultUsageParameterDetails() {
        List<String> defaultParamDetails = new ArrayList<String>();

        defaultParamDetails.add(formatUsageDetail("serverUri", "The base Uri for the server, i.e. http://protex.example.com:<port>/"));
        defaultParamDetails.add(formatUsageDetail("username", "The username for this server, i.e. tester@example.com"));
        defaultParamDetails.add(formatUsageDetail("password", "The password for this user, i.e. simplepassword"));

        return defaultParamDetails;
    }

    /**
     * @param parameter
     *            A command line parameter accepted/required by the sample
     * @param description
     *            A description of the parameter
     * @return A full output string describing the parameter
     */
    protected static String formatUsageDetail(String parameter, String description) {
        return "\t" + parameter + " - " + description;
    }

    /**
     * Outputs usage information for the sample
     *
     * @param className
     *            The name of the class
     * @param parameters
     *            A full list of parameters accepted/required by the sample
     * @param parameterDescriptions
     *            A full list of descriptions for parameters accepted/required by the sample
     */
    protected static void outputUsageDetails(String className, List<String> parameters, List<String> parameterDescriptions) {
        StringBuilder parameterString = new StringBuilder(className);

        for (String parameter : parameters) {
            parameterString.append(" ");

            parameterString.append(parameter);
        }

        System.out.println(parameterString);

        for (String description : parameterDescriptions) {
            System.out.println(description);
        }
    }
}
