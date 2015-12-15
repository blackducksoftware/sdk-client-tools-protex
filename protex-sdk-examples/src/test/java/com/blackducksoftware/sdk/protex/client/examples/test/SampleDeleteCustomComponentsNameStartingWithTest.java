package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleDeleteCustomComponentsNameStartingWith;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.component.ComponentRequest;

public class SampleDeleteCustomComponentsNameStartingWithTest extends AbstractSdkSampleTest {

    private String componentName;

    @BeforeClass
    protected void createProject() throws Exception {
        ComponentRequest componentRequest = new ComponentRequest();
        componentRequest.setComponentName("SampleDeleteCustomComponentsNameStartingWith Component");
        componentRequest.getLicenseIds().add("gpl20");

        getProxy().getComponentApi().createComponent(componentRequest);
        componentName = componentRequest.getComponentName();
    }

    @Test(groups = Tests.OPERATION_AFFECTING_TEST)
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = componentName.substring(0, componentName.length() - 1);

        SampleDeleteCustomComponentsNameStartingWith.main(args);
    }

}
