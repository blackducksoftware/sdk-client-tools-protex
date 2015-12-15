package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleGetAuditTrail;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.TestSources;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.common.BomRefreshMode;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.common.ForcibleBooleanOption;
import com.blackducksoftware.sdk.protex.common.IdentificationOptions;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationRequest;

public class SampleGetAuditTrailTest extends AbstractSdkSampleTest {

    private String projectId;

    @BeforeClass
    protected void createProject() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("SampleGetAuditTrailTest Project");

        AnalysisSourceLocation sourceLocation = TestSources.getAnalysisSourceLocation(getProxy());

        projectRequest.setAnalysisSourceLocation(sourceLocation);

        IdentificationOptions options = projectRequest.getIdentificationOptions();
        options = (options != null ? options : new IdentificationOptions());

        if (options.getAuditTrailOption() == null) {
            options.setAuditTrailOption(new ForcibleBooleanOption());
        }

        options.getAuditTrailOption().setOption(true);

        projectRequest.setIdentificationOptions(options);

        projectId = getProxy().getProjectApi().createProject(projectRequest, LicenseCategory.OPEN_SOURCE);

        TestSources.synchronousSourceScan(getProxy(), projectId, 1000);

        IdentificationRequest identificationRequest = new IdentificationRequest();
        ComponentKey abraGeneric = new ComponentKey();
        abraGeneric.setComponentId("abra20646");
        identificationRequest.setIdentifiedComponentKey(abraGeneric);
        identificationRequest.setIdentifiedUsageLevel(UsageLevel.COMPONENT);

        getProxy().getIdentificationApi().addDeclaredIdentification(projectId, "/", identificationRequest, BomRefreshMode.SYNCHRONOUS);
    }

    @Test(groups = { Tests.SOURCE_DEPENDENT_TEST })
    public void runSample() throws Exception {
        String[] args = new String[6];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = projectId;

        SampleGetAuditTrail.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        if (projectId != null) {
            getProxy().getProjectApi().deleteProject(projectId);
        }
    }

}
