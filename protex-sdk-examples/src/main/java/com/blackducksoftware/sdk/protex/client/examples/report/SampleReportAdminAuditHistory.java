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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.report.AuditHistoryReportRequest;
import com.blackducksoftware.sdk.protex.report.Report;
import com.blackducksoftware.sdk.protex.report.ReportApi;

public class SampleReportAdminAuditHistory extends BDProtexSample {

    private static ReportApi reportApi;

    private static Report report;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleReportAdminAuditHistory.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<report file name>");
        parameters.add("<audit history start date>");
        parameters.add("<audit history end date");
        parameters.add("<admin user Ids>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("report file name", "The name of the audit history report file"));
        paramDescriptions.add(formatUsageDetail("audit history start date", "The start date of the audit history report"));
        paramDescriptions.add(formatUsageDetail("audit history end date", "The end date of the audit history report"));
        paramDescriptions.add(formatUsageDetail("admin user ids", "The admin user ids whose audit history needs to be populated"));

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
        String reportFileName = args[3];
        String auditHistoryStartDate = args[4];
        String auditHistoryEndDate = args[5];
        List<String> adminHistoryUserIds = new LinkedList<String>();

        for (int i = 6; i < args.length; i++) {
            adminHistoryUserIds.add(args[i]);
        }

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

            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                AuditHistoryReportRequest auditHistoryReportRequest = new AuditHistoryReportRequest();
                auditHistoryReportRequest.setStartDate(dateFormat.parse(auditHistoryStartDate));
                auditHistoryReportRequest.setEndDate(dateFormat.parse(auditHistoryEndDate));
                auditHistoryReportRequest.getUserIds().addAll(adminHistoryUserIds);

                report = reportApi.generateAuditHistoryReport(auditHistoryReportRequest);
            } catch (SdkFault e) {
                System.err.println("generateAuditHistoryReport() failed: " + e.getMessage());
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
            System.err.println("SampleReportAdminAuditHistory failed");
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
