package com.blackducksoftware.sdk.protex.client.examples.report;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.StringSearchPattern;
import com.blackducksoftware.sdk.protex.project.ProjectApi;

/**
 * This sample gathers the data to generate the report section "String Searches"
 * 
 * It demonstrates:
 * - How to get string serach patterns ofr a project
 */
public class SampleReportDataStringSearchPatterns extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleReportDataStringSearchPatterns.class.getSimpleName();

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
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<StringSearchPattern> searches = null;

            try {
                searches = projectApi.getStringSearchPatterns(projectId);
            } catch (SdkFault e) {
                System.err.println("getStringSearchPatterns failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (searches != null) {
                // Display the report
                System.out.println("Search Pattern  \tType    \tMethod  \tPattern");
                for (StringSearchPattern search : searches) {
                    System.out.println("'" + search.getName() + "'\t" + search.getType() + "\t" + search.getMethod()
                            + "\t'" + search.getAdvancedPattern() + "'");
                }
            } else {
                System.out.println("No search patterns found for project");
            }
        } catch (Exception e) {
            System.err.println("SampleReportDataStringSearchPatterns failed");
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
