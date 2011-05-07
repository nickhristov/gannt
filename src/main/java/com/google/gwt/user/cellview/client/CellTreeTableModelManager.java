package com.google.gwt.user.cellview.client;

import java.util.PriorityQueue;

public abstract class CellTreeTableModelManager {

    public static <T> IndentData<T> findInTree(DataNode<T> rootNode, final T needle) {
        assert rootNode != null;
        assert needle != null;

        Visitor<T> finder = new Visitor<T>() {
            public boolean visit(int row, DataNode<T> visitNode, int indentLevel) {
                return ! (needle.equals(visitNode.getValue()));
            }
        };

        IndentData<T> result = walk(rootNode, finder, true, 0, 0);
        return result.getData().getValue().equals(needle) ? result : null;
    }

    public static <T> DataNode<T> logicalInsert(DataNode<T> parentNode, T value, int offset) {
        DataNode<T> node = createNode(parentNode, value);
        if (parentNode.getFirstChild() == null) {
            parentNode.setFirstChild(node);
            return node;
        }

        DataNode<T> prev = null;
        DataNode<T> child = parentNode.getFirstChild();
        int count = 0;
        while( (offset > count || offset < 0) && child != null) {
            count++;
            prev = child;
            child = child.getNextSibling();
        }
        prev.setNextSibling(node);
        if (child != null) {
            node.setNextSibling(child);
        }
        return node;
    }

    private static <T> DataNode<T> createNode(DataNode<T> parentNode, T value) {
        DataNode<T> node = new DataNode<T>();
        node.setParent(parentNode);
        node.setValue(value);
        return node;
    }

    /**
     * Performs a depth first search on the list of nodes.
     * <p/>
     * Child node is visited prior to siblings.
     *
     * @param current
     * @param visitor        to be called for each node
     * @param renderSiblings whether the siblings should be visited or not
     * @param indentLevel
     * @param row
     * @return node at which the search terminated
     */
    public static <T> IndentData<T> walk(DataNode<T> current, Visitor<T> visitor, boolean renderSiblings, int indentLevel, int row) {
        if (current == null) {
            return null;
        }

        PriorityQueue<IndentData<T>> queue = new PriorityQueue<IndentData<T>>();

        IndentData<T> data = new IndentData<T>();
        data.setData(current);
        data.setIndentLevel(indentLevel);
        data.setRowIndex(row);

        queue.add(data);
        IndentData<T> node = null;
        while( ! queue.isEmpty()) {
            node = queue.poll();
            node.setRowIndex(row);
            boolean shouldContinue = visitor.visit(row, node.getData(), node.getIndentLevel());
            if (!shouldContinue) {
                return node;
            }

            if (node.getData().getFirstChild() != null) {
                data = new IndentData<T>();
                data.setData(node.getData().getFirstChild());
                data.setIndentLevel(node.getIndentLevel() + 1);
                queue.add(data);
            }

            if (renderSiblings && node.getData().getNextSibling() != null) {
                data = new IndentData<T>();
                data.setData(node.getData().getNextSibling());
                data.setIndentLevel(node.getIndentLevel());
                queue.add(data);
            }
            row++;
        }
        return node;
    }
}
