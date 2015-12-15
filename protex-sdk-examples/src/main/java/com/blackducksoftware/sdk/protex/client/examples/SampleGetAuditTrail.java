package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.codetree.identification.AuditedEvent;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationApi;

/**
 * This sample demonstrates how to gradually identify all remaining code match discoveries
 * 
 * 
 * It requires:
 * - A project with ID event performed on it while audit trail recording was enabled
 * 
 * It demonstrates:
 * - How to generate a CodeTree for the root node only
 * - How to gather files with discoveries pending identification
 * - How to identify a discovery to its discovered code match (always choosing the first one in the list)
 * - How to refresh the BOM, before gathering the next round of discoveries
 * 
 * <p>
 * This samples demonstrates how it can be done straight forward. However for large projects this method will require
 * large amounts of memory in the client as well as in the server and could lead to "Out of Memory" exception on both
 * sides. See SampleIdentifyAllCodeMatchDiscoveriesMemoryManaged for a sample how to avoid this problem.
 * </p>
 */
public class SampleGetAuditTrail extends BDProtexSample {

    private static IdentificationApi identificationApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetAuditTrail.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The project ID of the project to list, i.e. c_testproject"));

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

        Long connectionTimeout = 240 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                identificationApi = myProtexServer.getIdentificationApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            try {
                // Identify at the file level where the discovery originates from
                Date ancientPast = new Date(0);
                Date now = new Date();
                List<AuditedEvent> events = identificationApi.getAuditTrail(projectId, ancientPast, now);

                if (events != null && !events.isEmpty()) {
                    for (AuditedEvent event : events) {
                        System.out.println("Identified: " + event.getPath() + " ==> " + event.getTime() + " ==> " + event.getType());
                    }
                }
            } catch (SdkFault e) {
                System.err.println("addCodeMatchIdentification failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.err.println("SampleGetAuditTrail failed");
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
