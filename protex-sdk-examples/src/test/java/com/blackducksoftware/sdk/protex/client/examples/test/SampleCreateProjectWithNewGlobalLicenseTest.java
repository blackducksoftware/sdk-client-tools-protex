package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleCreateProjectWithNewGlobalLicense;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.license.GlobalLicense;
import com.blackducksoftware.sdk.protex.project.Project;

public class SampleCreateProjectWithNewGlobalLicenseTest extends AbstractSdkSampleTest {

    private String projectName = "SampleCreateProjectWithNewGlobalLicenseTest Project";

    private String licenseName = "SampleCreateProjectWithNewGlobalLicenseTest License";

    @Test(groups = { Tests.OPERATION_AFFECTING_TEST })
    public void runSample() throws Exception {
        String[] args = new String[5];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = projectName;
        args[4] = licenseName;

        SampleCreateProjectWithNewGlobalLicense.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        Project project = getProxy().getProjectApi().getProjectByName(projectName);
        getProxy().getProjectApi().deleteProject(project.getProjectId());

        GlobalLicense license = getProxy().getLicenseApi().getLicenseByName(licenseName);
        getProxy().getLicenseApi().deleteLicense(license.getLicenseId());
    }

}
