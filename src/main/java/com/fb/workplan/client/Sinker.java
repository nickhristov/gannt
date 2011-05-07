package com.fb.workplan.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

public class Sinker extends Widget {
    Element me;
    public Sinker() {
        super();
        me = DOM.createElement("div");
        setElement(me);
        DOM.sinkEvents(me, Event.ONMOUSEDOWN);
        me.getStyle().setHeight(200, Style.Unit.PX);
        me.getStyle().setWidth(200, Style.Unit.PX);
        me.getStyle().setBackgroundColor("black");
    }

    public void onBrowserEvent(Event event) {
        GWT.log("caught event: " + event.getType());
    }
}
