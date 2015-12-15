package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleCreateProjectFromTemplate;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.project.Project;

public class SampleCreateProjectFromTemplateTest extends AbstractSdkSampleTest {

    private final String projectName = "SampleCreateProjectFromTemplateTest Project";

    private final String templateName = "SaaS [System Template]";

    @Test
    public void runSample() throws Exception {
        String[] args = new String[5];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = templateName;
        args[4] = projectName;

        SampleCreateProjectFromTemplate.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        Project project = getProxy().getProjectApi().getProjectByName(projectName);
        getProxy().getProjectApi().deleteProject(project.getProjectId());
    }

}
