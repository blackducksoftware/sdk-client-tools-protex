package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleListUsers;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.user.UserRequest;

public class SampleListUsersTest extends AbstractSdkSampleTest {

    private String userId = null;

    @BeforeClass
    protected void createUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail("testuser@sampleunittests.com");
        userRequest.setFirstName("Firstname");
        userRequest.setLastName("Lastname");

        userId = getProxy().getUserApi().createUser(userRequest, "password");
    }

    @Test(groups = Tests.OPERATION_AFFECTING_TEST)
    public void runSample() throws Exception {
        String[] args = new String[3];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();

        SampleListUsers.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        if (userId != null) {
            getProxy().getUserApi().deleteUser(userId);
        }
    }

}
