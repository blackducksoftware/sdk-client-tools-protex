package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleCreateGlobalRapidIdConfiguration;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.common.RapidIdConfiguration;

public class SampleCreateGlobalRapidIdConfigurationTest extends AbstractSdkSampleTest {

    private String globalIdName = "SampleCreateGlobalRapidIdConfigurationTest Config";

    @Test(groups = { Tests.OPERATION_AFFECTING_TEST })
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = globalIdName;

        SampleCreateGlobalRapidIdConfiguration.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        RapidIdConfiguration configuration = getProxy().getPolicyApi().getRapidIdConfigurationByName(globalIdName);
        getProxy().getPolicyApi().deleteRapidIdConfiguration(configuration.getConfigurationId());
    }

}
