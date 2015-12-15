package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleCreateLicenseWithAddedObligations;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.license.GlobalLicense;

public class SampleCreateLicenseWithAddedObligationsTest extends AbstractSdkSampleTest {

    private String licenseName = "SampleCreateLicenseWithAddedObligations License";

    @Test(groups = { Tests.OPERATION_AFFECTING_TEST })
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = licenseName;

        SampleCreateLicenseWithAddedObligations.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteLicense() throws Exception {
        GlobalLicense license = getProxy().getLicenseApi().getLicenseByName(licenseName);
        getProxy().getLicenseApi().deleteLicense(license.getLicenseId());
    }

}
