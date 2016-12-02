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

import com.blackducksoftware.sdk.protex.client.examples.SampleGetGlobalRapidIdConfigurations;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.common.ComponentVersionPreference;
import com.blackducksoftware.sdk.protex.common.PrimaryMatchOperation;
import com.blackducksoftware.sdk.protex.common.RapidIdConfigurationRequest;
import com.blackducksoftware.sdk.protex.common.SingleComponentCodeMatchOperation;

public class SampleGetGlobalRapidIdConfigurationsTest extends AbstractSdkSampleTest {

    private Long configId;

    @BeforeClass
    protected void createConfiguration() throws Exception {
        RapidIdConfigurationRequest rapidIdConfigurationRequest = new RapidIdConfigurationRequest();
        rapidIdConfigurationRequest.setName("Test Sample Config");
        rapidIdConfigurationRequest.setDescription("Some Rapid ID configuration Example with PrimaryMatch operation and Single Component Code Match Operation");

        // order matters here and only one of each kind of operations is allowed
        PrimaryMatchOperation primaryMatchOperationRequest = new PrimaryMatchOperation();
        rapidIdConfigurationRequest.getOperations().add(primaryMatchOperationRequest);
        SingleComponentCodeMatchOperation singleComponentCodeMatchOperation = new SingleComponentCodeMatchOperation();
        singleComponentCodeMatchOperation.setComponentVersionPreference(ComponentVersionPreference.LATEST_RELEASE);
        singleComponentCodeMatchOperation.setMinimumMatchPercentage(85);
        rapidIdConfigurationRequest.getOperations().add(singleComponentCodeMatchOperation);

        configId = getProxy().getPolicyApi().createRapidIdConfiguration(rapidIdConfigurationRequest);
    }

    @Test(groups = Tests.OPERATION_AFFECTING_TEST)
    public void runSample() throws Exception {
        String[] args = new String[3];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();

        SampleGetGlobalRapidIdConfigurations.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        if (configId != null) {
            getProxy().getPolicyApi().deleteRapidIdConfiguration(configId);
        }
    }

}
