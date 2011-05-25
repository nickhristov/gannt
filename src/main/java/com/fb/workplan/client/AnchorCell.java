package com.fb.workplan.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class AnchorCell extends AbstractCell<String> {

	public AnchorCell() {
		super("click", "mousedown");
	}

	interface Template extends SafeHtmlTemplates {
		@Template("<a>{0}</a>")
		SafeHtml anchor(String label);
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		sb.append(template.anchor(value));
	}

	HandlerManager manager = new HandlerManager(this);
	Template template = GWT.create(Template.class);

	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		return manager.addHandler(MouseDownEvent.getType(), handler);
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		if (event.getType().equals("mousedown")) {
			DomEvent.fireNativeEvent(event, manager);
		}
	}
}
