package com.fb.workplan.client.release;

import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.event.logical.shared.ValueChangeEvent;

class TaskChangeEvent extends ValueChangeEvent<TaskWidgetData> {

    TaskChangeEvent(TaskWidgetData value) {
        super(value);
    }
}
