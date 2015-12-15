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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blackducksoftware.sdk.fault.ErrorCode;
import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.AnalysisStatus;
import com.blackducksoftware.sdk.protex.common.CaptureOptions;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.common.ComponentType;
import com.blackducksoftware.sdk.protex.common.ForcibleBooleanOption;
import com.blackducksoftware.sdk.protex.common.ForcibleUploadSourceCodeOption;
import com.blackducksoftware.sdk.protex.common.IdentificationOptions;
import com.blackducksoftware.sdk.protex.common.UploadSourceCodeOption;
import com.blackducksoftware.sdk.protex.comparison.ComparisonType;
import com.blackducksoftware.sdk.protex.comparison.FileComparisonApi;
import com.blackducksoftware.sdk.protex.comparison.ProtexFileSourceType;
import com.blackducksoftware.sdk.protex.comparison.RelatedSnippets;
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceRepository;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchDiscovery;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

/**
 * This sample creates a project with a given source tree and runs the protex analysis, then retrieves file comparison
 * information
 *
 * It demonstrates:
 * - How to create a project
 * - How to set the projects source location on the server
 * - How to start the analysis of the source code
 * - How to monitor the progress of the analysis and wait until it is done
 * - How to retrieve the resulting code tree in the project
 * - Hot to retrieve file similarities and differences based on matches
 */
public class SampleAnalyzeProjectAndCompareFiles extends BDProtexSample {

    private static CodeTreeApi codeTreeApi = null;

    private static ProjectApi projectApi = null;

    private static DiscoveryApi discoveryApi = null;

    private static FileComparisonApi fileComparisonApi = null;

    private static ComponentApi componentApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleAnalyzeProjectAndCompareFiles.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project name>");
        parameters.add("<server source directory>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project name",
                "The name for the project to be created and analyzed, i.e. \"My Example Project\" (include the quotes, if the name contains spaces)"));
        paramDescriptions.add(formatUsageDetail("server source directory", "the directory on the server where the source "
                + "to be analyzed can be found, i.e. \"my-example\" (include the quotes, if the name contains spaces). The "
                + "source directory is relative to the \"blackduck.serverFileURL\", by default this is /home/blackduck, if "
                + "that does not exist it is the home dir of the user running the protex-tomcat. This is the same location where "
                + "the UI opens the tree for selecting a source dir. You can also find this location under http://<your protex server>/protex/monitor?v=1"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 5) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        Long connectionTimeout = 120 * 1000L;

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectName = args[3];
        String serverSourceDir = args[4];

        ProtexServerProxy myProtexServer = null;

        try {
            // get service and service port
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                codeTreeApi = myProtexServer.getCodeTreeApi();
                projectApi = myProtexServer.getProjectApi();
                discoveryApi = myProtexServer.getDiscoveryApi();
                fileComparisonApi = myProtexServer.getFileComparisonApi();
                componentApi = myProtexServer.getComponentApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Create project
            ProjectRequest projectRequest = new ProjectRequest();
            projectRequest.setName(projectName);
            projectRequest.setDescription("A Sample project");

            // Set Source Location
            AnalysisSourceLocation sourceLocation = new AnalysisSourceLocation();
            URL hostUrl = new URL(serverUri);
            sourceLocation.setHostname(hostUrl.getHost());
            sourceLocation.setRepository(AnalysisSourceRepository.REMOTE_SERVER);
            // Source path relative to the source root set on the server, i.e. "my-example" if the source is in
            // /home/blackduck/my-example
            sourceLocation.setSourcePath(serverSourceDir);

            projectRequest.setAnalysisSourceLocation(sourceLocation);

            ForcibleBooleanOption trueForcible = new ForcibleBooleanOption();
            trueForcible.setOption(true);

            ForcibleUploadSourceCodeOption uploadForcible = new ForcibleUploadSourceCodeOption();
            uploadForcible.setValue(UploadSourceCodeOption.ENABLED_NOT_ENCRYPTED);

            IdentificationOptions identificationOptions = new IdentificationOptions();
            identificationOptions.setRequireIdOfCodeMatchesOption(trueForcible);

            projectRequest.setIdentificationOptions(identificationOptions);

            CaptureOptions captureOptions = new CaptureOptions();
            captureOptions.setMultiUserFileComparisonOption(trueForcible);
            captureOptions.setUploadSourceCodeOption(uploadForcible);

            String projectId = null;

            try {
                projectId = projectApi.createProject(projectRequest, LicenseCategory.PROPRIETARY);
                projectApi.updateCaptureOptions(projectId, captureOptions);
            } catch (SdkFault e) {
                System.err.println("ProjectApi.createProject() failed");
                throw new RuntimeException(e);
            }

            System.out.println("Project '" + projectName + "' created with source path '" + serverSourceDir + "'");

            // Start Analysis
            try {
                projectApi.startAnalysis(projectId, Boolean.FALSE);
            } catch (SdkFault e) {
                System.err.println("ProjectApi.startAnalysis() failed");
                throw new RuntimeException(e);
            }

            // Wait until Analysis is finished
            waitForScanToComplete(projectId);

            // get code tree generated during scan, and the number of files analyzed (files count themselves)
            String rootPath = "/";

            List<CodeTreeNode> nodes = null;

            try {
                CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
                codeTreeParameters.getIncludedNodeTypes().add(CodeTreeNodeType.FILE);
                codeTreeParameters.setDepth(CodeTreeUtilities.INFINITE_DEPTH);
                codeTreeParameters.setIncludeParentNode(false);
                codeTreeParameters.getCounts().add(NodeCountType.PENDING_ID_CODE_MATCH);

                nodes = codeTreeApi.getCodeTreeNodes(projectId, rootPath, codeTreeParameters);
            } catch (SdkFault e) {
                System.err.println("CodeTreeApi.getCodeTreeNodes() failed");
                throw new RuntimeException(e);
            }

            if (nodes == null) {
                System.err.println("CodeTreeApi.getCodeTreeNodes() returned no nodes");
                throw new RuntimeException("CodeTreeApi.getCodeTreeNodes() returned no nodes");
            }

            // Filter nodes to operate on based on pending ID matches
            List<CodeTreeNode> filesWithMatches = new ArrayList<CodeTreeNode>();

            for (CodeTreeNode node : nodes) {
                Map<NodeCountType, Long> counts = CodeTreeUtilities.getNodeCountMap(node);

                if (counts.get(NodeCountType.PENDING_ID_CODE_MATCH) > 0) {
                    filesWithMatches.add(node);
                }
            }

            // Get matches to read data about
            if (filesWithMatches.isEmpty()) {
                System.out
                .println("No files with pending ID code matches found to compare. Make sure code matches are set to marked as pending identification");
            } else {
                List<CodeMatchDiscovery> discoveries = discoveryApi
                        .getCodeMatchDiscoveries(projectId, filesWithMatches, Arrays.asList(CodeMatchType.PRECISION));

                System.out.println(discoveries.size() + " code matches found");

                Map<String, ComponentType> componentTypes = new HashMap<String, ComponentType>();
                Set<ComponentKey> matchedComponents = new HashSet<ComponentKey>();

                // Get matched component data
                for (CodeMatchDiscovery discovery : discoveries) {
                    if (discovery.getDiscoveredComponentKey() != null) {
                        matchedComponents.add(discovery.getDiscoveredComponentKey());
                    }
                }

                if (!matchedComponents.isEmpty()) {
                    List<ComponentKey> componentKeyInput = new ArrayList<ComponentKey>();
                    componentKeyInput.addAll(matchedComponents);
                    componentKeyInput = componentApi.checkComponentsExist(componentKeyInput);

                    List<Component> components = componentApi.getComponentsByKey(componentKeyInput);

                    for (Component component : components) {
                        componentTypes.put(toComparisonComponentId(component.getComponentKey()), component.getComponentType());
                    }
                }

                for (CodeMatchDiscovery discovery : discoveries) {
                    ProtexFileSourceType matchedType = null;
                    String comparisonKey = toComparisonComponentId(discovery.getDiscoveredComponentKey());
                    ComponentType componentType = componentTypes.get(comparisonKey);

                    if (ComponentType.CUSTOM.equals(componentType)) {
                        matchedType = ProtexFileSourceType.CUSTOM_COMPONENT;
                    } else if (ComponentType.STANDARD.equals(componentType) || ComponentType.STANDARD_MODIFIED.equals(componentType)) {
                        matchedType = ProtexFileSourceType.STANDARD_COMPONENT;
                    }

                    if (matchedType != null) {
                        try {
                            List<RelatedSnippets> similarities = fileComparisonApi.compareFiles(projectId, discovery, ComparisonType.SIMILARITIES);
                            List<RelatedSnippets> differences = fileComparisonApi.compareFiles(projectId, discovery, ComparisonType.DIFFERENCES);

                            System.out.println("Similarities between " + discovery.getFilePath() + " and "
                                    + toComparisonComponentId(discovery.getDiscoveredComponentKey()) + ":"
                                    + discovery.getMatchingFileLocation().getFilePath());
                            for (RelatedSnippets similarity : similarities) {
                                System.out.println("\t" + similarity.getLeftSnippet().getFirstLine() + " to " + similarity.getLeftSnippet().getFirstLine()
                                        + "\t" + similarity.getRightSnippet().getFirstLine() + " to " + similarity.getRightSnippet().getLastLine());
                            }

                            System.out.println("Differences between " + discovery.getFilePath() + " and "
                                    + toComparisonComponentId(discovery.getDiscoveredComponentKey()) + ":"
                                    + discovery.getMatchingFileLocation().getFilePath());
                            for (RelatedSnippets difference : differences) {
                                System.out.println("\t" + difference.getLeftSnippet().getFirstLine() + " to " + difference.getLeftSnippet().getFirstLine()
                                        + "\t" + difference.getRightSnippet().getFirstLine() + " to " + difference.getRightSnippet().getLastLine());
                            }
                        } catch (SdkFault fault) {
                            if (ErrorCode.PROTEX_FILE_SOURCE_NOT_AVAILABLE.equals(fault.getFaultInfo().getErrorCode())) {
                                System.out.println("Source not available for component path: " + toComparisonComponentId(discovery.getDiscoveredComponentKey())
                                        + ":"
                                        + discovery.getMatchingFileLocation().getFilePath());
                            } else {
                                System.err.println("Error reading sim/diff data for " + discovery.getFilePath() + " to "
                                        + toComparisonComponentId(discovery.getDiscoveredComponentKey())
                                        + ":" + discovery.getMatchingFileLocation().getFilePath());
                                fault.printStackTrace(System.err);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("SampleAnalyzeProjectAndGetCodeTree failed");
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

    /**
     * Waits until a project scan is complete
     *
     * @param projectId
     *            The ID of the project to wait for scan completion on
     */
    public static void waitForScanToComplete(String projectId) {
        boolean finished = false;
        long start = System.currentTimeMillis();

        while (!finished) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

            try {
                AnalysisStatus status = projectApi.getAnalysisStatus(projectId);
                System.out.println("Phase: " + status.getAnalysisPhase() + " (" + status.getCurrentPhasePercentCompleted() + "%)");
                System.out.println("Files analyzed: " + status.getAnalyzedFileCount()
                        + " ( pending Analysis: " + status.getAnalysisPendingFileCount() + ")");
                finished = status.isFinished();
            } catch (SdkFault e) {
                if (ErrorCode.NO_ANALYSIS_RUNNING.equals(e.getFaultInfo().getErrorCode())) {
                    // This means the scan is complete
                    finished = true;
                } else {
                    System.err.println("ProjectApi.getAnalysisStatus failed");
                    throw new RuntimeException(e);
                }
            }
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Finished analysis in " + duration / 1000 + " sec.");
    }

    private static String toComparisonComponentId(ComponentKey componentKey) {
        StringBuilder key = new StringBuilder();
        key.append(componentKey.getComponentId());

        if (componentKey.getVersionId() != null && !componentKey.getVersionId().isEmpty()) {
            key.append('#').append(componentKey.getVersionId());
        }

        return key.toString();
    }

}
