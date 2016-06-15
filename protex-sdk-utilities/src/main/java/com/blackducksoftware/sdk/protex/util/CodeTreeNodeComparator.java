package com.blackducksoftware.sdk.protex.util;

import java.util.Comparator;

import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;

public class CodeTreeNodeComparator implements Comparator<CodeTreeNode> {

    public static Comparator<CodeTreeNode> getInstance() {
        return INSTANCE;
    }

    private static final Comparator<CodeTreeNode> INSTANCE = new CodeTreeNodeComparator();

    protected CodeTreeNodeComparator() {
    }

    protected static String preprocess(CodeTreeNode node) {
        return (node.getNodeType() != CodeTreeNodeType.FILE ? node.getName() + "/" : node.getName()).replaceAll("([^/]*)/+", "\0$1/");
    }

    @Override
    public int compare(CodeTreeNode node1, CodeTreeNode node2) {
        String str1 = preprocess(node1);
        String str2 = preprocess(node2);
        return str1.compareToIgnoreCase(str2);
    }

}
