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

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;
import com.blackducksoftware.sdk.protex.project.template.TemplateApi;
import com.blackducksoftware.sdk.protex.project.template.TemplateInfo;
import com.blackducksoftware.sdk.protex.project.template.TemplateInfoColumn;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample creates new project
 *
 * It demonstrates:
 * - How to create project
 */
public class SampleCreateProjectFromTemplate extends BDProtexSample {

    private static ProjectApi projectApi = null;

    private static TemplateApi templateApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleCreateProjectFromTemplate.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<template name>");
        parameters.add("<new project name>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions
        .add(formatUsageDetail("template name",
                "The name of the template from which to create a new project, e.g. \"SaaS [System Template]\" (include the quotes, if the name contains spaces)"));
        paramDescriptions
        .add(formatUsageDetail("new project name",
                "The name of the new project to create, e.g. \"New Sample Project\" (include the quotes, if the name contains spaces)"));
        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 5) {
            System.err.println("\n\nNot enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String templateName = args[3];
        String newProjectName = args[4];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                projectApi = myProtexServer.getProjectApi();
                templateApi = myProtexServer.getTemplateApi();
            } catch (RuntimeException e) {
                System.err.println("\nConnection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Create the project Request
            ProjectRequest newProjectSettings = new ProjectRequest();
            newProjectSettings.setName(newProjectName);
            String templateId = null;

            String newProjectId = null;
            try {
                List<TemplateInfo> templates = templateApi.getTemplateInfos(PageFilterFactory.getAllRows(TemplateInfoColumn.TEMPLATE_NAME));
                for (TemplateInfo template : templates) {
                    if (template != null && templateName.equals(template.getName())) {
                        templateId = template.getTemplateId();
                        break;
                    }
                }

                if (templateId == null) {
                    System.err.println("Unable to find a template with name \"" + templateName + "\".");
                    System.exit(-1);
                }

                newProjectId = projectApi.createProjectFromTemplate(templateId, newProjectSettings);
            } catch (SdkFault e) {
                System.err.println("Operation failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // Print some information returned
            System.out.println();
            System.out.println("New project '" + newProjectName + "' with project ID '" + newProjectId + "'");
        } catch (Exception e) {
            System.err.println("SampleCreateProjectFromTemplate failed");
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
