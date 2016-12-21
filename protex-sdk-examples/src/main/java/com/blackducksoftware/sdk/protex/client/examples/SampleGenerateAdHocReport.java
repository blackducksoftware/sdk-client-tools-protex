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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ExternalDocumentLink;
import com.blackducksoftware.sdk.protex.report.Report;
import com.blackducksoftware.sdk.protex.report.ReportApi;
import com.blackducksoftware.sdk.protex.report.ReportFormat;
import com.blackducksoftware.sdk.protex.report.ReportSection;
import com.blackducksoftware.sdk.protex.report.ReportSectionType;
import com.blackducksoftware.sdk.protex.report.ReportTemplateRequest;

/**
 * This sample generates a report from an ad-hoc template and writes it to a file
 * 
 * It demonstrates:
 * - How to generate a report from a client side supplied template
 * - How to receive this report and write it to a file (using MTOM - Attachments)
 */
public class SampleGenerateAdHocReport extends BDProtexSample {

    private static ReportApi reportApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGenerateAdHocReport.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");
        parameters.add("<report file name>");
        parameters.add("<table of contents>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The project ID of the project to list, i.e. c_testproject"));
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
        if (args.length < 6) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectId = args[3];
        String reportFileName = args[4];
        String tableOfContents = args[5];

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
                ReportTemplateRequest request = createSummaryReportTemplateRequest();
                report = reportApi.generateAdHocProjectReport(projectId, request, getReportFormat(reportFileName), showTOC);
            } catch (SdkFault e) {
                System.err.println("generateAdHocProjectReport() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

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
            System.err.println("SampleGenerateAdHocReport failed");
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

    private static ReportTemplateRequest createSummaryReportTemplateRequest() {
        ReportTemplateRequest reportTemplate = new ReportTemplateRequest();
        reportTemplate.setName("Sample Report");
        reportTemplate.setComment("My Report comments");
        reportTemplate.setForced(Boolean.TRUE);

        ExternalDocumentLink externalDocumentLink1 = new ExternalDocumentLink();

        // externalDocumentLink1.setDocumentId(null);
        externalDocumentLink1.setDocumentLocation("http://www.example.com/no/doc/here.html");
        externalDocumentLink1.setLinkText("Some Text");

        reportTemplate.getExternalDocumentLinks().add(externalDocumentLink1);

        ReportSection section1 = new ReportSection();
        section1.setLabel("Summary123");
        section1.setSectionType(ReportSectionType.SUMMARY);

        ReportSection section2 = new ReportSection();
        section2.setLabel("Bill of Materials123");
        section2.setSectionType(ReportSectionType.BILL_OF_MATERIALS);

        ReportSection section3 = new ReportSection();
        section3.setLabel("Code Matches All 123");
        section3.setSectionType(ReportSectionType.CODE_MATCHES_ALL);

        reportTemplate.getSections().add(section1);
        reportTemplate.getSections().add(section2);
        reportTemplate.getSections().add(section3);

        return reportTemplate;
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
