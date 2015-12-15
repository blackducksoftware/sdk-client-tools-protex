/*
 * Copyright (C) 2009, 2010 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.report.ReportApi;
import com.blackducksoftware.sdk.protex.report.ReportTemplate;

/**
 * This sample demonstrates how to suggest a list of report templates matching a suggested string
 * 
 * It demonstrates:
 * - How to get a list of report templates matching a suggest pattern
 * 
 */
public class SampleSuggestReportTemplates extends BDProtexSample {

    private static ReportApi reportApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleSuggestReportTemplates.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<any word starting with>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("any word starting with", "A few initial letters of pattern you want to search or suggest , i.e. \"ja\""));

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
        String anyWordStartsWith = args[3];

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

            List<ReportTemplate> reportTemplates = null;

            try {
                reportTemplates = reportApi.suggestReportTemplates(anyWordStartsWith);
            } catch (SdkFault e) {
                System.err.println("suggestReportTemplates() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (reportTemplates == null || reportTemplates.isEmpty()) {
                System.err.println("No report templates matching the suggest pattern returned");
            } else {
                for (ReportTemplate template : reportTemplates) {
                    System.out.println("Title: " + template.getName() + "; Id: " + template.getReportTemplateId());
                }
            }
        } catch (Exception e) {
            System.err.println("SampleSuggestReportTemplates failed");
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
