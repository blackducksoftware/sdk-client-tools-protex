package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleDeleteLicenseById;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.license.GlobalLicenseRequest;

public class SampleDeleteLicenseByIdTest extends AbstractSdkSampleTest {

    private String licenseId;

    @BeforeClass
    protected void createLicense() throws Exception {
        GlobalLicenseRequest licenseRequest = new GlobalLicenseRequest();
        licenseRequest.setName("SampleDeleteLicenseByIdTest License");
        licenseRequest.setText("Test license text".getBytes());

        licenseId = getProxy().getLicenseApi().createLicense(licenseRequest);
    }

    @Test(groups = Tests.OPERATION_AFFECTING_TEST)
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = licenseId;

        SampleDeleteLicenseById.main(args);
    }

}
