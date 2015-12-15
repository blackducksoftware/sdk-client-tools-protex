package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;

/**
 * This sample demonstrates how to refresh the Bill of Materials (BOM) of a project
 * 
 * It demonstrates:
 * - How to refresh the BOM in a variety of modes
 */
public class SampleRefreshBom extends BDProtexSample {

    private static final String DIRTY_FILES_ONLY = "dirty_only";

    private static final String ALL = "all";

    private static final String SYNCHRONOUS = "synchronous";

    private static final String ASYNCHRONOUS = "asynchronous";

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleRefreshBom.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");
        parameters.add("<scope>");
        parameters.add("<refreshMode>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID of the project , i.e. \"c_newsampleproject\""));
        paramDescriptions.add(formatUsageDetail("scope", "The cope of the refresh. either \"" + DIRTY_FILES_ONLY + "\" or \"" + ALL + "\""));
        paramDescriptions.add(formatUsageDetail("refreshMode", "The mode of the refresh either \"" + SYNCHRONOUS + "\" or \"" + ASYNCHRONOUS + "\""));

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
        Boolean allFiles = null;

        if (args[4].equalsIgnoreCase(DIRTY_FILES_ONLY) || args[4].equalsIgnoreCase(ALL)) {
            allFiles = args[4].equalsIgnoreCase(ALL);
        } else {
            System.err.println("Invalid value for 5th parameter \"<scope>\"!");
            usage();
            System.exit(-1);
        }

        Boolean refeshSynchronous = null;

        if (args[5].equalsIgnoreCase(SYNCHRONOUS) || args[5].equalsIgnoreCase(ASYNCHRONOUS)) {
            refeshSynchronous = args[5].equalsIgnoreCase(SYNCHRONOUS);
        } else {
            System.err.println("Invalid value for 6th parameter \"<refreshMode>\"!");
            usage();
            System.exit(-1);
        }

        Long connectionTimeout = 240 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                bomApi = myProtexServer.getBomApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            try {
                // Run a BOM Refresh to propagate the identified discoveries
                bomApi.refreshBom(projectId, allFiles, refeshSynchronous);
            } catch (SdkFault e) {
                System.err.println("getBomComponents() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("BOM refresh for \"" + projectId + "\" finished.");
        } catch (Exception e) {
            System.err.println("SampleRefreshBom failed");
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
