package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.apache.cxf.common.util.SortedArraySet;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ComponentInfo;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchDiscovery;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.IdentificationStatus;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

/**
 * This sample counts the files that have pending code match IDs for each component discovered
 * 
 * It requires:
 * - An analyzed project which already exists on the server with code match discoveries
 * 
 * It demonstrates:
 * - How to retrieve the code tree for a project
 * - How to reduce the code tree to files only (in order to avoid duplicates)
 * - How to retrieve the components in the context of a project
 * - How to retrieve the component version
 * - How to determine a discovery is still pending
 * - How to manage memory on the client and server
 */
public class SampleCodeMatchDiscoveriesPendingIdFileCountPerComponent extends BDProtexSample {

    private static ProjectApi projectApi;

    private static CodeTreeApi codeTreeApi;

    private static DiscoveryApi discoveryApi;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCodeMatchDiscoveriesPendingIdFileCountPerComponent.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project name",
                "The name for the project to be created and analyzed, i.e. \"My Example Project\" (include the quotes,if the name contains spaces)"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 4) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        Long connectionTimeout = 120 * 1000L;

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectName = args[3];

        ProtexServerProxy myProtexServer = null;

        try {
            // get service and service port
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                projectApi = myProtexServer.getProjectApi();
                codeTreeApi = myProtexServer.getCodeTreeApi();
                discoveryApi = myProtexServer.getDiscoveryApi(10 * connectionTimeout);
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            String projectId = null;

            try {
                // For Summary Section
                Project project = projectApi.getProjectByName(projectName);
                projectId = project.getProjectId();
            } catch (SdkFault e) {
                System.err.println("getProjectById() failed: " + e.getMessage());
                System.exit(-1);
            }

            // get CodeTree
            String root = "/";
            List<CodeTreeNode> fileNodesOnly = null;

            try {
                // Get a code tree with only nodes of type "FILE". This has two effects:
                // * It prevents duplicates as for each folder level request I get all discoveries for all files
                // * It prevents out of memory on the server as well as on the client
                // *** For very large projects you probably want to request the discoveries with a code tree that points
                // to a
                // single file to avoid out of memory issues (as the number of discoveries can be very large)
                CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
                codeTreeParameters.getIncludedNodeTypes().add(CodeTreeNodeType.FILE);
                codeTreeParameters.setDepth(CodeTreeUtilities.INFINITE_DEPTH);
                codeTreeParameters.setIncludeParentNode(true);

                fileNodesOnly = codeTreeApi.getCodeTreeNodes(projectId, root, CodeTreeUtilities.ALL_NODES_PARAMETERS);
            } catch (SdkFault e) {
                System.err.println("CodeTreeApi.getCodeTreeNodes() failed");
                throw new RuntimeException(e);
            }

            // Get the Code Match Discovery List
            List<CodeMatchType> precisionOnly = new ArrayList<CodeMatchType>(1);
            precisionOnly.add(CodeMatchType.PRECISION);

            List<CodeMatchDiscovery> codeMatchDiscoveryList = null;

            try {
                codeMatchDiscoveryList = discoveryApi.getCodeMatchDiscoveries(projectId, fileNodesOnly, precisionOnly);
            } catch (SdkFault e) {
                System.err.println("DiscoveryApi.getCodeMatchDiscoveries() failed");
                throw new RuntimeException(e);
            }

            if (codeMatchDiscoveryList == null) {
                System.err.println("No code matches were found");
                throw new RuntimeException("Project had no code match discoveries");
            }

            // Store a map with key per componentVersion and a set of file names that have discoveries for that
            // component version
            Map<ComponentKey, SortedSet<String>> componentsUsed = new HashMap<ComponentKey, SortedSet<String>>();

            for (CodeMatchDiscovery cmd : codeMatchDiscoveryList) {
                if (IdentificationStatus.PENDING_IDENTIFICATION.equals(cmd.getIdentificationStatus())) {
                    ComponentKey componentVersionKey = cmd.getDiscoveredComponentKey();
                    SortedSet<String> filesWithMatches = componentsUsed.get(componentVersionKey);
                    filesWithMatches = (filesWithMatches != null ? filesWithMatches : new SortedArraySet<String>());
                    filesWithMatches.add(cmd.getFilePath());
                    componentsUsed.put(componentVersionKey, filesWithMatches);
                }
            }

            for (Entry<ComponentKey, SortedSet<String>> entry : componentsUsed.entrySet()) {
                // Retrieving the component through the project API ensures that the data for the component matches any
                // changes specific to the project, and that any local components are retrieved correctly
                ComponentInfo comp = projectApi.getComponentByKey(projectId, entry.getKey());

                if (comp.getComponentKey().getVersionId() != null) {
                    System.out.println(comp.getComponentName() + " \t" + comp.getVersionName() + ":: \t"
                            + entry.getValue().size() + " files with code match discoveries.");
                } else {
                    System.out.println(comp.getComponentName() + " \tunspecified:: \t"
                            + entry.getValue().size() + " files with code match discoveries.");
                }
                for (String file : entry.getValue()) {
                    System.out.println("++ " + file);
                }
            }
        } catch (Exception e) {
            System.err.println("SampleCodeMatchDiscoveriesPendingIdFileCountPerComponent failed");
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
