package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ExternalDocumentLink;
import com.blackducksoftware.sdk.protex.report.ReportApi;
import com.blackducksoftware.sdk.protex.report.ReportSection;
import com.blackducksoftware.sdk.protex.report.ReportSectionType;
import com.blackducksoftware.sdk.protex.report.ReportTemplateRequest;

/**
 * This sample creates a report template
 * 
 * It demonstrates:
 * - How to create a report template
 */
public class SampleCreateReportTemplate extends BDProtexSample {

    private static ReportApi reportApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCreateReportTemplate.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<report template title>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("report template title", "The title of the template to create, i.e. \"New Report Template\""));

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
        String reportTitle = args[3];

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

            // Call the Api
            ReportTemplateRequest templateRequest = new ReportTemplateRequest();
            templateRequest.setName(reportTitle);
            templateRequest.setComment("My Report comments");
            templateRequest.setForced(Boolean.TRUE);

            ExternalDocumentLink externalDocumentLink1 = new ExternalDocumentLink();

            // externalDocumentLink1.setDocumentId(null);
            externalDocumentLink1.setDocumentLocation("http://www.example.com/no/doc/here.html");
            externalDocumentLink1.setLinkText("Some Text");

            templateRequest.getExternalDocumentLinks().add(externalDocumentLink1);

            ReportSection section1 = new ReportSection();
            section1.setLabel("SampleSummary");
            section1.setSectionType(ReportSectionType.SUMMARY);

            ReportSection section2 = new ReportSection();
            section2.setLabel("SampleBOM");
            section2.setSectionType(ReportSectionType.BILL_OF_MATERIALS);

            templateRequest.getSections().add(section1);
            templateRequest.getSections().add(section2);

            String reportTemplateId = null;

            try {
                reportTemplateId = reportApi.createReportTemplate(templateRequest);
            } catch (SdkFault e) {
                System.err.println("createReportTemplate() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("Report Template Id : " + reportTemplateId);
        } catch (Exception e) {
            System.err.println("SampleCreateReportTemplate failed");
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
