package com.google.gwt.user.cellview.client;

import com.google.gwt.cell.client.Cell;

public class FlexHeader<H> extends Header<H> {

    public FlexHeader(Cell<H> cell, H value) {
        this(cell, 1, value);
    }
    /**
     * Construct a Header with a given {@link com.google.gwt.cell.client.Cell}.
     *
     * @param cell the {@link com.google.gwt.cell.client.Cell} responsible for rendering items in the header
     * @param colSpan
     */
    public FlexHeader(Cell<H> cell, int colSpan, H value) {
        super(cell);
        this.colSpan = colSpan;
        this.value = value;
    }

    public int getColSpan(){
        return colSpan;
    }

    @Override
    public H getValue() {
        return value;
    }

    private int colSpan;
    private H value;
}
