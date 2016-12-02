/*
 * Black Duck Software Suite SDK
 * Copyright (C) 2015  Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.blackducksoftware.sdk.protex.client.examples.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.ComponentInfo;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.codetree.CharEncoding;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.SourceFileInfoNode;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeMatchIdentification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeMatchIdentificationDirective;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeTreeIdentificationInfo;
import com.blackducksoftware.sdk.protex.project.codetree.identification.DeclaredIdentification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.DependencyIdentification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.Identification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationApi;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationType;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentifiedStringSearchMatchLocation;
import com.blackducksoftware.sdk.protex.project.codetree.identification.StringSearchIdentification;
import com.blackducksoftware.sdk.protex.util.CodeTreeNodeComparator;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

public class SampleReportDataIdentifiedFiles extends BDProtexSample {

    private static final String ROOT = "/";

    private static Map<String, ComponentInfo> componentInfos = new HashMap<String, ComponentInfo>();

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

            CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
            codeTreeParameters.getIncludedNodeTypes().addAll(CodeTreeUtilities.ALL_CODE_TREE_NODE_TYPES);
            codeTreeParameters.setDepth(CodeTreeUtilities.INFINITE_DEPTH);
            codeTreeParameters.setIncludeParentNode(false);

            List<CodeTreeNode> fileNodes = codeTreeApi.getCodeTreeNodes(projectId, ROOT, codeTreeParameters);
            Collections.sort(fileNodes, CodeTreeNodeComparator.getInstance());

            List<CodeTreeIdentificationInfo> idInfos = identificationApi.getEffectiveIdentifications(projectId, fileNodes);
            List<SourceFileInfoNode> fileInfos = codeTreeApi.getFileInfo(projectId, ROOT, CodeTreeUtilities.INFINITE_DEPTH, true, CharEncoding.NONE);

            Map<String, SourceFileInfoNode> fileInfoMap = new HashMap<String, SourceFileInfoNode>();

            for (SourceFileInfoNode fileInfo : fileInfos) {
                fileInfoMap.put(fileInfo.getName(), fileInfo);
            }

            final String rowFormat = "%-16s | %-26s | %-100s | %9s | %9s | %-40s | %-40s | %-20s | %-40s | %-30s | %8s | %-100s | %12s | %s";
            System.out.println("");
            System.out.println(String.format(rowFormat, "Resolution Type", "Discovery Type", "File / Folder", "File Size", "File Line", "Discovery",
                    "Identified Component", "Identified Version", "License", "Usage", "Coverage", "Matched File", "Matched Line", "Comment"));

            for (CodeTreeIdentificationInfo idInfo : idInfos) {
                String filePath = idInfo.getName();
                SourceFileInfoNode thisNodeFileInfo = fileInfoMap.get(filePath);

                Long fileSize = thisNodeFileInfo != null ? thisNodeFileInfo.getLength() : null;

                for (Identification id : idInfo.getIdentifications()) {
                    ComponentInfo componentInfo = getComponentInfo(projectId, id.getIdentifiedComponentKey());

                    CodeMatchIdentificationDirective resolutionType = null;
                    Object discoveryType = id.getType();
                    Object fileLine = null;
                    String discovery = "";
                    String componentName = componentInfo.getComponentName();
                    String versionName = toString(componentInfo.getVersionName());
                    String license = id.getIdentifiedLicenseInfo() != null ? id.getIdentifiedLicenseInfo().getName() : "";
                    UsageLevel usage = id.getIdentifiedUsageLevel();
                    Integer coverage = null;
                    String matchedFile = "";
                    Integer matchedFileLine = null;
                    String comment = "";

                    if (IdentificationType.CODE_MATCH.equals(id.getType())) {
                        CodeMatchIdentification cmId = (CodeMatchIdentification) id;

                        resolutionType = cmId.getCodeMatchIdentificationDirective();
                        fileLine = cmId.getFirstLine();
                        discovery = toString(cmId.getDiscoveredComponentKey());
                        coverage = cmId.getMatchRatioAsPercent();
                        matchedFile = cmId.getComponentFilePath();
                        comment = cmId.getComment();

                    } else if (IdentificationType.STRING_SEARCH.equals(id.getType())) {
                        StringSearchIdentification ssId = (StringSearchIdentification) id;

                        Collections.sort(ssId.getMatchLocations(), new Comparator<IdentifiedStringSearchMatchLocation>() {
                            @Override
                            public int compare(IdentifiedStringSearchMatchLocation hit1, IdentifiedStringSearchMatchLocation hit2) {
                                return hit1.getFirstLine().compareTo(hit2.getFirstLine());
                            }
                        });

                        StringJoiner fileLines = new StringJoiner();
                        Map<String, List<String>> commentsMap = new LinkedHashMap<String, List<String>>();

                        for (IdentifiedStringSearchMatchLocation hit : ssId.getMatchLocations()) {
                            String lineStr = toString(hit.getFirstLine());
                            fileLines.append(lineStr);

                            String oneComment = hit.getIdentificationComment();
                            if ((oneComment != null) && !oneComment.isEmpty()) {
                                List<String> commentLines = commentsMap.get(oneComment);

                                if (commentLines == null) {
                                    commentLines = new ArrayList<String>();
                                    commentsMap.put(oneComment, commentLines);
                                }

                                commentLines.add(lineStr);
                            }
                        }

                        fileLine = fileLines.toString();
                        discovery = ssId.getStringSearchId();

                        if ((commentsMap.size() == 1) && (commentsMap.values().iterator().next().size() == ssId.getMatchLocations().size())) {
                            comment = commentsMap.keySet().iterator().next();

                        } else {
                            StringJoiner comments = new StringJoiner();

                            for (Entry<String, List<String>> commentsEntry : commentsMap.entrySet()) {
                                StringJoiner commentLines = new StringJoiner();

                                for (String lineStr : commentsEntry.getValue()) {
                                    commentLines.append(lineStr);
                                }

                                comments.append("(", commentLines, ") ", commentsEntry.getKey());
                            }

                            comment = comments.toString();
                        }

                    } else if (IdentificationType.DEPENDENCY.equals(id.getType())) {
                        DependencyIdentification dpId = (DependencyIdentification) id;

                        discoveryType = dpId.getDependencyType();
                        discovery = dpId.getDependencyId();
                        comment = dpId.getComment();

                    } else if (IdentificationType.DECLARATION.equals(id.getType())) {
                        DeclaredIdentification dId = (DeclaredIdentification) id;

                        comment = dId.getComment();
                    }

                    System.out.println(String.format(rowFormat, toString(resolutionType), toString(discoveryType), filePath, fileSize, toString(fileLine),
                            discovery, componentName, versionName, license, toString(usage), coverage != null ? coverage.toString() + "%" : "", matchedFile,
                            toString(matchedFileLine), comment));
                }
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

    private static String toString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private static String toString(ComponentKey componentKey) {
        return componentKey.getComponentId()
                + ((componentKey.getVersionId() != null) && !componentKey.getVersionId().isEmpty() ? "#" + componentKey.getVersionId() : "");
    }

    private static ComponentInfo getComponentInfo(String projectId, ComponentKey componentKey) throws SdkFault {
        String componentName = toString(componentKey);
        ComponentInfo componentInfo = componentInfos.get(componentName);

        if (componentInfo == null) {
            componentInfo = projectApi.getComponentByKey(projectId, componentKey);
            componentInfos.put(componentName, componentInfo);
        }

        return componentInfo;
    }

    private static class StringJoiner {

        private final StringBuilder builder = new StringBuilder();

        private final String joiner;

        public StringJoiner() {
            this(", ");
        }

        public StringJoiner(String joiner) {
            this.joiner = joiner;
        }

        public int length() {
            return builder.length();
        }

        public boolean isEmpty() {
            return length() == 0;
        }

        public StringJoiner append(Object obj) {
            String s = SampleReportDataIdentifiedFiles.toString(obj);
            if ((s != null) && !s.isEmpty()) {
                join();
                builder.append(s);
            }
            return this;
        }

        public StringJoiner append(Object... objs) {
            boolean join = true;
            for (Object obj : objs) {
                String s = SampleReportDataIdentifiedFiles.toString(obj);
                if ((s != null) && !s.isEmpty()) {
                    if (join) {
                        join();
                        join = false;
                    }
                    builder.append(s);
                }
            }
            return this;
        }

        protected void join() {
            if (!isEmpty()) {
                builder.append(joiner);
            }
        }

        @Override
        public String toString() {
            return builder.toString();
        }

    }

}
