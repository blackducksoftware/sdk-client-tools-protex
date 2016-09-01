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

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCount;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

public class CodeTreeUtilitiesTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void constructPathNullParentPath() throws Exception {
        CodeTreeUtilities.constructPath(null, "file.txt");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void constructPathNullNodeName() throws Exception {
        CodeTreeUtilities.constructPath("folder", null);
    }

    @Test
    public void constructPathEmptyParentPath() throws Exception {
        String path = CodeTreeUtilities.constructPath("", "file.txt");

        Assert.assertEquals(path, "file.txt");
    }

    @Test
    public void constructPathNoTrailingSeparator() throws Exception {
        String path = CodeTreeUtilities.constructPath("folder", "file.txt");

        Assert.assertEquals(path, "folder/file.txt");
    }

    @Test
    public void constructPathTrailingSeparator() throws Exception {
        String path = CodeTreeUtilities.constructPath("folder/", "file.txt");

        Assert.assertEquals(path, "folder/file.txt");
    }

    @Test
    public void constructPathEmptyNodeName() throws Exception {
        String path = CodeTreeUtilities.constructPath("folder", "");

        Assert.assertEquals(path, "folder");
    }

    @Test
    public void constructPathEmptyInputs() throws Exception {
        String path = CodeTreeUtilities.constructPath("", "");

        Assert.assertEquals(path, "");
    }

    @Test
    public void getNodeCountsNullNode() throws Exception {
        Map<NodeCountType, Long> counts = CodeTreeUtilities.getNodeCountMap(null);

        Assert.assertTrue(counts.isEmpty());
    }

    @Test
    public void getNodeCountsEmptyNodeCounts() throws Exception {
        CodeTreeNode node = new CodeTreeNode();

        Map<NodeCountType, Long> counts = CodeTreeUtilities.getNodeCountMap(node);

        Assert.assertTrue(counts.isEmpty());
    }

    @Test
    public void getNodeCounts() throws Exception {
        CodeTreeNode node = new CodeTreeNode();
        NodeCount count = new NodeCount();
        count.setCount(10L);
        count.setCountType(NodeCountType.APPROVED);
        node.getNodeCounts().add(count);

        Map<NodeCountType, Long> counts = CodeTreeUtilities.getNodeCountMap(node);

        Assert.assertEquals(counts.size(), 1);
        Assert.assertEquals(counts.get(NodeCountType.APPROVED), count.getCount());
    }

}
