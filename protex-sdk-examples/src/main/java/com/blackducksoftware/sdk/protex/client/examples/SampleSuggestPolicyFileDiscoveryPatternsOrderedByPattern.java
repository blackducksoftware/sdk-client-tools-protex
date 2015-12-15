package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.FileDiscoveryPattern;
import com.blackducksoftware.sdk.protex.common.FileDiscoveryPatternColumn;
import com.blackducksoftware.sdk.protex.common.FileDiscoveryPatternPageFilter;
import com.blackducksoftware.sdk.protex.policy.PolicyApi;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how to suggest global file discovery patterns (policy manager), that match a certain name
 * snippet
 * 
 * It demonstrates:
 * - How to get a list of global file discovery patterns matching a suggest pattern
 * 
 */
public class SampleSuggestPolicyFileDiscoveryPatternsOrderedByPattern extends BDProtexSample {

    private static PolicyApi policyApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleSuggestPolicyFileDiscoveryPatternsOrderedByPattern.class.getSimpleName();

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

                policyApi = myProtexServer.getPolicyApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<FileDiscoveryPattern> fileDiscoveryPatterns = null;
            FileDiscoveryPatternPageFilter pageFilter = PageFilterFactory.getAllRows(FileDiscoveryPatternColumn.FILE_TYPE);

            try {
                fileDiscoveryPatterns = policyApi.suggestFileDiscoveryPatterns(anyWordStartsWith, pageFilter);
            } catch (SdkFault e) {
                System.err.println("suggestFileDiscoveryPatterns() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (fileDiscoveryPatterns == null || fileDiscoveryPatterns.isEmpty()) {
                System.out.println("No FileDiscoveryPatterns returned");
            } else {
                for (FileDiscoveryPattern fileDiscoveryPattern : fileDiscoveryPatterns) {
                    System.out
                    .println("FileDiscoveryPattern: " + fileDiscoveryPattern.getPattern() + ", "
                            + fileDiscoveryPattern.getPatternId() + " File Type: "
                            + fileDiscoveryPattern.getFileType());
                }
            }
        } catch (Exception e) {
            System.err.println("SampleSuggestPolicyFileDiscoveryPatternsOrderedByPattern failed");
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
