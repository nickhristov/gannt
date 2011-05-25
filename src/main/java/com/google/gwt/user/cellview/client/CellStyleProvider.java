package com.google.gwt.user.cellview.client;

public interface CellStyleProvider<T> {
    String[] getStyleNames(T data, Column<T, ?> column);
}
