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

import java.util.List;

import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.RapidIdConfiguration;
import com.blackducksoftware.sdk.protex.common.RapidIdOperation;
import com.blackducksoftware.sdk.protex.common.SingleComponentCodeMatchOperation;
import com.blackducksoftware.sdk.protex.policy.PolicyApi;

public class SampleGetGlobalRapidIdConfigurations extends BDProtexSample {

    private static PolicyApi policyApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetGlobalRapidIdConfigurations.class.getSimpleName();
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

                policyApi = myProtexServer.getPolicyApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            List<RapidIdConfiguration> rapidIdConfigurations = policyApi.getRapidIdConfigurations();

            for (RapidIdConfiguration c : rapidIdConfigurations) {
                System.out.println("--- " + c.getName() + " (" + c.getConfigurationId() + ")");
                System.out.println("  Description: " + c.getDescription());
                System.out.println("  Origin: " + c.getOriginType());
                for (RapidIdOperation o : c.getOperations()) {
                    if (o != null) {
                        switch (o.getType()) {
                        case LEARNED_ID:
                            System.out.println("  Learned identification");
                            break;
                        case PRIMARY_MATCH:
                            System.out.println("  Primary match based on MD5 sum identity");
                            break;
                        case SINGLE_COMPONENT_CODE_MATCH:
                            SingleComponentCodeMatchOperation sccmo = (SingleComponentCodeMatchOperation) o;
                            System.out.println("  Single Component Code Match with > " + sccmo.getMinimumMatchPercentage()
                                    + "%");
                            switch (sccmo.getComponentVersionPreference()) {
                            case LATEST_RELEASE:
                                System.out.println("  >> Prefer Latests Release");
                                break;
                            case EARLIEST_RELEASE:
                                System.out.println("  >> Prefer Earliest Release");
                                break;
                            case HIGHEST_COVERAGE:
                                System.out.println("  >> Prefer Version with highest match percentage");
                                break;

                            default:
                                System.out.println("  *** Version preference '" + sccmo.getComponentVersionPreference()
                                        + "' unknown ***");
                                break;
                            }
                            System.out.println("  Single Component Code Match with > " + sccmo.getMinimumMatchPercentage()
                                    + "%");
                            break;
                        default:
                            System.out.println("  *** operation type '" + o.getType() + "' unknown ***");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("SampleGetGlobalRapidIdConfigurations failed");
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
