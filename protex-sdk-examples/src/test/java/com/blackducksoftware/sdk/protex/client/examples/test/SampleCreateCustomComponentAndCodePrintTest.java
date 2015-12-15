package com.blackducksoftware.sdk.protex.client.examples.test;

import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.SampleCreateCustomComponentAndCodePrint;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.TestSources;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.common.ComponentType;
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;

public class SampleCreateCustomComponentAndCodePrintTest extends AbstractSdkSampleTest {

    private String componentName = "SampleCreateCustomComponentAndCodePrintTest Component";

    @Test(groups = { Tests.OPERATION_AFFECTING_TEST, Tests.SOURCE_DEPENDENT_TEST })
    public void runSample() throws Exception {
        AnalysisSourceLocation sourceLocation = TestSources.getAnalysisSourceLocation(getProxy());

        String[] args = new String[5];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = componentName;
        args[4] = sourceLocation.getSourcePath();

        SampleCreateCustomComponentAndCodePrint.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void cleanupComponent() {
        try {
            List<Component> components = getProxy().getComponentApi().getComponentsByName(componentName, null);

            if (components != null) {
                for (Component component : components) {
                    if (ComponentType.CUSTOM.equals(component.getComponentType())) {
                        getProxy().getComponentApi().deleteComponent(component.getComponentKey());
                    }
                }
            }
        } catch (SdkFault fault) {
            fault.printStackTrace(System.err);
        }
    }
}
