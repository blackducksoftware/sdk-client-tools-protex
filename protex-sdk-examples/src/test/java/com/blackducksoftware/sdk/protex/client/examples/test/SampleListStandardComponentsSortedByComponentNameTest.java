package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleListStandardComponentsSortedByComponentName;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;

public class SampleListStandardComponentsSortedByComponentNameTest extends AbstractSdkSampleTest {

    @Test
    public void runSample() throws Exception {
        String[] args = new String[3];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();

        SampleListStandardComponentsSortedByComponentName.main(args);
    }

}
