package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleAddBomComponentVersion;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.TestSources;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;

public class SampleAddBomComponentVersionTest extends AbstractSdkSampleTest {

    private String projectId, projectName;

    @BeforeClass
    protected void createProject() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("SampleAddBomComponentVersionTest Project");

        AnalysisSourceLocation sourceLocation = TestSources.getAnalysisSourceLocation(getProxy());

        projectRequest.setAnalysisSourceLocation(sourceLocation);

        ProtexServerProxy serverProxy = getProxy();

        projectId = serverProxy.getProjectApi().createProject(projectRequest, LicenseCategory.OPEN_SOURCE);

        TestSources.synchronousSourceScan(serverProxy, projectId, 1000);

        projectName = projectRequest.getName();
    }

    @Test(groups = { Tests.SOURCE_DEPENDENT_TEST })
    public void runSample() throws Exception {
        String[] args = new String[6];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = projectName;
        args[4] = "Cyclos";
        args[5] = "3.0.7";

        SampleAddBomComponentVersion.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        if (projectId != null) {
            // Make sure BOM is done
            getProxy().getBomApi().refreshBom(projectId, false, false);
            getProxy().getProjectApi().deleteProject(projectId);
        }
    }

}
