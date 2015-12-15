package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleListUsersSortedByLastName;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;

public class SampleListUsersSortedByLastNameTest extends AbstractSdkSampleTest {

    @Test
    public void runSample() throws Exception {
        String[] args = new String[3];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();

        SampleListUsersSortedByLastName.main(args);
    }

}
