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
import com.blackducksoftware.sdk.protex.license.GlobalLicenseRequest;
import com.blackducksoftware.sdk.protex.license.LicenseApi;
import com.blackducksoftware.sdk.protex.license.LicenseApprovalState;
import com.blackducksoftware.sdk.protex.license.LicenseAttributes;
import com.blackducksoftware.sdk.protex.license.LicenseExtensionLevel;
import com.blackducksoftware.sdk.protex.license.PermittedOrRequired;
import com.blackducksoftware.sdk.protex.license.RestrictionType;
import com.blackducksoftware.sdk.protex.license.RightToDistributeBinaryForMaximumUsage;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;

/**
 * This sample demonstrates how create a global license, create project and a assign the new global license
 * 
 * It demonstrates:
 * - How to create a global license
 * - How to create a project
 * - How to assign a specific license to a project
 */
public class SampleCreateProjectWithNewGlobalLicense extends BDProtexSample {

    private static LicenseApi licenseApi = null;

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCreateProjectWithNewGlobalLicense.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project name>");
        parameters.add("<license name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project name", "Name for the Project you are creating i.e. \"My Test Project\""));
        paramDescriptions.add(formatUsageDetail("license name", "Name for the license you are creating i.e. \"My License\""));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 5) {
            System.err.println("\n\nNot enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectName = args[3];
        String licenseName = args[4];

        Long connectionTimeout = 120 * 1000L;
        ProtexServerProxy myProtexServer = null;

        String licenseId = null;
        String projectId = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                projectApi = myProtexServer.getProjectApi();
                licenseApi = myProtexServer.getLicenseApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            try {
                // creating License
                GlobalLicenseRequest request = createLicenseRequest(licenseName);
                licenseId = licenseApi.createLicense(request);
                System.out.println("Created licenseId: " + licenseId);
            } catch (SdkFault e) {
                System.err.println("failure in createLicense(): " + e.getMessage());
                throw new RuntimeException(e);
            }

            try {
                // creating Project and associating License with Project
                ProjectRequest projectRequest = new ProjectRequest();
                projectRequest.setName(projectName);
                projectRequest.setDescription("My test project");
                // Set the declared license
                projectRequest.setLicenseId(licenseId);

                projectId = projectApi.createProject(projectRequest, null);
            } catch (SdkFault e) {
                System.err.println("failure in createProject(): " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("New project '" + projectName + "' with project ID '" + projectId + "'");
        } catch (Exception e) {
            System.err.println("SampleCreateProjectWithNewGlobalLicense failed");
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

    public static GlobalLicenseRequest createLicenseRequest(String licenseName) {
        GlobalLicenseRequest licenseRequest = new GlobalLicenseRequest();

        licenseRequest.setName(licenseName);
        licenseRequest.setComment("My license comments");
        licenseRequest.setExplanation("My license Explanation");
        licenseRequest.setText("Tester's License Self destruct if test fails".getBytes());
        licenseRequest.setApprovalState(LicenseApprovalState.APPROVED);

        LicenseAttributes attributes = new LicenseAttributes();

        attributes.setAntiDrmProvision(Boolean.TRUE);
        attributes.setCarriesDistributionObligations(Boolean.TRUE);
        attributes.setChangeNoticeRequired(Boolean.TRUE);
        attributes.setChargingFees(PermittedOrRequired.REQUIRED);
        attributes.setDiscriminatoryRestrictions(RestrictionType.HAS_NO_RESTRICTIONS);
        attributes.setExpressPatentLicense(Boolean.TRUE);
        attributes.setGrantRecipientRightToCopy(PermittedOrRequired.PERMITTED);
        attributes.setGrantRecipientRightToModify(PermittedOrRequired.REQUIRED);
        attributes.setGrantRecipientRightToReverseEngineer(PermittedOrRequired.REQUIRED);
        attributes.setIncludeLicense(Boolean.TRUE);
        attributes.setIndemnificationRequired(Boolean.FALSE);
        attributes.setIntegrationLevelForLicenseApplication(LicenseExtensionLevel.FILE_PER_MPL);
        attributes.setLicenseBackRequired(Boolean.FALSE);
        attributes.setLimitationOfLiabilityRequired(Boolean.TRUE);
        attributes.setNotice(Boolean.FALSE);
        attributes.setPatentRetaliation(Boolean.TRUE);
        attributes.setPromotionRestriction(Boolean.TRUE);
        attributes.setRightToDistributeBinaryForMaximumUsage(RightToDistributeBinaryForMaximumUsage.NON_COMMERCIAL_OR_PERSONAL_USE);
        attributes.setShareAlikeReciprocity(Boolean.TRUE);
        attributes.setSourceCodeDistribution(PermittedOrRequired.REQUIRED);
        attributes.setWarrantyDisclaimerRequired(Boolean.TRUE);

        licenseRequest.setAttributes(attributes);

        return licenseRequest;
    }

}
