package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.FileDiscoveryPattern;
import com.blackducksoftware.sdk.protex.common.FileDiscoveryPatternColumn;
import com.blackducksoftware.sdk.protex.common.FileDiscoveryPatternPageFilter;
import com.blackducksoftware.sdk.protex.common.PatternOriginType;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how to suggest file discovery patterns at the project level
 * 
 * It demonstrates:
 * - How to get a list of project file discovery patterns
 * - How to use patternTypeFilter
 * - How to use PageFilter to sort by a certain column
 */
public class SampleListProjectFileDiscoveryPatternsOrderedByPattern extends BDProtexSample {

    private static ProjectApi projectApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleListProjectFileDiscoveryPatternsOrderedByPattern.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID of the project , i.e. \"c_newsampleproject\""));

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
                System.err.println("\nConnection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<FileDiscoveryPattern> fileDiscoveryPatterns = null;
            FileDiscoveryPatternPageFilter pageFilter = PageFilterFactory.getAllRows(FileDiscoveryPatternColumn.PATTERN);

            List<PatternOriginType> originTypeList = Arrays.asList(PatternOriginType.values());

            try {
                fileDiscoveryPatterns = projectApi.getFileDiscoveryPatterns(projectId, originTypeList, pageFilter);
            } catch (SdkFault e) {
                System.err.println("getFileDiscoveryPatterns() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (fileDiscoveryPatterns == null || fileDiscoveryPatterns.isEmpty()) {
                System.out.println("No FileDiscoveryPatterns returned");
            } else {
                for (FileDiscoveryPattern fileDiscoveryPattern : fileDiscoveryPatterns) {
                    System.out.println("FileDiscoveryPattern: " + fileDiscoveryPattern.getPattern() + ", "
                            + fileDiscoveryPattern.getPatternId() + " Origin: " + fileDiscoveryPattern.getOriginType());
                }
            }
        } catch (Exception e) {
            System.err.println("SampleListProjectFileDiscoveryPatternsOrderedByPattern failed");
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
