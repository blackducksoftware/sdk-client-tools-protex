package com.blackducksoftware.sdk.protex.client.examples.report;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.license.License;
import com.blackducksoftware.sdk.protex.license.LicenseAttributes;
import com.blackducksoftware.sdk.protex.license.LicenseExtensionLevel;
import com.blackducksoftware.sdk.protex.license.PermittedOrRequired;
import com.blackducksoftware.sdk.protex.license.RestrictionType;
import com.blackducksoftware.sdk.protex.license.RightToDistributeBinaryForMaximumUsage;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;
import com.blackducksoftware.sdk.protex.project.bom.LicenseViolation;

public class SampleReportDataLicenseConflicts extends BDProtexSample {

    private static ProjectApi projectApi = null;

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleReportDataLicenseConflicts.class.getSimpleName();

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
                    List<LicenseViolation> licenseViolations = bomApi.getBomComponentLicenseViolatingAttributes(projectId, bomComponent.getComponentKey());

                    if (licenseViolations != null && !licenseViolations.isEmpty()) {
                        License license = null;
                        if (bomComponent.getLicenseInfo() != null) {
                            license = projectApi.getLicenseById(projectId, bomComponent.getLicenseInfo().getLicenseId());
                        }

                        for (LicenseViolation lViolation : licenseViolations) {
                            foundAny = true;

                            reportViolatingAttribute(rowFormat, bomComponent.getComponentKey(),
                                    bomComponent.getLicenseInfo() == null ? "" : bomComponent
                                            .getLicenseInfo().getName(), lViolation.getComponentKey(),
                                            lViolation.getLicenseInfo() == null ? "" : lViolation
                                                    .getLicenseInfo().getName(),
                                                    license == null ? null : license.getAttributes(), lViolation.getViolatingAttributes());
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
            System.err.println("SampleReportDataLicenseConflicts failed");
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

    private static void reportViolatingAttribute(String rowFormat, ComponentKey componentKey,
            String licenseName, ComponentKey violatingComponentKey, String violatingLicenseName,
            LicenseAttributes licenseAttributes, LicenseAttributes violatingAttributes) {
        String componentId = componentKey.getComponentId();
        String versionId = (componentKey.getVersionId() != null ? componentKey.getVersionId() : "");
        String violatingComponentId = violatingComponentKey.getComponentId();
        String violatingVersionId = (violatingComponentKey.getVersionId() != null ? violatingComponentKey.getVersionId() : "");

        if (violatingAttributes.getChargingFees() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, chargingFeesToText(licenseAttributes.getChargingFees()),
                    chargingFeesToText(violatingAttributes.getChargingFees())));
        }
        if (violatingAttributes.getDiscriminatoryRestrictions() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName,
                    discriminatoryRestrictionsToText(licenseAttributes.getDiscriminatoryRestrictions()),
                    discriminatoryRestrictionsToText(violatingAttributes.getDiscriminatoryRestrictions())));
        }
        if (violatingAttributes.getGrantRecipientRightToCopy() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName,
                    grantRecipientRightToCopyToText(licenseAttributes.getGrantRecipientRightToCopy()),
                    grantRecipientRightToCopyToText(violatingAttributes.getGrantRecipientRightToCopy())));
        }
        if (violatingAttributes.getGrantRecipientRightToModify() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName,
                    grantRecipientRightToModifyToText(licenseAttributes.getGrantRecipientRightToModify()),
                    grantRecipientRightToModifyToText(violatingAttributes.getGrantRecipientRightToModify())));
        }
        if (violatingAttributes.getGrantRecipientRightToReverseEngineer() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName,
                    grantRecipientRightToReverseEngineerToText(licenseAttributes
                            .getGrantRecipientRightToReverseEngineer()),
                            grantRecipientRightToReverseEngineerToText(violatingAttributes
                                    .getGrantRecipientRightToReverseEngineer())));
        }
        if (violatingAttributes.getIntegrationLevelForLicenseApplication() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName,
                    integrationLevelForLicenseApplicationtoText(licenseAttributes
                            .getIntegrationLevelForLicenseApplication()),
                            integrationLevelForLicenseApplicationtoText(violatingAttributes
                                    .getIntegrationLevelForLicenseApplication())));
        }
        if (violatingAttributes.getRightToDistributeBinaryForMaximumUsage() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName,
                    rightToDistributeBinaryForMaximumUsageToText(licenseAttributes
                            .getRightToDistributeBinaryForMaximumUsage()),
                            rightToDistributeBinaryForMaximumUsageToText(violatingAttributes
                                    .getRightToDistributeBinaryForMaximumUsage())));
        }
        if (violatingAttributes.getSourceCodeDistribution() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.getSourceCodeDistribution(),
                    violatingAttributes.getSourceCodeDistribution()));
        }
        if (violatingAttributes.isAntiDrmProvision() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isAntiDrmProvision(),
                    violatingAttributes.isAntiDrmProvision()));
        }
        if (violatingAttributes.isCarriesDistributionObligations() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isCarriesDistributionObligations(),
                    violatingAttributes.isCarriesDistributionObligations()));
        }
        if (violatingAttributes.isChangeNoticeRequired() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isChangeNoticeRequired(),
                    violatingAttributes.isChangeNoticeRequired()));
        }
        if (violatingAttributes.isExpressPatentLicense() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isExpressPatentLicense(),
                    violatingAttributes.isExpressPatentLicense()));
        }
        if (violatingAttributes.isIncludeLicense() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isIncludeLicense(),
                    violatingAttributes.isIncludeLicense()));
        }
        if (violatingAttributes.isIndemnificationRequired() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isIndemnificationRequired(),
                    violatingAttributes.isIndemnificationRequired()));
        }
        if (violatingAttributes.isLicenseBackRequired() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isLicenseBackRequired(),
                    violatingAttributes.isLicenseBackRequired()));
        }
        if (violatingAttributes.isLimitationOfLiabilityRequired() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isLimitationOfLiabilityRequired(),
                    violatingAttributes.isLimitationOfLiabilityRequired()));
        }
        if (violatingAttributes.isNotice() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isNotice(),
                    violatingAttributes.isNotice()));
        }
        if (violatingAttributes.isPatentRetaliation() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isPatentRetaliation(),
                    violatingAttributes.isPatentRetaliation()));
        }
        if (violatingAttributes.isPromotionRestriction() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isPromotionRestriction(),
                    violatingAttributes.isPromotionRestriction()));
        }
        if (violatingAttributes.isShareAlikeReciprocity() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isShareAlikeReciprocity(),
                    violatingAttributes.isShareAlikeReciprocity()));
        }
        if (violatingAttributes.isWarrantyDisclaimerRequired() != null) {
            System.out.println(String.format(rowFormat, componentId, versionId, licenseName, violatingComponentId,
                    violatingVersionId, violatingLicenseName, licenseAttributes.isWarrantyDisclaimerRequired(),
                    violatingAttributes.isWarrantyDisclaimerRequired()));
        }

    }

    private static Object rightToDistributeBinaryForMaximumUsageToText(
            RightToDistributeBinaryForMaximumUsage rightToDistributeBinaryForMaximumUsage) {
        String text = "Integration Level for License Application: ";
        switch (rightToDistributeBinaryForMaximumUsage) {
        case ANY:
            text += "For any Use";
            break;
        case INTERNAL_EVALUATION:
            text += "for internal Evaluation";
            break;
        case INTERNAL_PRODUCTION_USE:
            text += "for internal use in production";
            break;
        case NON_COMMERCIAL_OR_PERSONAL_USE:
            text += "for non commercial or personal use";
            break;
        default:
            text += "Unknown value !!!";
            break;
        }
        return text;
    }

    private static Object integrationLevelForLicenseApplicationtoText(
            LicenseExtensionLevel integrationLevelForLicenseApplication) {
        String text = "Integration Level for License Application: ";
        switch (integrationLevelForLicenseApplication) {
        case ACCOMPANYING_SOFTWARE_USING_PER_SLEEPY_CAT:
            text += "Accompanying Software per Sleepy Cat";
            break;
        case DYNAMIC_LIBRARY_PER_LGPL:
            text += "Dynamic Library per LGPL";
            break;
        case FILE_PER_MPL:
            text += "File per MPL";
            break;
        case MODULE_PER_EPL_CPL:
            text += "Module per CPL";
            break;
        case NON:
            text += "Non";
            break;
        case WORK_BASED_ON_PER_GPL:
            text += "Work based on per GPL";
            break;
        default:
            text += "Unknown value !!!";
            break;
        }
        return text;
    }

    private static Object grantRecipientRightToReverseEngineerToText(
            PermittedOrRequired grantRecipientRightToReverseEngineer) {
        return "Grant Recipient Right to Reverse Engineer: " + permittedOrRequiredToText(grantRecipientRightToReverseEngineer);
    }

    private static Object grantRecipientRightToModifyToText(PermittedOrRequired grantRecipientRightToModify) {
        return "Grant Recipient Right to Modify: " + permittedOrRequiredToText(grantRecipientRightToModify);
    }

    private static Object grantRecipientRightToCopyToText(PermittedOrRequired grantRecipientRightToCopy) {
        return "Grant Recipient Right to Copy: " + permittedOrRequiredToText(grantRecipientRightToCopy);
    }

    private static String permittedOrRequiredToText(PermittedOrRequired grantRecipientRightToCopy) {
        switch (grantRecipientRightToCopy) {
        case NOT_PERMITTED:
            return "not permitted";
        case PERMITTED:
            return "permitted";
        case REQUIRED:
            return "required";
        default:
            return "unknown status !!!";
        }
    }

    private static Object discriminatoryRestrictionsToText(RestrictionType discriminatoryRestrictions) {
        return "Discriminatory Restrictions: " + discriminatoryRestrictions;
    }

    private static Object chargingFeesToText(PermittedOrRequired chargingFees) {
        return "Charging Fees: " + chargingFees;
    }
}
