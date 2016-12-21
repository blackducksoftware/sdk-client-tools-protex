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

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ComponentVersionPreference;
import com.blackducksoftware.sdk.protex.common.PrimaryMatchOperation;
import com.blackducksoftware.sdk.protex.common.RapidIdConfigurationRequest;
import com.blackducksoftware.sdk.protex.common.SingleComponentCodeMatchOperation;
import com.blackducksoftware.sdk.protex.policy.PolicyApi;

public class SampleCreateGlobalRapidIdConfiguration extends BDProtexSample {

    private static PolicyApi policyApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCreateGlobalRapidIdConfiguration.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<configuration name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("configuration name", "The name of the configuration to create, i.e. \"My global Rapid ID Configuration\""));

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
        String configurationName = args[3];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                policyApi = myProtexServer.getPolicyApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            Long rapidIdConfigurationId = null;

            try {
                RapidIdConfigurationRequest rapidIdConfigurationRequest = new RapidIdConfigurationRequest();
                rapidIdConfigurationRequest.setName(configurationName);
                rapidIdConfigurationRequest
                .setDescription("Some Rapid ID configuration Example with PrimaryMatch operation and Single Component Code Match Operation");
                // order matters here and only one of each kind of operations is allowed
                PrimaryMatchOperation primaryMatchOperationRequest = new PrimaryMatchOperation();
                rapidIdConfigurationRequest.getOperations().add(primaryMatchOperationRequest);
                SingleComponentCodeMatchOperation singleComponentCodeMatchOperation = new SingleComponentCodeMatchOperation();
                singleComponentCodeMatchOperation
                .setComponentVersionPreference(ComponentVersionPreference.LATEST_RELEASE);
                singleComponentCodeMatchOperation.setMinimumMatchPercentage(85);
                rapidIdConfigurationRequest.getOperations().add(singleComponentCodeMatchOperation);

                rapidIdConfigurationId = policyApi.createRapidIdConfiguration(rapidIdConfigurationRequest);
                System.out.println("Created RapidIdConfiguration '" + configurationName + "' with ID: " + rapidIdConfigurationId);
            } catch (SdkFault e) {
                System.err.println("createRapidIdConfiguration failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.err.println("SampleCreateGlobalRapidIdConfiguration failed");
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
