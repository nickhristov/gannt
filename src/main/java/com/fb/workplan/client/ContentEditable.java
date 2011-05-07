package com.fb.workplan.client;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.TextBoxBase;

public class ContentEditable extends TextBoxBase {

    public ContentEditable() {
        super(DOM.createElement("div"));
        getElement().setAttribute("contentEditable", "");
        addStyleName("description-editable");
    }

    public void setText(String text) {
        getElement().setInnerText(text);
    }

    public String getText() {
        return getElement().getInnerText();
    }
}
