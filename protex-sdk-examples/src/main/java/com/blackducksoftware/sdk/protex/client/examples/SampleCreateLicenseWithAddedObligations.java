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
import com.blackducksoftware.sdk.protex.obligation.AssignedObligationRequest;
import com.blackducksoftware.sdk.protex.obligation.ObligationCategory;

/**
 * This sample creates a global license
 * 
 * It demonstrates:
 * - How to create a global license
 * - How to add a license-scope obligation to a license
 */
public class SampleCreateLicenseWithAddedObligations extends BDProtexSample {

    private static LicenseApi licenseApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCreateLicenseWithAddedObligations.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<license name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("license name",
                "Name for the license you are creating i.e. \"My Test License\" (include the quotes, if the name contains spaces)"));

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
        String licenseName = args[3];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                licenseApi = myProtexServer.getLicenseApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Prepare the license request
            GlobalLicenseRequest licenseRequest = new GlobalLicenseRequest();
            licenseRequest.setName(licenseName);
            licenseRequest.setComment("My license comments");
            licenseRequest.setExplanation("My license Explanation");
            licenseRequest.setText("Tester's License Self-destruct if test fails".getBytes());
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

            String licenseId = null;
            try {
                licenseId = licenseApi.createLicense(licenseRequest);
            } catch (SdkFault e) {
                System.err.println("createLicense() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("Created LicenseId: " + licenseId);

            List<ObligationCategory> categories = null;

            try {
                categories = myProtexServer.getObligationApi().suggestObligationCategories("");
            } catch (SdkFault e) {
                System.err.println("suggestObligationCategories() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (categories == null || categories.isEmpty()) {
                System.out.println("Did not find any categories");
                throw new RuntimeException("No categories found - minimum of standard categories expected");
            }

            AssignedObligationRequest obligationRequest = new AssignedObligationRequest();
            obligationRequest.setDescription("This obligation was created and assigned via the SDK");
            obligationRequest.setFulfilled(false);
            obligationRequest.setName("Create License Added Obligation");
            obligationRequest.setObligationCategoryId(categories.get(0).getObligationCategoryId());
            obligationRequest.setReviewAndReport(true);

            try {
                myProtexServer.getLicenseApi().addLicenseObligation(licenseId, obligationRequest);
            } catch (SdkFault e) {
                System.err.println("addLicenseObligation() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("Obligation Added to license: " + licenseId);
        } catch (Exception e) {
            System.err.println("SampleCreateLicense failed");
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
