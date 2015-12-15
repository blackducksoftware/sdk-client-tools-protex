package com.blackducksoftware.sdk.protex.client.examples.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.client.examples.SampleCreateReportTemplate;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.report.ReportTemplate;

public class SampleCreateReportTemplateTest extends AbstractSdkSampleTest {

    private String templateName = "SampleCreateReportTemplateTest Template";

    @Test(groups = { Tests.OPERATION_AFFECTING_TEST })
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = templateName;

        SampleCreateReportTemplate.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        ReportTemplate template = getProxy().getReportApi().getReportTemplateByTitle(templateName);
        getProxy().getReportApi().deleteReportTemplate(template.getReportTemplateId());
    }

}
