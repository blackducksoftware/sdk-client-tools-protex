/*
 * Black Duck Software Suite SDK
 * Copyright (C) 2015  Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleWorkWithTwoUsers;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.TestSources;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;
import com.blackducksoftware.sdk.protex.user.UserRequest;

public class SampleWorkWithTwoUsersTest extends AbstractSdkSampleTest {

    private String projectId1, projectId2, userId = null;

    @BeforeClass
    protected void createUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail("testuser@sampleunittests.com");
        userRequest.setFirstName("Firstname");
        userRequest.setLastName("Lastname");

        userId = getProxy().getUserApi().createUser(userRequest, "password");

        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("SampleWorkWithTwoUsersTest1 Project");

        AnalysisSourceLocation sourceLocation = TestSources.getAnalysisSourceLocation(getProxy());

        projectRequest.setAnalysisSourceLocation(sourceLocation);

        projectId1 = getProxy().getProjectApi().createProject(projectRequest, LicenseCategory.OPEN_SOURCE);

        TestSources.synchronousSourceScan(getProxy(), projectId1, 1000);

        projectRequest.setName("SampleWorkWithTwoUsersTest2 Project");

        projectId2 = getProxy().getProjectApi().createProject(projectRequest, LicenseCategory.OPEN_SOURCE);

        TestSources.synchronousSourceScan(getProxy(), projectId2, 1000);

        getProxy().getProjectApi().addProjectUser(projectId2, userId);
    }

    @Test(groups = { Tests.OPERATION_AFFECTING_TEST, Tests.SOURCE_DEPENDENT_TEST })
    public void runSample() throws Exception {
        String[] args = new String[5];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = userId;
        args[4] = "password";

        SampleWorkWithTwoUsers.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        if (userId != null) {
            getProxy().getUserApi().deleteUser(userId);
        }
        if (projectId1 != null) {
            getProxy().getProjectApi().deleteProject(projectId1);
        }
        if (projectId2 != null) {
            getProxy().getProjectApi().deleteProject(projectId2);
        }
    }

}
