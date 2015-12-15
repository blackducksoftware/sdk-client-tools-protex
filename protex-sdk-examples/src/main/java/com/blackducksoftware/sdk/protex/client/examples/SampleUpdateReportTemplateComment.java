package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.report.ReportApi;
import com.blackducksoftware.sdk.protex.report.ReportTemplate;
import com.blackducksoftware.sdk.protex.report.ReportTemplateRequest;

/**
 * This sample updates a report template's comment
 *
 * It demonstrates:
 * - How to update a report template
 */
public class SampleUpdateReportTemplateComment extends BDProtexSample {

    private static ReportApi reportApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleUpdateReportTemplateComment.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<report template title>");
        parameters.add("<report template comment>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("report template title", "The title of the template to update, i.e. \"A Report Template\""));
        paramDescriptions.add(formatUsageDetail("report template comment", "The comment to assign to the template, i.e. \"This is a commennt\""));

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
        String reportTitle = args[3];
        String comment = args[4];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                reportApi = myProtexServer.getReportApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Find the report template
            ReportTemplate reportTemplate = reportApi.getReportTemplateByTitle(reportTitle);

            // Call the Api
            ReportTemplateRequest templateRequest = new ReportTemplateRequest();
            templateRequest.setComment(comment);

            try {
                reportApi.updateReportTemplate(reportTemplate.getReportTemplateId(), templateRequest);
            } catch (SdkFault e) {
                System.err.println("updateReportTemplate() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("Report Template Id : " + reportTemplate.getReportTemplateId());
        } catch (Exception e) {
            System.err.println("SampleUpdateReportTemplateComment failed");
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
