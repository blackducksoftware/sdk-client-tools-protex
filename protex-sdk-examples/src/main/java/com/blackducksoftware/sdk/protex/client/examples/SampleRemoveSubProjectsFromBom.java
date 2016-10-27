package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.BomRefreshMode;
import com.blackducksoftware.sdk.protex.common.ComponentType;
import com.blackducksoftware.sdk.protex.common.SortType;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectInfo;
import com.blackducksoftware.sdk.protex.project.ProjectInfoColumn;
import com.blackducksoftware.sdk.protex.project.ProjectInfoPageFilter;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeMatchIdentification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeTreeIdentificationInfo;
import com.blackducksoftware.sdk.protex.project.codetree.identification.DeclaredIdentification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.DependencyIdentification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.Identification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationApi;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationMode;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationType;
import com.blackducksoftware.sdk.protex.project.codetree.identification.StringSearchIdentification;

/**
 * This is a sample sdk client example created per request from PROTEX-21896.
 *
 * This sdk example traverses through a list of projects, finds the sub-projects inside the project and reset the
 * identifications of that sub project if any identifications are present. If identifications for that subproject are
 * not present removes the subproject from the bom component.
 *
 * @author apitchai
 *
 */
public class SampleRemoveSubProjectsFromBom extends BDProtexSample {

    private static ProjectApi projectApi = null;

    private static IdentificationApi identificationApi = null;

    private static BomApi bomApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleRemoveSubProjectsFromBom.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<projects filter first index>");
        parameters.add("<projects filter last index>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions
                .add(formatUsageDetail("projects page filter first index",
                        "The first index of the project page filter in integer i.e. 0"));
        paramDescriptions
                .add(formatUsageDetail("projects page filter last index",
                        "The last index of the project page filter in integer i.e. 20"));
        outputUsageDetails(className, parameters, paramDescriptions);

    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 5) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String firstIndex = args[3];
        String lastIndex = args[4];

        Long connectionTimeout = 240 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

            projectApi = myProtexServer.getProjectApi();
            identificationApi = myProtexServer.getIdentificationApi();
            bomApi = myProtexServer.getBomApi();

            try {
                ProjectInfoPageFilter projectPageFilter = new ProjectInfoPageFilter();
                projectPageFilter.setFirstRowIndex(Integer.parseInt(firstIndex));
                projectPageFilter.setLastRowIndex(Integer.parseInt(lastIndex));
                projectPageFilter.setSortedColumn(ProjectInfoColumn.PROJECT_ID);
                projectPageFilter.setSortType(SortType.ALPHABETICAL_CASE_INSENSITIVE);
                projectPageFilter.setSortAscending(true);
                List<ProjectInfo> projectsInfo = projectApi.suggestProjects("", projectPageFilter);

                for (ProjectInfo pInfo : projectsInfo) {
                    String projectId = pInfo.getProjectId();

                    // Getting all the bom components of this project and filtering out the just the subprojects
                    List<BomComponent> allBomComponents = bomApi.getBomComponents(projectId);
                    List<BomComponent> subProjects = new LinkedList<BomComponent>();
                    for (BomComponent bComponent : allBomComponents) {
                        if (bComponent.getComponentType().equals(ComponentType.PROJECT)
                                && !bComponent.getComponentKey().getComponentId().equals(projectId)) {
                            subProjects.add(bComponent);
                        }
                    }

                    List<IdentificationType> idTypes = Arrays.asList(IdentificationType.CODE_MATCH, IdentificationType.DECLARATION,
                            IdentificationType.DEPENDENCY,
                            IdentificationType.STRING_SEARCH);
                    List<IdentificationMode> idModes = Arrays.asList(IdentificationMode.MANUAL, IdentificationMode.RAPID_ID);
                    // Finding the identifications of the subprojects and resetting the identifications.
                    // If no identifications are present, removing the subproject from bom component.
                    for (BomComponent bComponent : subProjects) {
                        List<CodeTreeNode> idNodes = bomApi.getFilesIdentifiedTo(projectId, bComponent.getComponentKey(), idTypes, idModes);
                        if (idNodes == null || idNodes.isEmpty()) {
                            bomApi.removeBomComponent(projectId, bComponent.getComponentKey(), true, BomRefreshMode.ASYNCHRONOUS);
                        } else {
                            List<CodeTreeIdentificationInfo> idsInfo = identificationApi.getEffectiveIdentifications(projectId, idNodes);
                            for (CodeTreeIdentificationInfo idInfo : idsInfo) {
                                for (Identification identification : idInfo.getIdentifications()) {
                                    if (identification instanceof CodeMatchIdentification) {
                                        CodeMatchIdentification codeMatchIdentification = (CodeMatchIdentification) identification;
                                        identificationApi.removeCodeMatchIdentification(projectId, codeMatchIdentification, BomRefreshMode.ASYNCHRONOUS);
                                    } else if (identification instanceof DeclaredIdentification) {
                                        DeclaredIdentification declaredIdentification = (DeclaredIdentification) identification;
                                        identificationApi.removeDeclaredIdentification(projectId, declaredIdentification, BomRefreshMode.ASYNCHRONOUS);
                                    } else if (identification instanceof StringSearchIdentification) {
                                        StringSearchIdentification stringSearchIdentification = (StringSearchIdentification) identification;
                                        identificationApi.removeStringSearchIdentification(projectId, stringSearchIdentification, BomRefreshMode.ASYNCHRONOUS);
                                    } else if (identification instanceof DependencyIdentification) {
                                        DependencyIdentification dependencyIdentification = (DependencyIdentification) identification;
                                        identificationApi.removeDependencyIdentification(projectId, dependencyIdentification, BomRefreshMode.ASYNCHRONOUS);
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

        } catch (RuntimeException e) {
            System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
            throw e;
        }
    }
}
