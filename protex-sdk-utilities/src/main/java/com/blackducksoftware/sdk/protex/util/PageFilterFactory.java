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
package com.blackducksoftware.sdk.protex.util;

import java.util.Arrays;

import com.blackducksoftware.sdk.protex.common.ComponentColumn;
import com.blackducksoftware.sdk.protex.common.ComponentPageFilter;
import com.blackducksoftware.sdk.protex.common.ComponentType;
import com.blackducksoftware.sdk.protex.common.FileDiscoveryPatternColumn;
import com.blackducksoftware.sdk.protex.common.FileDiscoveryPatternPageFilter;
import com.blackducksoftware.sdk.protex.common.LearnedIdentificationColumn;
import com.blackducksoftware.sdk.protex.common.LearnedIdentificationPageFilter;
import com.blackducksoftware.sdk.protex.common.PageFilter;
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

/**
 * Convenience class to create and manage PageFilter objects
 *
 * PageFitler objects can be used to force particular sort orders as well as to limit the number of objects returned.
 * The main purpose of limiting the number of returned objects is performance and memory management on the client side
 */
public final class PageFilterFactory {

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of project data available
     */
    public static ProjectPageFilter getAllRows(ProjectColumn sortColumn) {
        ProjectPageFilter pageFilter = getAllRows(new ProjectPageFilter());
        pageFilter.setSortedColumn(sortColumn);
        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of project info available
     */
    public static ProjectInfoPageFilter getAllRows(ProjectInfoColumn sortColumn) {
        ProjectInfoPageFilter pageFilter = getAllRows(new ProjectInfoPageFilter());
        pageFilter.setSortedColumn(sortColumn);
        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of project template info available
     */
    public static TemplateInfoPageFilter getAllRows(TemplateInfoColumn sortColumn) {
        TemplateInfoPageFilter pageFilter = getAllRows(new TemplateInfoPageFilter());
        pageFilter.setSortedColumn(sortColumn);
        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of local component info available
     */
    public static LocalComponentPageFilter getAllRows(LocalComponentColumn sortColumn) {
        LocalComponentPageFilter pageFilter = getAllRows(new LocalComponentPageFilter());
        pageFilter.setSortedColumn(sortColumn);
        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of license info available
     */
    public static LicenseInfoPageFilter getAllRows(LicenseInfoColumn sortColumn) {
        LicenseInfoPageFilter pageFilter = getAllRows(new LicenseInfoPageFilter());
        pageFilter.setSortedColumn(sortColumn);
        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of component info available
     */
    public static ComponentPageFilter getAllRows(ComponentColumn sortColumn) {
        ComponentPageFilter pageFilter = getAllRows(new ComponentPageFilter());
        pageFilter.setSortedColumn(sortColumn);
        pageFilter.setIncludeDeprecated(false);
        pageFilter.getComponentTypes().addAll(Arrays.asList(ComponentType.values()));
        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of assigned obligation info available
     */
    public static AssignedObligationPageFilter getAllRows(AssignedObligationColumn sortColumn) {
        AssignedObligationPageFilter pageFilter = getAllRows(new AssignedObligationPageFilter());
        pageFilter.setSortedColumn(sortColumn);
        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of learned identification info available
     */
    public static LearnedIdentificationPageFilter getAllRows(LearnedIdentificationColumn sortColumn) {
        LearnedIdentificationPageFilter pageFilter = getAllRows(new LearnedIdentificationPageFilter());
        pageFilter.setSortedColumn(sortColumn);
        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of file discovery pattern info available
     */
    public static FileDiscoveryPatternPageFilter getAllRows(FileDiscoveryPatternColumn sortColumn) {
        return getAllRows(sortColumn, SortType.ALPHABETICAL_CASE_INSENSITIVE);
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @param sortType
     *            The type of sort to use
     * @return Page filter which will retrieve all rows of file discovery pattern info available
     */
    public static FileDiscoveryPatternPageFilter getAllRows(FileDiscoveryPatternColumn sortColumn, SortType sortType) {
        FileDiscoveryPatternPageFilter pageFilter = getAllRows(new FileDiscoveryPatternPageFilter(), sortType);
        pageFilter.setSortedColumn(sortColumn);
        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of user info available
     */
    public static UserPageFilter getAllRows(UserColumn sortColumn) {
        UserPageFilter pageFilter = getAllRows(new UserPageFilter());
        pageFilter.setSortedColumn(sortColumn);

        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @param sortType
     *            The type of sorting to use
     * @return Page filter which will retrieve all rows of user info available
     */
    public static UserPageFilter getAllRows(UserColumn sortColumn, SortType sortType) {
        UserPageFilter pageFilter = getAllRows(new UserPageFilter(), sortType);
        pageFilter.setSortedColumn(sortColumn);
        return pageFilter;
    }

    /**
     * @param sortColumn
     *            Column to sort results on
     * @return Page filter which will retrieve all rows of user role info available
     */
    public static UserRoleInfoPageFilter getAllRows(UserRoleInfoColumn sortColumn) {
        UserRoleInfoPageFilter pageFilter = getAllRows(new UserRoleInfoPageFilter());
        pageFilter.setSortedColumn(sortColumn);
        return pageFilter;
    }

    /**
     * Gets a page filter for the first page of component data
     *
     * @param pageSize
     *            The size of the page to retrieve
     * @param sortedColumn
     *            The column to sort the returned info on
     * @param sortAssending
     *            True if results should be sorted in ascending order, false for descending
     * @return A page filter for the first page of component data
     */
    public static ComponentPageFilter getFirstPage(int pageSize, ComponentColumn sortedColumn, boolean sortAssending) {
        ComponentPageFilter firstPage = getFirstPage(new ComponentPageFilter(), pageSize, sortAssending);
        firstPage.setSortedColumn(sortedColumn);
        firstPage.setIncludeDeprecated(false);
        firstPage.getComponentTypes().addAll(Arrays.asList(ComponentType.values()));
        return firstPage;
    }

    /**
     * Gets a page filter for the first page of license info data
     *
     * @param pageSize
     *            The size of the page to retrieve
     * @param sortedColumn
     *            The column to sort the returned info on
     * @param sortAssending
     *            True if results should be sorted in ascending order, false for descending
     * @return A page filter for the first page of license info data
     */
    public static LicenseInfoPageFilter getFirstPage(int pageSize, LicenseInfoColumn sortedColumn, boolean sortAssending) {
        LicenseInfoPageFilter firstPage = getFirstPage(new LicenseInfoPageFilter(), pageSize, sortAssending);
        firstPage.setSortedColumn(sortedColumn);
        return firstPage;
    }

    /**
     * Advances the requested indexes of a page filter to be from the current last index to the next index which will
     * generate a page of the same size as the current page
     *
     * @param pageFilter
     *            The page filter to advance
     * @return The advanced page filter
     * @param <T>
     *            The type representing the page filter
     */
    public static <T extends PageFilter> T getNextPage(T pageFilter) {
    	int startIndex = pageFilter.getFirstRowIndex();
    	int endIndex = pageFilter.getLastRowIndex();   	
    	
        int pageSize = Math.max(1, endIndex - startIndex + 1);

        pageFilter.setFirstRowIndex(endIndex + 1);
        pageFilter.setLastRowIndex(endIndex + pageSize);

        return pageFilter;
    }

    /**
     * Applies settings to a page filter which cause it to retrieve the maximum number of rows
     *
     * @param pageFilter
     *            The page filter to apply settings to
     * @return A page filter designed to return all rows
     */
    private static <T extends PageFilter> T getAllRows(T pageFilter) {
        return getAllRows(pageFilter, SortType.ALPHABETICAL_CASE_INSENSITIVE);
    }

    /**
     * Applies settings to a page filter which cause it to retrieve the maximum number of rows
     *
     * @param pageFilter
     *            The page filter to apply settings to
     * @param sortType
     *            The type of sorting to do
     * @return A page filter designed to return all rows
     */
    private static <T extends PageFilter> T getAllRows(T pageFilter, SortType sortType) {
        pageFilter.setFirstRowIndex(0);
        pageFilter.setLastRowIndex(Integer.MAX_VALUE);
        pageFilter.setSortAscending(Boolean.TRUE);
        pageFilter.setSortType(sortType);
        return pageFilter;
    }

    /**
     * Applies settings to a page filter which will cause it return values at indexes 0 through {@code pageSize}
     *
     * @param pageFilter
     *            The page filter to apply values to
     * @param pageSize
     *            The size of the page to retrieve
     * @param sortAssending
     *            True if returns should be sorted in ascending order, false otherwise
     * @return A page filter with the specified ascending property and indexes of 0, {@code pageSize}
     */
    private static <T extends PageFilter> T getFirstPage(T pageFilter, int pageSize, boolean sortAssending) {
        pageFilter.setFirstRowIndex(0);
        pageFilter.setLastRowIndex(pageSize-1);
        pageFilter.setSortAscending(sortAssending);
        pageFilter.setSortType(SortType.ALPHABETICAL_CASE_INSENSITIVE);

        return pageFilter;
    }

}
