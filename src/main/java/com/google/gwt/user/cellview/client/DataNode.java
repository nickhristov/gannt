package com.google.gwt.user.cellview.client;

/**
 * TreeNode interface is horrendeous, so it is unused. Duh.
 * @param <T>
 */
class DataNode<T> {

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public DataNode<T> getNextSibling() {
        return nextSibling;
    }

    public void setNextSibling(DataNode<T> nextSibling) {
        this.nextSibling = nextSibling;
    }

    public DataNode<T> getFirstChild() {
        return firstChild;
    }

    public void setFirstChild(DataNode<T> firstChild) {
        this.firstChild = firstChild;
    }

    public DataNode<T> getParent() {
        return parent;
    }

    public void setParent(DataNode<T> parent) {
        this.parent = parent;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    T value;
    DataNode<T> nextSibling;
    DataNode<T> firstChild;
    DataNode<T> parent;
    boolean expanded = true;

    @Override
    public boolean equals(Object ob) {
        if (! (ob instanceof DataNode)) {
            return false;
        }
        DataNode<T> other = (DataNode<T>) ob;
        return other.value != null && other.value.equals(value);
    }
}
