package com.google.gwt.user.cellview.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ContentEditableCell extends AbstractCell<String> {

	public String getTextFromDiv(Element parent) {
		return parent.getFirstChildElement().getInnerText();
	}

	interface Template extends SafeHtmlTemplates {
        @Template("<div class=\"ContentEditableCellInline\" contenteditable=\"\">{0}</div>")
        SafeHtml inline(String value);

		@Template("<div class=\"ContentEditableCellBlock\" contenteditable=\"\">{0}</div>")
		SafeHtml block(String value);

    }

    private static Template template = null;

    private boolean inFocus = false;
    
    public ContentEditableCell(boolean inline) {
        super("focus", "blur", "keydown", "keyup", "keypress");
		this.inline = inline;
		if (template == null) {
            template = GWT.create(Template.class);
        }
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
		if (inline) {
			sb.append(template.inline(value));
		} else {
			sb.append(template.block(value));
		}
    }

    public void onBrowserEvent(Context context, Element parent, String value,
                               NativeEvent event, ValueUpdater<String> valueUpdater) {
        if (event.getType().equals("focus")) {
            inFocus = true;
            return;
        }

        if (event.getType().equals("blur")) {
            inFocus = false;
			String newValue = getTextFromDiv(parent);
			valueUpdater.update(newValue);
            return;
        }

		if (event.getType().equals("keyup") && KeyCodes.KEY_ENTER == event.getKeyCode()) {
			String newValue = getTextFromDiv(parent);
			valueUpdater.update(newValue);
			event.preventDefault();
			event.stopPropagation();
		}
    }

	private final boolean inline;
}
