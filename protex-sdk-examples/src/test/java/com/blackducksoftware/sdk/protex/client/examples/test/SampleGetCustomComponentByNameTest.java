package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleGetCustomComponentByName;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.component.ComponentRequest;

public class SampleGetCustomComponentByNameTest extends AbstractSdkSampleTest {

    private String componentName;

    private ComponentKey componentKey;

    @BeforeClass
    protected void createComponent() throws Exception {
        ComponentRequest componentRequest = new ComponentRequest();
        componentRequest.setComponentName("SampleGetCustomComponentByNameTest Component");
        componentRequest.getLicenseIds().add("gpl20");

        componentKey = getProxy().getComponentApi().createComponent(componentRequest);
        componentName = componentRequest.getComponentName();
    }

    @Test(groups = Tests.OPERATION_AFFECTING_TEST)
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = componentName;

        SampleGetCustomComponentByName.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteComponent() throws Exception {
        if (componentKey != null) {
            getProxy().getComponentApi().deleteComponent(componentKey);
        }
    }


}
