package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleModifyStandardComponentLocally;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.common.ComponentKey;

public class SampleModifyStandardComponentLocallyTest extends AbstractSdkSampleTest {

    private ComponentKey modifiedKey = null;

    @Test(groups = Tests.OPERATION_AFFECTING_TEST)
    public void runSample() throws Exception {
        modifiedKey = new ComponentKey();
        modifiedKey.setComponentId("abra20646");

        String[] args = new String[5];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = modifiedKey.getComponentId();
        args[4] = "gpl20";

        SampleModifyStandardComponentLocally.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void resetComponent() throws Exception {
        getProxy().getComponentApi().resetComponent(modifiedKey);
    }

}
