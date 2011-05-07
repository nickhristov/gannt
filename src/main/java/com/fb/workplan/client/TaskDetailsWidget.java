package com.fb.workplan.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class TaskDetailsWidget extends Composite {
    public void setText(String text, boolean fireEvents) {
        description.setText(text);
    }

    interface TaskDetailsWidgetUiBinder extends UiBinder<FlowPanel, TaskDetailsWidget> {
    }

    private static TaskDetailsWidgetUiBinder ourUiBinder = GWT.create(TaskDetailsWidgetUiBinder.class);
    FlowPanel rootElement;

    @UiField
    CheckBox cb;

    @UiField
    ContentEditable description;

    public TaskDetailsWidget(TaskWidgetData task) {
        rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
        description.setText(task.getDescription());
        cb.setValue(task.isComplete());
        rootElement.getElement().getStyle().setMarginLeft(task.getIndentLevel() * 0.5, Style.Unit.EM);
    }
}