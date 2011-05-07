package com.fb.workplan.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class GanntFloaterWidget implements GanntFloater {
    public Widget asWidget() {
        return rootElement;
    }

    public void setWidth(int width) {
        rootElement.setWidth(width + "px");
        inner.setWidth(width-2+"px");
    }

    interface GanntFloaterWidgetUiBinder extends UiBinder<FlowPanel, GanntFloaterWidget> {
    }

    private static GanntFloaterWidgetUiBinder ourUiBinder = GWT.create(GanntFloaterWidgetUiBinder.class);

    FlowPanel rootElement;
    @UiField
    SimplePanel inner;
    @UiField
    SimplePanel percentLabel;

    public GanntFloaterWidget() {
        rootElement = ourUiBinder.createAndBindUi(this);

    }
}