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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.codetree.CharEncoding;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.SourceFileInfoNode;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

/**
 * This sample gathers the file information for a all paths in the project.
 * 
 * It demonstrates:
 * - How to generate a CodeTree
 * - How to retrieve file information for all nodes in the code tree
 * - How to use date related information from the SDK
 */
public class SampleGetFileInfo extends BDProtexSample {

    private static CodeTreeApi codetreeApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetFileInfo.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The project ID of the project to list file info for, i.e. c_testproject"));

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

                codetreeApi = myProtexServer.getCodeTreeApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            String root = "/";

            List<SourceFileInfoNode> fileInfo = null;

            try {
                fileInfo = codetreeApi.getFileInfo(projectId, root, CodeTreeUtilities.INFINITE_DEPTH, Boolean.TRUE, CharEncoding.BASE_64);
            } catch (SdkFault e) {
                System.err.println("getFileInfo() failed: " + e.getMessage());
                System.exit(-1);
            }
            if (fileInfo == null) {
                System.err.println("no getFileInfo() returned");
                System.exit(-1);
            }

            final String rowFormat = "%1$-60s | %2$12s | %3$-8s | %4$32s | %5$32s | %6$23s | " + "%7$-60s";
            System.out.println("");
            System.out.println(String.format(rowFormat, "File", "File Type", "Size", "Hash", "MD5 Sum", "Date", "Last Contributions"));

            for (SourceFileInfoNode node : fileInfo) {
                String filePath = node.getName();
                String lastContributions = parseLastContributions(node.getLastContributions());
                Date fileSystemDate = node.getFileSystemDate();
                DateFormat outputFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT,
                        SimpleDateFormat.DEFAULT);

                System.out.println(String.format(rowFormat, filePath, node.getNodeType(), node.getLength(),
                        node.getHash(), node.getExactChecksum(), outputFormat.format(fileSystemDate),
                        lastContributions
                        ));
            }
        } catch (Exception e) {
            System.err.println("SampleGetFileInfo failed");
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

    private static String parseLastContributions(List<String> lastContributions) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String lastContribution : lastContributions) {
            if (!first) {
                sb.append("; ");
            } else {
                first = !first;
            }
            String[] contributions = lastContribution.split("=");
            try {
                sb.append(URLDecoder.decode(contributions[0], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            sb.append(" (");
            DateFormat df = new java.text.SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            // explicitly set timezone of input if needed
            df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
            Date date;
            try {
                date = df.parse(contributions[1]);
                DateFormat outputFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT,
                        SimpleDateFormat.DEFAULT);

                sb.append(outputFormat.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            sb.append(" )");
        }
        return sb.toString();
    }
}
