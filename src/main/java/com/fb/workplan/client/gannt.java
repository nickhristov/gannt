package com.fb.workplan.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fb.workplan.client.release.GanntWidget;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.RootPanel;

public class gannt implements EntryPoint {

    public void onModuleLoad() {
       GanntWidget widget = new GanntWidget();
        widget.setWidth("90%");
        widget.getElement().getStyle().setMargin(2, Style.Unit.EM);
       createModel(widget);
       RootPanel.get().add(widget);
    }

    private void createModel(GanntWidget widget) {
        widget.addTask(createTask("1", "Add support for dependencies", "2011-04-01", 3, EMPTY_DEPENDENCY, null));

        widget.addTask(createTask("2", "Add support for parent tasks", "2011-04-05", 20, EMPTY_DEPENDENCY, null));
        widget.addTask(createTask("10", "Fix the weekly numbering", "2011-04-13", 2, EMPTY_DEPENDENCY, null));

        TaskWidgetData task = createTask("4", "Drag and drop for elements", "2011-06-13", 11, EMPTY_DEPENDENCY, null);
        TaskWidgetData c1 = createTask("5", "HTML5 drag and drop", "2011-06-13", 11, EMPTY_DEPENDENCY, "4");
        TaskWidgetData c2 = createTask("6", "linear drag and drop", "2011-06-13", 11, EMPTY_DEPENDENCY, "5");
        TaskWidgetData c3 = createTask("12", "Cell intercept of event", "2011-06-13", 11, EMPTY_DEPENDENCY, "4");
        TaskWidgetData c4 = createTask("13", "GWT patch to support native DND", "2011-06-13", 11, EMPTY_DEPENDENCY, "5");

        task.getChildren().add(c1);
        task.getChildren().add(c3);
        c1.getChildren().add(c2);
        c1.getChildren().add(c4);

        widget.addTask(task);
        widget.addTask(c1);
        widget.addTask(c2);
        widget.addTask(c3);
        widget.addTask(c4);

        widget.addTask(createTask("7", "Sort intelligently based on parent", "2011-08-20", 1, EMPTY_DEPENDENCY, "2"));
        widget.addTask(createTask("8", "Create canvas based parent floater", "2011-06-21", 11, EMPTY_DEPENDENCY, "2"));
        widget.addTask(createTask("9", "Updated to child dates should update parent as well", "2011-04-21", 10, EMPTY_DEPENDENCY, "2"));

        widget.addTask(createTask("3", "Add support to shift dates", "2011-05-20", 20, new String[]{"2", "1", "8"}, null));
    }
    private TaskWidgetData createTask(String id,
                                  String shortDescription,
                                  String dueDate,
                                  int duration,
                                  String[] dependencies,
                                  String parentTaskId) {
        TaskWidgetData data = new TaskWidgetData();
        data.setId(id);
        data.setDescription(shortDescription);
        data.setDueDate(DateFormatUtils.parse(dueDate));
        data.setDuration(duration);
        if (dependencies != null && dependencies.length > 0) {
            Set<String> dependencyIds = new HashSet<String>();
            dependencyIds.addAll(Arrays.asList(dependencies));
            data.setDependencyIds(dependencyIds);
        }
        data.setParentId(parentTaskId);
        idMap.put(data.getId(), data);
        if (parentTaskId != null) {
            data.setParent(idMap.get(parentTaskId));
        }
        return data;
    }

    private static final String[] EMPTY_DEPENDENCY = new String[]{};

    Map<String, TaskWidgetData> idMap = new HashMap<String, TaskWidgetData>();
}
