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

import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleManageTags;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;

public class SampleManageTagsTest extends AbstractSdkSampleTest {

    private String projectId, projectName, licenseId;

    @BeforeClass
    protected void createProject() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("SampleManageTagsTest Project");
        projectRequest.setLicenseId("gpl20");

        projectId = getProxy().getProjectApi().createProject(projectRequest, null);

        projectName = projectRequest.getName();
        licenseId = projectRequest.getLicenseId();
    }

    @Test(groups = Tests.OPERATION_AFFECTING_TEST)
    public void runSample() throws Exception {
        String[] args = new String[6];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = projectName;
        args[4] = "Cyclos";

        SampleManageTags.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        if (licenseId != null) {
            getProxy().getLicenseApi().removeTag(Tests.getServerUsername(), licenseId, "SampleGeneratedTag");
        }
        if (projectId != null) {
            getProxy().getProjectApi().deleteProject(projectId);
        }

        List<Component> components = getProxy().getComponentApi().getComponentsByName("Cyclos", null);
        for (Component component : components) {
            getProxy().getComponentApi().removeTag(Tests.getServerUsername(), component.getComponentKey().getComponentId(), "SampleGeneratedTag");
        }
    }

}
