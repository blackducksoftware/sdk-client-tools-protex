package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleListLocalComponentsOrderedByComponentName;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalComponentRequest;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalLicenseRequest;

public class SampleListLocalComponentsOrderedByComponentNameTest extends AbstractSdkSampleTest {

    private String projectId;

    @BeforeClass
    protected void createProject() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("SampleCreateLocalComponentAndAddToBomTest Project");

        projectId = getProxy().getProjectApi().createProject(projectRequest, LicenseCategory.OPEN_SOURCE);

        LocalComponentRequest componentRequest = new LocalComponentRequest();
        componentRequest.setName("Test Local Component");

        LocalLicenseRequest licenseRequest = new LocalLicenseRequest();
        licenseRequest.setBasedOnLicenseId("gpl20");
        licenseRequest.setLicenseText("Test text".getBytes());
        licenseRequest.setName("Test Local License");

        getProxy().getLocalComponentApi().createLocalComponent(projectId, componentRequest, licenseRequest);
    }

    @Test
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = projectId;

        SampleListLocalComponentsOrderedByComponentName.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        if (projectId != null) {
            getProxy().getProjectApi().deleteProject(projectId);
        }
    }

}
