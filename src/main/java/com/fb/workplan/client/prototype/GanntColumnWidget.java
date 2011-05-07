package com.fb.workplan.client.prototype;

import com.fb.workplan.client.DateFormatUtils;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.Date;

public class GanntColumnWidget extends FlowPanel {
    Label label = new Label();
    private Date date;

    public GanntColumnWidget(Date date) {
        this.date = date;
        add(label);
        getElement().getStyle().setPosition(Style.Position.RELATIVE);
        refreshLabel();
    }
    private void refreshLabel() {
        label.setText(DateFormatUtils.format(date));

    }

    public Date getDate() {
        return date;
    }
}
