package com.google.gwt.user.cellview.client;

public interface Visitor<T> {
    /**
     * TODO: pass a generic context object instead of a indent level and row index
     * Called when traversing each node of the tree.
     *
     * @param row         corresponding row index in tree (or index of traversal)
     * @param visitNode   node that is visited
     * @param indentLevel an indentation level which is carried by the parent
     * @return true if traversal should continue, false otherwise
     */
    boolean visit(int row, DataNode<T> visitNode, int indentLevel);
}