package com.fb.workplan.client.prototype;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.IsWidget;

public interface GanntView extends IsWidget {
    void addTask(JSONObject task);
}
