package com.fb.workplan.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fb.workplan.client.release.GanntWidget;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.ui.RootPanel;

public class gannt implements EntryPoint {

    public void onModuleLoad() {
       GanntWidget widget = new GanntWidget();
        createModel(widget);
        RootPanel.get().add(widget);
    }

    private void createModel(GanntWidget widget) {
        Resources r = GWT.create(Resources.class);
        String data = r.taskData().getText();
        JSONValue value = JSONParser.parseStrict(data);

        assert value.isArray() != null;
        assert value.isArray().size() > 0;
        JSONArray dataArray = value.isArray();

        for(int i = 0; i < dataArray.size(); i++) {
            addTaskToModel(dataArray.get(i));
        }
        resolveOrphans();
        widget.addAll(all);
    }

    private void addTaskToModel(JSONValue jsonValue) {
        JSONObject obj = jsonValue.isObject();
        assert obj != null;
        assert obj.containsKey("id");
        assert obj.containsKey("shortDescription");
        TaskWidgetData data = new TaskWidgetData();
        data.setId(obj.get("id").isString().stringValue());
        data.setDescription(obj.get("shortDescription").isString().stringValue());
        if (obj.containsKey("dueDate")) {
            data.setDueDate(DateFormatUtils.parse(obj.get("dueDate").isString().stringValue()));
        }
        if (obj.containsKey("startDate")) {
            data.setStartDate(DateFormatUtils.parse(obj.get("startDate").isString().stringValue()));
        }

        if (obj.containsKey("duration")) {
            data.setDuration(new Double(obj.get("duration").isNumber().doubleValue()).intValue());
        }
        if (obj.containsKey("dependencies")) {
            JSONValue depVal = obj.get("dependencies");
            JSONArray dependencies;
            dependencies = depVal.isArray();
            if (dependencies != null && dependencies.size() > 0) {
                Set<String> dependencyIds = new HashSet<String>();
                dependencyIds.addAll(asList(dependencies));
                data.setDependencyIds(dependencyIds);

				for (String depid: data.getDependencyIds()) {
					TaskWidgetData dependency = idMap.get(depid);
					if (dependency == null) {
						orphans.add(data);
					} else {
						data.getDependencies().add(dependency);
					}
				}
            }
        }
        if (obj.containsKey("parentId")) {
            String parentTaskId = obj.get("parentId").isString().stringValue();
            data.setParentId(parentTaskId);
            TaskWidgetData parentData = idMap.get(parentTaskId);
            if (parentData != null) {
                data.setParent(parentData);
                parentData.getChildren().add(data);
            } else {
                orphans.add(data);
            }
        }
        idMap.put(data.getId(), data);
        all.add(data);
    }

    private Collection<String> asList(JSONArray dependencies) {
		List<String> result = new ArrayList<String>(dependencies.size());
		for(int i = 0; i < dependencies.size(); i++) {
			result.add(dependencies.get(i).isString().stringValue());
		}
		return result;
    }

    private void resolveOrphans() {
        for(TaskWidgetData orphan: orphans) {
			if (StringUtils.hasText(orphan.getParentId())) {
				TaskWidgetData parentData = idMap.get(orphan.getParentId());
				if (parentData != null) {
					orphan.setParent(parentData);
					parentData.getChildren().add(orphan);
				}
			}
			if (! orphan.getDependencyIds().isEmpty()) {
				orphan.getDependencies().clear();
				for(String depId: orphan.getDependencyIds()) {
					TaskWidgetData dependency = idMap.get(depId);
					if(dependency != null) {
						orphan.getDependencies().add(dependency);
					}
				}
			}
        }
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
            TaskWidgetData parent = idMap.get(parentTaskId);
            data.setParent(parent);
            if (parent != null) {
                parent.getChildren().add(data);
            } else {
                orphans.add(data);
            }
        }
        return data;
    }

    private static final String[] EMPTY_DEPENDENCY = new String[]{};

    Map<String, TaskWidgetData> idMap = new HashMap<String, TaskWidgetData>();

    Set<TaskWidgetData> orphans = new HashSet<TaskWidgetData>();
    List<TaskWidgetData> all = new LinkedList<TaskWidgetData>();
    
    interface Resources extends ClientBundle {
        @Source("taskData.json")
        TextResource taskData();
    }
}
