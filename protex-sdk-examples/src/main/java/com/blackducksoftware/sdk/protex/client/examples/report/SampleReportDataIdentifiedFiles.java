package com.blackducksoftware.sdk.protex.client.examples.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ComponentInfo;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.codetree.CharEncoding;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;
import com.blackducksoftware.sdk.protex.project.codetree.SourceFileInfoNode;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeMatchIdentification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeTreeIdentificationInfo;
import com.blackducksoftware.sdk.protex.project.codetree.identification.DeclaredIdentification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.DependencyIdentification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.Identification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationApi;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationType;
import com.blackducksoftware.sdk.protex.project.codetree.identification.StringSearchIdentification;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

public class SampleReportDataIdentifiedFiles extends BDProtexSample {

    private static final String ROOT = "/";

    private static Map<ComponentKey, ComponentInfo> componentInfos = new HashMap<ComponentKey, ComponentInfo>();

    private static ProjectApi projectApi = null;

    private static CodeTreeApi codeTreeApi = null;

    private static IdentificationApi identificationApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleReportDataIdentifiedFiles.class.getSimpleName();

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

        Long connectionTimeout = 240 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                projectApi = myProtexServer.getProjectApi();
                codeTreeApi = myProtexServer.getCodeTreeApi();
                identificationApi = myProtexServer.getIdentificationApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            final String rowFormat = "%1$-16s | %2$-14s | %3$-80s | %4$10s | %5$11s | %6$11s | "
                    + "%7$-20s | %8$-15s | %9$-20s | %10$-20s | %11$4s%% | %12$-80s | %13$12s | %14$25s | %15$20s";
            System.out.println("");
            System.out.println(String.format(rowFormat, "Resolution Type", "Discovery Type", "File / Folder", "Size",
                    "File Line", "Total Lines", "Component", "Version", "License", "Usage", "", "Matched File",
                    "Matched File Line", "Comment", "Search"));

            try {
                CodeTreeWorker myWorker = new CodeTreeWorker() {
                    @Override
                    public void doWork(String projectId, String parentPath, List<CodeTreeNode> thisLevel) throws SdkFault {
                        // do the work here
                        if (thisLevel.size() == 0) {
                            return;
                        }
                        List<CodeTreeNode> fileNodes = new ArrayList<CodeTreeNode>();

                        for (CodeTreeNode node : thisLevel) {
                            if (node.getNodeType() == CodeTreeNodeType.FILE) {
                                fileNodes.add(node);
                            }
                        }
                        if (fileNodes.size() == 0) {
                            // System.out.println("CodeTree for '" + thisLevel.getParentPath()
                            // + "' does not contain any file nodes");
                            return;
                        }
                        List<CodeTreeIdentificationInfo> idInfos = identificationApi.getEffectiveIdentifications(projectId, fileNodes);
                        List<SourceFileInfoNode> fileInfos = codeTreeApi.getFileInfo(projectId, parentPath, -1, true, CharEncoding.NONE);

                        Map<String, SourceFileInfoNode> fileInfoMap = new HashMap<String, SourceFileInfoNode>();

                        for (SourceFileInfoNode fileInfo : fileInfos) {
                            fileInfoMap.put(fileInfo.getName(), fileInfo);
                        }

                        for (CodeTreeIdentificationInfo idInfo : idInfos) {
                            String filePath = idInfo.getName();
                            SourceFileInfoNode thisNodeFileInfo = fileInfoMap.get(filePath);
                            for (Identification id : idInfo.getIdentifications()) {
                                ComponentInfo componentInfo = getComponentInfo(projectId, id.getIdentifiedComponentKey());
                                String versionName = (componentInfo.getVersionName() != null ? componentInfo.getVersionName() : "");

                                if (IdentificationType.CODE_MATCH.equals(id.getType())) {
                                    CodeMatchIdentification cmId = (CodeMatchIdentification) id;
                                    System.out.println(String.format(rowFormat, "", id.getType(), filePath,
                                            thisNodeFileInfo.getLength(),
                                            cmId.getFirstLine(), "<?>",
                                            componentInfo.getComponentName(),
                                            versionName,
                                            id.getIdentifiedLicenseInfo() == null ? "" : id.getIdentifiedLicenseInfo()
                                                    .getName(),
                                                    id.getIdentifiedUsageLevel(),
                                                    cmId.getMatchRatioAsPercent(), cmId.getComponentFilePath(),
                                                    "Matched File Line", "Comment",
                                            ""));
                                } else if (IdentificationType.STRING_SEARCH.equals(id.getType())) {
                                    StringSearchIdentification ssId = (StringSearchIdentification) id;
                                    System.out.println(String.format(
                                            rowFormat,
                                            "",
                                            ssId.getType(),
                                            filePath,
                                            thisNodeFileInfo.getLength(),
                                            "",
                                            "<?>",
                                            componentInfo.getComponentName(),
                                            versionName,
                                            id.getIdentifiedLicenseInfo() == null ? "" : id.getIdentifiedLicenseInfo()
                                                    .getName(),
                                                    id.getIdentifiedUsageLevel(),
                                                    "", "",
                                                    "Matched File Line", "Comment",
                                                    ssId.getStringSearchId()));
                                } else if (IdentificationType.DEPENDENCY.equals(id.getType())) {
                                    DependencyIdentification dpId = (DependencyIdentification) id;
                                    System.out.println(String.format(
                                            rowFormat,
                                            "",
                                            dpId.getType(),
                                            filePath,
                                            thisNodeFileInfo.getLength(),
                                            "",
                                            "<?>",
                                            componentInfo.getComponentName(),
                                            versionName,
                                            id.getIdentifiedLicenseInfo() == null ? "" : id.getIdentifiedLicenseInfo()
                                                    .getName(),
                                                    id.getIdentifiedUsageLevel(),
                                                    "", "",
                                                    "Matched File Line", "Comment",
                                            ""));
                                } else if (IdentificationType.DECLARATION.equals(id.getType())) {
                                    DeclaredIdentification dId = (DeclaredIdentification) id;
                                    System.out.println(String.format(
                                            rowFormat,
                                            "",
                                            dId.getType(),
                                            filePath,
                                            thisNodeFileInfo.getLength(),
                                            "",
                                            "<?>",
                                            componentInfo.getComponentName(),
                                            versionName,
                                            id.getIdentifiedLicenseInfo() == null ? "" : id.getIdentifiedLicenseInfo()
                                                    .getName(),
                                                    id.getIdentifiedUsageLevel(),
                                                    "", "",
                                                    "Matched File Line", "Comment",
                                            ""));
                                }
                            }
                        }
                    }
                };

                myWorker.walk(projectId, ROOT);
            } catch (SdkFault e) {
                System.err.println("getCodeTree() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.err.println("SampleReportDataIdentifiedFiles failed");
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

    private static ComponentInfo getComponentInfo(String projectId, ComponentKey componentKey) throws SdkFault {
        ComponentInfo componentInfo = componentInfos.get(componentKey);
        if (componentInfo == null) {
            componentInfo = projectApi.getComponentByKey(projectId, componentKey);
            componentInfos.put(componentInfo.getComponentKey(), componentInfo);
        }

        return componentInfo;
    }

    private static abstract class CodeTreeWorker {

        public abstract void doWork(String projectId, String parentPath, List<CodeTreeNode> nodes) throws SdkFault;

        private void walk(String projectId, String parentPath) throws SdkFault {
            List<CodeTreeNode> thisLevel = getCodeTreeChildren(projectId, parentPath);
            // Deal with all the sub-folders first
            for (CodeTreeNode node : thisLevel) {
                if ((CodeTreeNodeType.EXPANDED_ARCHIVE.equals(node.getNodeType()))
                        || (CodeTreeNodeType.FOLDER.equals(node.getNodeType()))) {
                    String subFolder = node.getName();
                    walk(projectId, subFolder);
                }
            }

            doWork(projectId, parentPath, thisLevel);
        }

        private List<CodeTreeNode> getCodeTreeChildren(String projectId, String parentPath) throws SdkFault {
            CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
            codeTreeParameters.getIncludedNodeTypes().addAll(CodeTreeUtilities.ALL_CODE_TREE_NODE_TYPES);
            codeTreeParameters.setDepth(CodeTreeUtilities.DIRECT_CHILDREN);
            codeTreeParameters.setIncludeParentNode(false);

            return codeTreeApi.getCodeTreeNodes(projectId, parentPath, codeTreeParameters);
        }

    }





}
