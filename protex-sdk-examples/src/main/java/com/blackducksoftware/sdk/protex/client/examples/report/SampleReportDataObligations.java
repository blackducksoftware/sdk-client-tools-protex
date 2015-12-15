package com.blackducksoftware.sdk.protex.client.examples.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.license.License;
import com.blackducksoftware.sdk.protex.license.LicenseApi;
import com.blackducksoftware.sdk.protex.license.LicenseOriginType;
import com.blackducksoftware.sdk.protex.obligation.AssignedObligation;
import com.blackducksoftware.sdk.protex.obligation.ObligationApi;
import com.blackducksoftware.sdk.protex.obligation.ObligationCategory;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;

/**
 * This sample gathers the data to generate the report section "Obligations"
 * 
 * It demonstrates:
 * - How to get the bill of material (BOM)
 * - How to retrieve a license by ID (including the license text)
 * - How to retrieve a component by its ID
 * - How to retrieve the obligation category
 * - How to translate the ID a component version into names that humans are more familiar with
 */
public class SampleReportDataObligations extends BDProtexSample {

    private static Map<String, String> obligationCategoryNames = new HashMap<String, String>();

    private static BomApi bomApi = null;

    private static ProjectApi projectApi = null;

    private static LicenseApi licenseApi = null;

    private static ObligationApi obligationApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleReportDataObligations.class.getSimpleName();

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

                licenseApi = myProtexServer.getLicenseApi();
                obligationApi = myProtexServer.getObligationApi();
                bomApi = myProtexServer.getBomApi();
                projectApi = myProtexServer.getProjectApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            List<BomComponent> bomComponents = null;

            try {
                bomComponents = bomApi.getBomComponents(projectId);
            } catch (SdkFault e) {
                System.err.println("getBomComponents() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (bomComponents != null && !bomComponents.isEmpty()) {
                // Display the license
                List<AssignedObligation> obligations = null;
                System.out.println("Component        \tLicense Name       \tFulfilled\tObligation                       \tCategory\tDiscription");

                for (BomComponent bomComponent : bomComponents) {
                    if (bomComponent.getLicenseInfo().getLicenseId() != null) {
                        License license = projectApi.getLicenseById(projectId, bomComponent.getLicenseInfo().getLicenseId());

                        if (!LicenseOriginType.PROJECT_LOCAL.equals(license.getLicenseOriginType())) {
                            try {
                                obligations = licenseApi.getLicenseObligations(bomComponent.getLicenseInfo().getLicenseId());
                            } catch (SdkFault e) {
                                System.err.println("getLicenseObligations() failed: " + e.getMessage());
                                throw new RuntimeException(e);
                            }

                            // Check for valid return
                            if (obligations != null && !obligations.isEmpty()) {
                                String componentLabel = bomComponent.getComponentName();

                                if (bomComponent.getVersionName() != null) {
                                    componentLabel += "//" + bomComponent.getVersionName();
                                }

                                for (AssignedObligation obligation : obligations) {
                                    System.out.println("'" + componentLabel
                                            + "'\t'" + bomComponent.getLicenseInfo().getName()
                                            + "'\t" + (obligation.isFulfilled() ? "Yes" : "No")
                                            + "\t'" + obligation.getName()
                                            + "'\t" + getObligationCategoryName(obligation.getObligationCategoryId())
                                            + (obligation.getDescription() == null ? "" : "\t'" + obligation.getDescription()
                                                    + "'"));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("SampleReportDataObligations failed");
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

    private static String getObligationCategoryName(String oCategoryId) throws SdkFault {
        String categoryName = obligationCategoryNames.get(oCategoryId);

        if (categoryName == null) {
            ObligationCategory category = obligationApi.getObligationCategoryById(oCategoryId);
            obligationCategoryNames.put(oCategoryId, category.getName());
            categoryName = category.getName();
        }

        return categoryName;
    }

}
