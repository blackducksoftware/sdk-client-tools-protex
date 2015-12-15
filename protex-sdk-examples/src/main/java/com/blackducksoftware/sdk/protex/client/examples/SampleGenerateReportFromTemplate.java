package com.blackducksoftware.sdk.protex.client.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.report.Report;
import com.blackducksoftware.sdk.protex.report.ReportApi;
import com.blackducksoftware.sdk.protex.report.ReportFormat;

/**
 * This sample generates a report from a predefined template and writes it to a file
 * 
 * It demonstrates:
 * - How to generate a report from a client side supplied template
 * - How to receive this report and write it to a file (using MTOM - Attachments)
 */
public class SampleGenerateReportFromTemplate extends BDProtexSample {

    private static ReportApi reportApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGenerateReportFromTemplate.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");
        parameters.add("<report template ID>");
        parameters.add("<report file name>");
        parameters.add("<table of contents>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The project ID of the project to list, i.e. c_testproject"));
        paramDescriptions.add(formatUsageDetail("report template ID", "The ID of report template to use, e.g., myreporttemplate"));
        paramDescriptions
        .add(formatUsageDetail(
                "report file name",
                "File name of the report (with desired extension), e.g., report.html. report file name must have one of the following extensions: .html, .xls, .doc, .odt, which also determine the format of the report."));
        paramDescriptions
        .add(formatUsageDetail(
                "table of contents",
                "the table of Contents of the report sections in the project report, e.g., true. If the report will have the table of contents of the available report sections, if false - no table of contents in the report"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 7) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectId = args[3];
        String reportTemplateId = args[4];
        String reportFileName = args[5];
        String tableOfContents = args[6];

        if (getReportFormat(reportFileName) == null) {
            System.err.println("Invalid reportFileName extension!");
            usage();
            System.exit(-1);
        }

        Boolean showTOC = Boolean.valueOf("true".equals(tableOfContents));

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                // Set timeout for this API individually to indefinite - because reports can take a very long time.
                reportApi = myProtexServer.getReportApi(ProtexServerProxy.INDEFINITE_TIMEOUT);
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            Report report = null;

            try {
                report = reportApi.generateProjectReport(projectId, reportTemplateId, getReportFormat(reportFileName), showTOC);
            } catch (SdkFault e) {
                System.err.println("generateProjectReport failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println();
            System.out.println(report.getFileName());

            File transferredFile = new File(reportFileName);
            FileOutputStream outStream = null;

            try {
                outStream = new FileOutputStream(transferredFile);
                report.getFileContent().writeTo(outStream);
            } catch (IOException e) {
                System.err.println("report.getFileContent().writeTo() failed: " + e.getMessage());
                throw new RuntimeException(e);
            } finally {
                if (outStream != null) {
                    outStream.close();
                }
            }

            System.out.println("\nReport written to: " + transferredFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("SampleGenerateReportFromTemplate failed");
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

    private static ReportFormat getReportFormat(String reportFileName) {

        String fileNameExtension = reportFileName.substring(reportFileName.lastIndexOf(".") + 1, reportFileName
                .length());
        if (fileNameExtension.equals("html")) {
            return ReportFormat.HTML;
        } else if (fileNameExtension.equals("xls")) {
            return ReportFormat.XLS;
        } else if (fileNameExtension.equals("doc")) {
            return ReportFormat.MS_WORD;
        } else if (fileNameExtension.equals("odt")) {
            return ReportFormat.ODF_TEXT;
        } else {
            return null;
        }

    }

}
