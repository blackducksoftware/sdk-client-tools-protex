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

import com.blackducksoftware.sdk.protex.client.examples.SampleSuggestReportTemplates;
import com.blackducksoftware.sdk.protex.client.examples.test.type.AbstractSdkSampleTest;
import com.blackducksoftware.sdk.protex.client.examples.test.type.Tests;
import com.blackducksoftware.sdk.protex.common.ExternalDocumentLink;
import com.blackducksoftware.sdk.protex.report.ReportSection;
import com.blackducksoftware.sdk.protex.report.ReportSectionType;
import com.blackducksoftware.sdk.protex.report.ReportTemplateRequest;

public class SampleSuggestReportTemplatesTest extends AbstractSdkSampleTest {

    private String templateId;

    @BeforeClass
    protected void createProject() throws Exception {
        ReportTemplateRequest templateRequest = createSummaryReportTemplateRequest();
        templateId = getProxy().getReportApi().createReportTemplate(templateRequest);
    }

    private ReportTemplateRequest createSummaryReportTemplateRequest() {
        ReportTemplateRequest reportTemplate = new ReportTemplateRequest();
        // reportTemplate.setName("Sample Report Name");
        reportTemplate.setTitle("Sample Report");
        reportTemplate.setName("Sample Report");
        reportTemplate.setComment("My Report comments");
        reportTemplate.setForced(Boolean.TRUE);

        ExternalDocumentLink externalDocumentLink1 = new ExternalDocumentLink();

        // externalDocumentLink1.setDocumentId(null);
        externalDocumentLink1.setDocumentLocation("http://www.example.com/no/doc/here.html");
        externalDocumentLink1.setLinkText("Some Text");

        reportTemplate.getExternalDocumentLinks().add(externalDocumentLink1);

        ReportSection section1 = new ReportSection();
        section1.setLabel("Summary123");
        section1.setSectionType(ReportSectionType.SUMMARY);

        ReportSection section2 = new ReportSection();
        section2.setLabel("Bill of Materials123");
        section2.setSectionType(ReportSectionType.BILL_OF_MATERIALS);

        ReportSection section3 = new ReportSection();
        section3.setLabel("Code Matches All 123");
        section3.setSectionType(ReportSectionType.CODE_MATCHES_ALL);

        reportTemplate.getSections().add(section1);
        reportTemplate.getSections().add(section2);
        reportTemplate.getSections().add(section3);

        return reportTemplate;
    }

    @Test(groups = Tests.OPERATION_AFFECTING_TEST)
    public void runSample() throws Exception {
        String[] args = new String[4];
        args[0] = Tests.getServerUrl();
        args[1] = Tests.getServerUsername();
        args[2] = Tests.getServerPassword();
        args[3] = "Samp";

        SampleSuggestReportTemplates.main(args);
    }

    @AfterClass(alwaysRun = true)
    protected void deleteProject() throws Exception {
        if (templateId != null) {
            getProxy().getReportApi().deleteReportTemplate(templateId);
        }
    }

}
