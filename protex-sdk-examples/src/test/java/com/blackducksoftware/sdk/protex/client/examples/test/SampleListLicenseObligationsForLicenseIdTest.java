package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleListLicenseObligationsForLicenseId;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;

public class SampleListLicenseObligationsForLicenseIdTest extends AbstractSdkSampleTest {

    @Test
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = "gpl20";

        SampleListLicenseObligationsForLicenseId.main(args);
    }

}
