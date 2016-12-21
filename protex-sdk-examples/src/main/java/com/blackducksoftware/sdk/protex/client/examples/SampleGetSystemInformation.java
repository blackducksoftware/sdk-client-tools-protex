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

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.policy.PolicyApi;
import com.blackducksoftware.sdk.protex.policy.ProtexSystemInformation;

public class SampleGetSystemInformation extends BDProtexSample {

    private static PolicyApi policyApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetSystemInformation.class.getSimpleName();
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

            // Get global code label option (before updating)
            ProtexSystemInformation systemInformation = null;

            try {
                systemInformation = policyApi.getSystemInformation();
            } catch (SdkFault e) {
                System.err.println("getSystemInformation failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // showing system information
            System.out.println("BDS Server Library Version: " + systemInformation.getBdsServerLibraryVersion());

            System.out.println("BDS Database version: " + systemInformation.getBdsBasicDatabaseVersion());
            System.out.println("BDS Knowledgebase Update Level: " + systemInformation.getBdsKnowledgebaseUpdateLevel());
            System.out.println("BDS Knowledgebase Update Stream: " + systemInformation.getBdsKnowledgebaseUpdateStream());
            System.out.println("BDS Proxy Protocol Version: " + systemInformation.getBdsProxyProtcolVersion());
            System.out.println("BDS Proxy Version: " + systemInformation.getBdsProxyVersion());
            System.out.println("BDS Local Registration Service Version: "
                    + systemInformation.getLocalRegistrationServiceVersion());
            System.out.println("BDS Registration Service Version: " + systemInformation.getRegistrationServiceVersion());
            System.out.println("BDS Service Version: " + systemInformation.getServiceVersion());
            System.out.println("BDS Update Service Version: " + systemInformation.getUpdateServiceVersion());

            System.out.println("BDS Custom Database Version: " + systemInformation.getBdsCustomDatabaseVersion());
            System.out.println("BDS Fingerprint Database Version: "
                    + systemInformation.getFingerprintBasicDatabaseVersion());
            System.out.println("BDS Custom Fingerprint Database Version: "
                    + systemInformation.getFingerprintCustomDatabaseVersion());

            System.out
            .println("BDS Raw Fingerprint Files Version: " + systemInformation.getBdsRawFingerprintFilesVersion());

            System.out.println("BDS SAOP Protocol Version: " + systemInformation.getBdsSoapProtcolVersion());
            System.out.println("BDS SOAP Version: " + systemInformation.getBdsSoapVersion());
            System.out.println("BDS SOAP Service Version: " + systemInformation.getSoapServiceVersion());

            System.out.println("BDS Client Protocol Version: " + systemInformation.getBdsClientProtcolVersion());
            System.out.println("BDS Client Version: " + systemInformation.getBdsClientVersion());
            System.out.println("BDS Estimation Tool Protocol Version: "
                    + systemInformation.getBdsEstimationToolProtocolVersion());
            System.out.println("BDS Estimation Tool Version: " + systemInformation.getBdsEstimationToolVersion());
            System.out.println("BDS Tool Protocol Version: " + systemInformation.getBdsToolProtcolVersion());
            System.out.println("BDS Tool Version: " + systemInformation.getBdsToolVersion());
            System.out.println("BDS Update Protocol Version: " + systemInformation.getBdsUpdateProtcolVersion());
            System.out.println("BDS Update Version: " + systemInformation.getBdsUpdateVersion());
        } catch (Exception e) {
            System.err.println("SampleGetSystemInformation failed");
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
