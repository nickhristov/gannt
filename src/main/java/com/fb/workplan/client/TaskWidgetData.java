package com.fb.workplan.client;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

@SuppressWarnings({"UnusedDeclaration"})
public class TaskWidgetData implements Comparable<TaskWidgetData> {

    public TaskWidgetData() {
    }

    private Date dueDate;
    private Date startDate;
    private String description;
    private String id;
    private String parentId;
    private int duration = 0;
    private List<TaskWidgetData> dependencies = new LinkedList<TaskWidgetData>();
    private Set<String> dependencyIds;
    private TaskWidgetData parent;
    private int indentLevel = 0;
    private final List<TaskWidgetData> children = new LinkedList<TaskWidgetData>();
    private final MFunction<TaskWidgetData, Integer> getDurationFunc = new  MFunction<TaskWidgetData, Integer>() {
        public Integer apply( TaskWidgetData input) {
            return input.getDuration();
        }
    } ;
    private final MFunction<TaskWidgetData, Date> getStartDateFunc = new MFunction<TaskWidgetData, Date>() {
        public Date apply(TaskWidgetData input) {
            return input.getStartDate();
        }
    };

    private final MFunction<TaskWidgetData, Date> getEndDateFunc = new MFunction<TaskWidgetData, Date>() {
        public Date apply(TaskWidgetData input) {
            return input.getDueDate();
        }
    };


    public Set<String> getDependencyIds() {
        return dependencyIds;
    }

    public void setDependencyIds(Set<String> dependencyIds) {
        this.dependencyIds = dependencyIds;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    private boolean complete;


    public int getIndentLevel() {
        if (indentLevel == 0 && parent != null) {
            indentLevel = calculateIndentLevel();
        }
        return indentLevel;
    }

    private int calculateIndentLevel() {
        TaskWidgetData parent = this.parent;
        int indentLevel = 1;
        while(parent!= null) {
            parent = parent.getParent();
            indentLevel++;
        }
        return indentLevel;
    }

    public List<TaskWidgetData> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<TaskWidgetData> dependencies) {
        this.dependencies = dependencies;
    }

    public TaskWidgetData getParent() {
        return parent;
    }

    public void setParent(TaskWidgetData parent) {
        this.parent = parent;
    }

    public Date getDueDate() {
        if (getChildren().size() < 1) {
            return dueDate;
        } else {
            List<Date> endDates = MLists.transform(getChildren(), getEndDateFunc);
            return Collections.max(endDates);
        }
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
        if (this.duration > 0) {
            this.startDate = new Date(this.dueDate.getTime() - duration * DAY_IN_MS);
        }

    }

    public Date getStartDate() {
        if (getChildren().size() < 1) {
            return startDate;
        } else {
            List<Date> startDates = MLists.transform(getChildren(), getStartDateFunc);
            return Collections.min(startDates);
        }
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        if (this.duration > 0) {
            this.dueDate = new Date(startDate.getTime() + duration * DAY_IN_MS);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDuration() {
        if (this.getChildren().isEmpty()) {
            return duration;
        } else {
            List<Integer> durations = MLists.transform(getChildren(), getDurationFunc);
            int sum = 0;
            for(int d : durations) {
                sum+= d;
            }
            int daysDiff = DateUtils.getDaysDiff(getStartDate(), getDueDate());

            return Math.max(daysDiff, sum);
        }
    }

    public void setDuration(int duration) {
        this.duration = duration;
        if (duration > 0 && this.startDate != null) {
            this.dueDate = new Date(startDate.getTime() + duration * DAY_IN_MS);
        } else if (duration > 0 && this.dueDate != null) {
            this.startDate = new Date(dueDate.getTime() - duration * DAY_IN_MS);
        }
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public static TaskWidgetData createFromJSON(JSONObject task) {
        TaskWidgetData data = new TaskWidgetData();
        String startDateStr;
        String dueDateStr = null;
        if (task.containsKey("startDate")) {
            startDateStr = task.get("startDate").isString().stringValue();
            data.startDate = DateFormatUtils.parse(startDateStr);
        }
        if (task.containsKey("dueDate")) {
            dueDateStr = task.get("dueDate").isString().stringValue();
            data.dueDate = DateFormatUtils.parse(dueDateStr);
        }
        if (task.containsKey("duration")) {
            data.duration = new Double(task.get("duration").isNumber().doubleValue()).intValue();
        }
        if (data.startDate == null &&
                data.duration > 0 &&
                data.dueDate != null) {
            data.startDate = new Date(data.dueDate.getTime() - data.duration * DAY_IN_MS);
        }
        if (dueDateStr == null &&
                data.duration > 0 &&
                data.startDate != null) {
            data.dueDate = new Date(data.startDate.getTime() + data.duration * DAY_IN_MS);
        }
        data.description =task.get("description").isString().stringValue();
        data.id = task.get("id").isString().stringValue();
        if (task.containsKey("parentId")) {
            data.parentId = task.get("parentId").isString().stringValue();
        }
        if (task.containsKey("dependencies")) {
            JSONArray deps = task.get("dependencies").isArray();
            Set<String> depset = new HashSet<String>();
            for(int i = 0; i < deps.size(); i++) {
                String depid = deps.get(i).isString().stringValue();
                depset.add(depid);
            }
            data.dependencyIds = depset;
        }
        return data;
    }

    public List<TaskWidgetData> getChildren() {
        return children;
    }

    public boolean equals(Object ob) {
        if (ob == this) {
            return true;
        }
        if (! (ob instanceof TaskWidgetData)) {
            return false;
        }
        TaskWidgetData twd = (TaskWidgetData) ob;
        return id.equals(twd.id);
    }
    public int compareTo(TaskWidgetData o) {
        if (o != null) {
            return o.id.compareTo(id);
        } else {
            return 1;
        }
    }

    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return this.description;
    }
    private static final long DAY_IN_MS = 3600 * 24*1000;
}
