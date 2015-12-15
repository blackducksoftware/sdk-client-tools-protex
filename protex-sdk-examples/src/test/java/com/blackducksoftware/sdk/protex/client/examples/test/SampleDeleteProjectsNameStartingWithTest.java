package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleDeleteProjectsNameStartingWith;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;

public class SampleDeleteProjectsNameStartingWithTest extends AbstractSdkSampleTest {

    private String projectName;

    @BeforeClass
    protected void createProject() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("SampleListProjectsTest Project");

        getProxy().getProjectApi().createProject(projectRequest, LicenseCategory.OPEN_SOURCE);

        projectName = projectRequest.getName();
    }

    @Test
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = projectName.substring(0, projectName.length() - 1);

        SampleDeleteProjectsNameStartingWith.main(args);
    }

}
