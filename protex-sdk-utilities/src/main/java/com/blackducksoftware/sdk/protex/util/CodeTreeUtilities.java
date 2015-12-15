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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCount;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;

/**
 * Utilities for manipulation code tree node input and output
 */
public class CodeTreeUtilities {

    /**
     * Value which, when used as input to
     * {@link com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest#setDepth(Integer)}, will result in
     * the all children of the path requested being returned
     */
    public static final Integer INFINITE_DEPTH = -1;

    /**
     * Value which, when used as input to
     * {@link com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest#setDepth(Integer)}, will result in
     * the path requested being returned
     */
    public static final Integer SINGLE_NODE = 0;

    /**
     * Value which, when used as input to
     * {@link com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest#setDepth(Integer)}, will result in
     * the direct children of the path requested being returned
     */
    public static final Integer DIRECT_CHILDREN = 1;

    /**
     * A list of all the available types of code tree nodes (file, folder, etc)
     */
    public static final List<CodeTreeNodeType> ALL_CODE_TREE_NODE_TYPES;

    /**
     * A code tree node request which will retrieve all the code tree nodes in the project
     */
    public static final CodeTreeNodeRequest ALL_NODES_PARAMETERS;

    /**
     * File Separator for CodeTree paths. This value is NOT OS dependent!
     */
    private static final String PATH_SEPARATOR = "/";

    static {
        ALL_CODE_TREE_NODE_TYPES = Arrays.asList(CodeTreeNodeType.values());

        ALL_NODES_PARAMETERS = new CodeTreeNodeRequest();
        ALL_NODES_PARAMETERS.setDepth(INFINITE_DEPTH);
        ALL_NODES_PARAMETERS.setIncludeParentNode(true);
        ALL_NODES_PARAMETERS.getIncludedNodeTypes().addAll(ALL_CODE_TREE_NODE_TYPES);
    }

    /**
     * Constructs a safe path, avoiding double file separators &quot;/&quot;
     *
     * @param parentPath
     *            The parentPath, i.e. &quot;/&quot;, &quot;/folder1&quot;
     * @param nodeName
     *            Name of a file, folder or expanded archive within the folder indicated by the parent path
     * @return The concatenated path
     */
    public static String constructPath(String parentPath, String nodeName) {
        String p = ((parentPath.isEmpty()) || (parentPath.endsWith(PATH_SEPARATOR) || (nodeName.isEmpty())) ? parentPath
                : parentPath + PATH_SEPARATOR) + nodeName;
        return p;
    }

    /**
     * Extracts count information into a more random-access friendly form
     *
     * @param node
     *            A code tree node with count information
     * @return A map relating each count type to the returned count
     */
    public static Map<NodeCountType, Long> getNodeCountMap(CodeTreeNode node) {
        Map<NodeCountType, Long> counts = new HashMap<NodeCountType, Long>();

        if (node != null && node.getNodeCounts() != null) {
            for (NodeCount count : node.getNodeCounts()) {
                counts.put(count.getCountType(), count.getCount());
            }
        }

        return counts;
    }
}
