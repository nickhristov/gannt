package com.google.gwt.user.cellview.client;

class TreeContext<T> implements Comparable<TreeContext<T>> {

    public int compareTo(TreeContext<T> o) {
        return - (this.indentLevel - o.indentLevel);
    }

    DataNode<T> data;
    int indentLevel;
    int rowIndex;

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public DataNode<T> getData() {
        return data;
    }

    public void setData(DataNode<T> data) {
        this.data = data;
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
    }
}
