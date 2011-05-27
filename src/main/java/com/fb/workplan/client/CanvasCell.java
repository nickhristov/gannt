package com.fb.workplan.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;


/**
 * A cell which renders its contents via a canvas element. Actual rendering is
 * done via the supplied renderer. Cell containers such as CellTree or CellTreeTable
 * MUST call setValue for the cell to render the contents of the canvas element.
 */
public class CanvasCell<C> extends AbstractCell<C> {

	interface Template extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<canvas></canvas>")
		SafeHtml standard();
	}

	Template template = GWT.create(Template.class);

	public CanvasCell(CanvasCellRenderer<C> renderer) {
		this.renderer = renderer;
	}

	@Override
	public void render(Context context, C value, SafeHtmlBuilder sb) {
		sb.append(template.standard());
	}

	public void setValue(Context context, Element parent, C value) {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		render(context, value, sb);
		parent.setInnerHTML(sb.toSafeHtml().asString());

		CanvasElement canvasElement = parent.getFirstChildElement().cast();
		renderer.render(canvasElement, value, context, parent);
	}

	private final CanvasCellRenderer<C> renderer;
}
