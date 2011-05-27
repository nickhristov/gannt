package com.fb.workplan.client;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Element;

public interface CanvasCellRenderer<C> {
	void render(CanvasElement canvasElement, C value, Cell.Context context, Element parent);

}
