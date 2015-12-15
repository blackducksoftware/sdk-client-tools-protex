package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleBillOfMaterial;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.TestSources;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.common.BomRefreshMode;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;
import com.blackducksoftware.sdk.protex.project.bom.BomComponentRequest;

public class SampleBillOfMaterialTest extends AbstractSdkSampleTest {

    private String projectId;

    @BeforeClass
    protected void createProject() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("SampleAddBomComponentVersionTest Project");

        AnalysisSourceLocation sourceLocation = TestSources.getAnalysisSourceLocation(getProxy());

        projectRequest.setAnalysisSourceLocation(sourceLocation);

        projectId = getProxy().getProjectApi().createProject(projectRequest, LicenseCategory.OPEN_SOURCE);

        TestSources.synchronousSourceScan(getProxy(), projectId, 1000);

        Component component = getProxy().getComponentApi().getComponentsByName("Cyclos", "3.0.7").get(0);

        // Add a BOM component
        BomComponentRequest componentRequest = new BomComponentRequest();
        componentRequest.setComponentKey(component.getComponentKey());
        componentRequest.setLicenseId(component.getPrimaryLicenseId());
        componentRequest.setUsageLevel(UsageLevel.DEVELOPMENT_TOOL);

        getProxy().getBomApi().addBomComponent(projectId, componentRequest, BomRefreshMode.SYNCHRONOUS);
    }

    @Test(groups = { Tests.SOURCE_DEPENDENT_TEST })
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = projectId;

        SampleBillOfMaterial.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        if (projectId != null) {
            getProxy().getProjectApi().deleteProject(projectId);
        }
    }

}
