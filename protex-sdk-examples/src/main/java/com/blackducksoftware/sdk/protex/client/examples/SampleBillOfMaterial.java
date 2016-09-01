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
import com.blackducksoftware.sdk.protex.common.ApprovalState;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;

/**
 * This sample retrieves the bill of material of a given project
 * 
 * It requires:
 * - An analyzed project which already exists on the server. It is recommended that the project have multiple components
 * on the BOM
 * 
 * It demonstrates:
 * - How to retrieve a project by its ID
 * - How to retrieve the bill of material (BOM) from the project
 * - How to retrieve additional information of a component listed in the BOM
 */
public class SampleBillOfMaterial extends BDProtexSample {

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleBillOfMaterial.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID for the project to be created and analyzed, i.e. c_test-project"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 4) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        Long connectionTimeout = 120 * 1000L;

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectId = args[3];

        ProtexServerProxy myProtexServer = null;

        try {
            // get service and service port
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                bomApi = myProtexServer.getBomApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            List<BomComponent> bomComponents = null;

            try {
                bomComponents = bomApi.getBomComponents(projectId);
            } catch (SdkFault e) {
                System.err.println("BomApi.getBomComponents() failed");
                throw new RuntimeException(e);
            }

            System.out.println("-- Bill Of Materials --");

            // Generate output which describes several aspects of the component's use within the project
            for (BomComponent bomComponent : bomComponents) {
                StringBuilder componentOutput = new StringBuilder();

                componentOutput.append("Component: ").append(bomComponent.getComponentName());

                if (bomComponent.getVersionName() != null) {
                    componentOutput.append("    Version: ").append(bomComponent.getVersionName());
                }

                componentOutput.append("    Type: ").append(bomComponent.getComponentType());

                componentOutput.append("    Usage(s): ");

                StringBuilder usages = new StringBuilder();
                for (UsageLevel ul : bomComponent.getUsageLevels()) {
                    if (usages.length() > 0) {
                        usages.append(",");
                    }

                    usages.append(ul);
                }

                componentOutput.append(usages.toString());
                componentOutput.append("    License: ").append(bomComponent.getLicenseInfo().getName());
                componentOutput.append("    Approval Status: ").append(bomComponent.getApprovalState());

                if (!ApprovalState.NOT_REVIEWED.equals(bomComponent.getApprovalState())) {
                    componentOutput.append(" by ").append(bomComponent.getApprovedBy())
                            .append(" on ").append(bomComponent.getApprovalDate());
                }

                componentOutput.append("    License Conflict: ");

                if (bomComponent.isHasDeclaredLicenseConflict()) {
                    componentOutput.append(" Declared Conflict");
                } else if (bomComponent.isHasDeclaredLicenseConflict()) {
                    componentOutput.append(" Component Conflict");
                } else {
                    componentOutput.append(" None");
                }

                try {
                    String componentComment = bomApi.getComponentComment(projectId, bomComponent.getComponentKey());

                    if (componentComment != null) {
                        componentOutput.append("    Comment: ").append(componentComment);
                    }
                } catch (SdkFault e) {
                    System.err.println("BomApi.getComponentComment() failed");
                    throw new RuntimeException(e);
                }

                componentOutput.append("    # Search: ").append(bomComponent.getFileCounts().getStringSearches());

                System.out.println(componentOutput.toString());
            }

            System.out.println("###");
        } catch (Exception e) {
            System.err.println("SampleBillOfMaterial failed");
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
