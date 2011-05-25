package com.fb.workplan.client;

import java.util.List;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class FixedCompositeCell<C> extends CompositeCell<C> {

	public FixedCompositeCell(List<HasCell<C, ?>> hasCells) {
		super(hasCells);
	}

	@Override
	public void setValue(Context context, Element parent, C object) {
		Element container = getContainerElement(parent);
		Element curChild = getContainerElement(parent).getFirstChildElement();
		if (curChild == null) {
			SafeHtmlBuilder builder = new SafeHtmlBuilder();
			render(context, object, builder);
			container.setInnerHTML(builder.toSafeHtml().asString());
		} else {
			super.setValue(context, parent, object);
		}
	}
}
