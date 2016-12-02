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
package com.blackducksoftware.sdk.protex.util.test;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.common.ComponentColumn;
import com.blackducksoftware.sdk.protex.common.ComponentPageFilter;
import com.blackducksoftware.sdk.protex.common.ComponentType;
import com.blackducksoftware.sdk.protex.common.FileDiscoveryPatternColumn;
import com.blackducksoftware.sdk.protex.common.FileDiscoveryPatternPageFilter;
import com.blackducksoftware.sdk.protex.common.LearnedIdentificationColumn;
import com.blackducksoftware.sdk.protex.common.LearnedIdentificationPageFilter;
import com.blackducksoftware.sdk.protex.common.SortType;
import com.blackducksoftware.sdk.protex.license.LicenseInfoColumn;
import com.blackducksoftware.sdk.protex.license.LicenseInfoPageFilter;
import com.blackducksoftware.sdk.protex.obligation.AssignedObligationColumn;
import com.blackducksoftware.sdk.protex.obligation.AssignedObligationPageFilter;
import com.blackducksoftware.sdk.protex.project.ProjectColumn;
import com.blackducksoftware.sdk.protex.project.ProjectInfoColumn;
import com.blackducksoftware.sdk.protex.project.ProjectInfoPageFilter;
import com.blackducksoftware.sdk.protex.project.ProjectPageFilter;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalComponentColumn;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalComponentPageFilter;
import com.blackducksoftware.sdk.protex.project.template.TemplateInfoColumn;
import com.blackducksoftware.sdk.protex.project.template.TemplateInfoPageFilter;
import com.blackducksoftware.sdk.protex.role.UserRoleInfoColumn;
import com.blackducksoftware.sdk.protex.role.UserRoleInfoPageFilter;
import com.blackducksoftware.sdk.protex.user.UserColumn;
import com.blackducksoftware.sdk.protex.user.UserPageFilter;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

public class PageFilterFactoryTest {

    @Test
    public void getAllRowsProject() throws Exception {
        ProjectPageFilter pageFilter = PageFilterFactory.getAllRows(ProjectColumn.PROJECT_NAME);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), ProjectColumn.PROJECT_NAME);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsProjectInfo() throws Exception {
        ProjectInfoPageFilter pageFilter = PageFilterFactory.getAllRows(ProjectInfoColumn.PROJECT_NAME);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), ProjectInfoColumn.PROJECT_NAME);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsTemplateInfo() throws Exception {
        TemplateInfoPageFilter pageFilter = PageFilterFactory.getAllRows(TemplateInfoColumn.TEMPLATE_NAME);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), TemplateInfoColumn.TEMPLATE_NAME);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsLocalComponent() throws Exception {
        LocalComponentPageFilter pageFilter = PageFilterFactory.getAllRows(LocalComponentColumn.COMPONENT_NAME);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), LocalComponentColumn.COMPONENT_NAME);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsLicenseInfo() throws Exception {
        LicenseInfoPageFilter pageFilter = PageFilterFactory.getAllRows(LicenseInfoColumn.LICENSE_NAME);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), LicenseInfoColumn.LICENSE_NAME);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsComponent() throws Exception {
        ComponentPageFilter pageFilter = PageFilterFactory.getAllRows(ComponentColumn.NAME);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), ComponentColumn.NAME);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());

        Assert.assertEquals(pageFilter.isIncludeDeprecated(), Boolean.FALSE);
        Assert.assertTrue(pageFilter.getComponentTypes().containsAll(Arrays.asList(ComponentType.values())));
    }

    @Test
    public void getAllRowsAssignedObligation() throws Exception {
        AssignedObligationPageFilter pageFilter = PageFilterFactory.getAllRows(AssignedObligationColumn.OBLIGATION_ID);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), AssignedObligationColumn.OBLIGATION_ID);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsLearnedIdentification() throws Exception {
        LearnedIdentificationPageFilter pageFilter = PageFilterFactory.getAllRows(LearnedIdentificationColumn.COMPONENT_ID);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), LearnedIdentificationColumn.COMPONENT_ID);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsFileDiscoveryPattern() throws Exception {
        FileDiscoveryPatternPageFilter pageFilter = PageFilterFactory.getAllRows(FileDiscoveryPatternColumn.FILE_TYPE);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), FileDiscoveryPatternColumn.FILE_TYPE);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsFileDiscoveryPatternWithSortType() throws Exception {
        FileDiscoveryPatternPageFilter pageFilter = PageFilterFactory.getAllRows(FileDiscoveryPatternColumn.FILE_TYPE, SortType.NUMERIC);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), FileDiscoveryPatternColumn.FILE_TYPE);
        Assert.assertEquals(pageFilter.getSortType(), SortType.NUMERIC);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsUser() throws Exception {
        UserPageFilter pageFilter = PageFilterFactory.getAllRows(UserColumn.EMAIL);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), UserColumn.EMAIL);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsUserWithSortType() throws Exception {
        UserPageFilter pageFilter = PageFilterFactory.getAllRows(UserColumn.EMAIL, SortType.NUMERIC);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), UserColumn.EMAIL);
        Assert.assertEquals(pageFilter.getSortType(), SortType.NUMERIC);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getAllRowsUserRoleInfo() throws Exception {
        UserRoleInfoPageFilter pageFilter = PageFilterFactory.getAllRows(UserRoleInfoColumn.LABEL);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(pageFilter.getSortedColumn(), UserRoleInfoColumn.LABEL);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getFirstPageComponent() throws Exception {
        ComponentPageFilter pageFilter = PageFilterFactory.getFirstPage(10, ComponentColumn.NAME, true);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(9));
        Assert.assertEquals(pageFilter.getSortedColumn(), ComponentColumn.NAME);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());

        Assert.assertEquals(pageFilter.isIncludeDeprecated(), Boolean.FALSE);
        Assert.assertTrue(pageFilter.getComponentTypes().containsAll(Arrays.asList(ComponentType.values())));
    }

    @Test
    public void getFirstPageLicenseInfo() throws Exception {
        LicenseInfoPageFilter pageFilter = PageFilterFactory.getFirstPage(10, LicenseInfoColumn.LICENSE_NAME, true);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(0));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(9));
        Assert.assertEquals(pageFilter.getSortedColumn(), LicenseInfoColumn.LICENSE_NAME);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

    @Test
    public void getNextPage() throws Exception {
        LicenseInfoPageFilter pageFilter = PageFilterFactory.getFirstPage(10, LicenseInfoColumn.LICENSE_NAME, true);
        pageFilter = PageFilterFactory.getNextPage(pageFilter);

        Assert.assertNotNull(pageFilter);
        Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(10));
        Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(19));
        Assert.assertEquals(pageFilter.getSortedColumn(), LicenseInfoColumn.LICENSE_NAME);
        Assert.assertEquals(pageFilter.getSortType(), SortType.ALPHABETICAL_CASE_INSENSITIVE);
        Assert.assertTrue(pageFilter.isSortAscending());
    }

}
