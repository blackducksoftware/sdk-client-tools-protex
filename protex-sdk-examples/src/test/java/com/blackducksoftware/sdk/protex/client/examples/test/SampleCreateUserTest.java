package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleCreateUser;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;

public class SampleCreateUserTest extends AbstractSdkSampleTest {

    private String email = "testuser@sampleunittests.com";

    @Test(groups = { Tests.OPERATION_AFFECTING_TEST })
    public void runSample() throws Exception {
        String[] args = new String[6];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = email;
        args[4] = "Firstname";
        args[5] = "Lastname";

        SampleCreateUser.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        getProxy().getUserApi().deleteUser(email);
    }

}
