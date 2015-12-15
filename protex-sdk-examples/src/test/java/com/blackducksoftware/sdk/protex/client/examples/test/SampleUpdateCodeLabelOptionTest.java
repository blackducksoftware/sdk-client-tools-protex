package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleUpdateCodeLabelOption;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.common.CodeLabelOption;

public class SampleUpdateCodeLabelOptionTest extends AbstractSdkSampleTest {

    private CodeLabelOption originalOption;

    @BeforeClass
    protected void storeOriginalOption() throws Exception {
        originalOption = getProxy().getPolicyApi().getCodeLabelOption();
    }

    @Test(groups = Tests.OPERATION_AFFECTING_TEST)
    public void runSample() throws Exception {
        String[] args = new String[5];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = "http://blackducksoftware.com";
        args[4] = "BlackDuck SDK Sample Test";

        SampleUpdateCodeLabelOption.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void restoreOption() throws Exception {
        if (originalOption != null) {
            getProxy().getPolicyApi().updateCodeLabelOption(originalOption);
        }
    }

}
