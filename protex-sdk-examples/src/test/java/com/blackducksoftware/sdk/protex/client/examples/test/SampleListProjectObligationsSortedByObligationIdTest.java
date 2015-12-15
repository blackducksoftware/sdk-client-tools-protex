package com.blackducksoftware.sdk.protex.client.examples.test;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleListProjectObligationsSortedByObligationId;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.obligation.AssignedObligationRequest;
import com.blackducksoftware.sdk.protex.obligation.ObligationCategory;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;

public class SampleListProjectObligationsSortedByObligationIdTest extends AbstractSdkSampleTest {

    private String projectId;

    @BeforeClass
    protected void createProject() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("SampleGetProjectStatusTest Project");

        projectId = getProxy().getProjectApi().createProject(projectRequest, LicenseCategory.OPEN_SOURCE);

        List<ObligationCategory> categories = getProxy().getObligationApi().suggestObligationCategories("");
        Assert.assertNotNull(categories);
        Assert.assertFalse(categories.isEmpty());

        // Add an obligation to give the sample something to print
        AssignedObligationRequest obligationRequest = new AssignedObligationRequest();
        obligationRequest.setDescription("Test assigned obligation");
        obligationRequest.setFulfilled(false);
        obligationRequest.setName("Test Asssigned Obligation");
        obligationRequest.setReviewAndReport(true);
        obligationRequest.setObligationCategoryId(categories.get(0).getObligationCategoryId());

        getProxy().getProjectApi().addProjectObligation(projectId, obligationRequest);
    }

    @Test
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = projectId;

        SampleListProjectObligationsSortedByObligationId.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        if (projectId != null) {
            getProxy().getProjectApi().deleteProject(projectId);
        }
    }

}
