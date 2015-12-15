package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.CodeLabelOption;
import com.blackducksoftware.sdk.protex.policy.PolicyApi;

public class SampleUpdateCodeLabelOption extends BDProtexSample {

    private static PolicyApi policyApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleUpdateCodeLabelOption.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<open source reference location>");
        parameters.add("<furnished by>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("open source reference location", "The location to provide for source code on code labels"));
        paramDescriptions.add(formatUsageDetail("furnished by",
                "The organization to set as the default furnisher or source code on code labels"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 5) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String openSourceReferencelocation = args[3];
        String furnishedBy = args[4];

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
            CodeLabelOption codeLabelOption = null;

            try {
                codeLabelOption = policyApi.getCodeLabelOption();
            } catch (SdkFault e) {
                System.err.println("getCodeLabelOption failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // showing global code label option (before updating)
            System.out.println("Before Updating Code Label Option:");

            if (codeLabelOption.getOpenSourceReferenceLocation() == null) {
                System.out.println("No Open Source Reference Location for Code Label Option");
            } else {
                System.out.println("openSourceReferenceLocation : " + codeLabelOption.getOpenSourceReferenceLocation());
            }

            if (codeLabelOption.getFurnishedBy() == null) {
                System.out.println("No Furnished By for Code Label Option");
            } else {
                System.out.println("Furnished By : " + codeLabelOption.getFurnishedBy());
            }

            // update the global code label option
            try {
                codeLabelOption.setOpenSourceReferenceLocation(openSourceReferencelocation);
                codeLabelOption.setFurnishedBy(furnishedBy);
                policyApi.updateCodeLabelOption(codeLabelOption);
            } catch (SdkFault e) {
                System.err.println("updateCodeLabelOption failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // Get global code label option (after updating)
            try {
                codeLabelOption = policyApi.getCodeLabelOption();
            } catch (SdkFault e) {
                System.err.println("getCodeLabelOption failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // showing global code label option (after updating)
            if (codeLabelOption.getOpenSourceReferenceLocation() == null) {
                System.out.println("No Open Source Reference Location for Code Label Option");
            } else {
                System.out.println("openSourceReferenceLocation : " + codeLabelOption.getOpenSourceReferenceLocation());
            }

            if (codeLabelOption.getFurnishedBy() == null) {
                System.out.println("No Furnished By for Code Label Option");
            } else {
                System.out.println("Furnished By : " + codeLabelOption.getFurnishedBy());
            }

            System.out.println("--------------------");
        } catch (Exception e) {
            System.err.println("SampleUpdateCodeLabelOption failed");
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
