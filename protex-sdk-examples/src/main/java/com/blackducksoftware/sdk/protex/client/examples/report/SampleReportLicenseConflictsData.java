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
package com.blackducksoftware.sdk.protex.client.examples.report;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;
import com.blackducksoftware.sdk.protex.report.LicenseConflictReport;

public class SampleReportLicenseConflictsData extends BDProtexSample {

    private static ProjectApi projectApi = null;

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleReportLicenseConflictsData.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID of the project to get data for"));

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
        String projectId = args[3];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                projectApi = myProtexServer.getProjectApi();
                bomApi = myProtexServer.getBomApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            final String rowFormat = "%1$-30s | %2$-20s | %3$-26s | %4$-30s | %5$-20s | %6$-26s | %7$-30s | %8$-30s";
            System.out.println("");
            System.out.println(String.format(rowFormat, "Component", "Version",
                    "License", "Conflicting Component", "Conflicting Version", "Conflicting License",
                    "Component Obligation", "Conflicting Obligation"));

            List<BomComponent> bomComponents = null;

            try {
                bomComponents = bomApi.getBomComponents(projectId);
            } catch (SdkFault e) {
                System.err.println("getBomComponents() failed: " + e.getMessage());
                System.exit(-1);
            }

            boolean foundAny = false;

            for (BomComponent bomComponent : bomComponents) {
                try {
                    // get violating attributes with declared project license

                    List<LicenseConflictReport> licenseViolations = bomApi.getBomComponentLicenseConflictData(projectId, bomComponent.getComponentKey());

                    if (licenseViolations != null && !licenseViolations.isEmpty()) {

                        for (LicenseConflictReport lViolation : licenseViolations) {
                            foundAny = true;

                            reportViolatingAttribute(rowFormat, lViolation.getComponent().getBomComponentName(), lViolation.getComponent().getBomVersionName(),
                                    lViolation.getComponent().getLicenseInfo().getName(),
                                    lViolation.getConflictingComponent().getBomComponentName(), lViolation.getConflictingComponent().getBomVersionName(),
                                    lViolation.getConflictingComponent().getLicenseInfo().getName(), lViolation.getComponent().getObligationBean(),
                                    lViolation.getConflictingComponent().getObligationBean());
                        }
                    }
                } catch (SdkFault e) {
                    System.err.println("getBomComponentLicenseViolatingAttributes() failed: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            if (!foundAny) {
                System.out.println("No license Conflicts found for this project");
            }
        } catch (Exception e) {
            System.err.println("SampleReportLicenseConflictsData failed");
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

    private static void reportViolatingAttribute(String rowFormat, String componentId, String versionId,
            String licenseName, String violatingComponentId, String violatingVersionId, String violatingLicenseName,
            com.blackducksoftware.sdk.protex.project.ObligationBean attribute,
            com.blackducksoftware.sdk.protex.project.ObligationBean violatingAttributes) {

        System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                violatingVersionId, violatingLicenseName, attribute.getLabel(),
                violatingAttributes.getConflictingLabel()));

    }
}
