package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.SampleAnalyzeProjectAndGetCodeTree;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.TestSources;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.Project;

public class SampleAnalyzeProjectAndGetCodeTreeTest extends AbstractSdkSampleTest {

    private String projectName = "SampleAnalyzeProjectAndGetCodeTreeTest Project";

    @Test(groups = { Tests.SOURCE_DEPENDENT_TEST })
    public void runSample() throws Exception {
        AnalysisSourceLocation sourceLocation = TestSources.getAnalysisSourceLocation(getProxy());

        String[] args = new String[5];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = projectName;
        args[4] = sourceLocation.getSourcePath();

        SampleAnalyzeProjectAndGetCodeTree.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void cleanupProject() {
        try {
            Project project = getProxy().getProjectApi().getProjectByName(projectName);

            if (project != null) {
                getProxy().getProjectApi().deleteProject(project.getProjectId());
            }
        } catch (SdkFault fault) {
            fault.printStackTrace(System.err);
        }
    }
}
