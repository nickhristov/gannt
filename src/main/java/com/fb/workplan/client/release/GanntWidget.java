package com.fb.workplan.client.release;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fb.workplan.client.StringUtils;
import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.CellTreeTable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mostly orchestrates events and data across several subwidgets.
 */
public class GanntWidget extends Composite {

    List<TaskWidgetData> displayModel = new ArrayList<TaskWidgetData>(50);
    public GanntWidget() {
        table = new CellTreeTable<TaskWidgetData>();
        table.addStyleName("gannt-table");
        manager = new HandlerManager(this);

        detailsController = new GanntGridController(table, manager);
        calendarController = new GanntCalendarController(table, displayModel, manager);
        canvasWidget = new GanntCanvasController(table, displayModel);
        idDataMap = new HashMap<String, TaskWidgetData>();
        initWidget(table);
    }

    protected void initWidget(Widget table) {
        super.initWidget(table);
        detailsController.init();
    }

    // TODO: this code is getting duplicated in cell tree because of model management issues, investigate and fix
    public void addTask(TaskWidgetData data) {
        if (data.getParent() != null) {
            table.addItem(data.getParent(), data);
        } else {
            table.addItem(data);
        }
        displayModel.add(data);
        calendarController.updateCalendar();
//        detailsController.insertData(node);
//        calendarController.insertData(index, node);
//        canvasWidget.insertData(index, data);
    }

    private void fixAndReorderDisplayModel() {
        for (TaskWidgetData task : displayModel) {
            idDataMap.put(task.getId(), task);
        }
        ArrayList<TaskWidgetData> reinsertList = new ArrayList<TaskWidgetData>(20);
        Iterator<TaskWidgetData> displayModelIt = displayModel.iterator();

        while (displayModelIt.hasNext()) {
            TaskWidgetData task = displayModelIt.next();
            if (StringUtils.hasText(task.getParentId())) {
                TaskWidgetData parentData = idDataMap.get(task.getParentId());
                parentData.getChildren().add(task);
                task.setParent(parentData);
                reinsertList.add(task);
                displayModelIt.remove();
            }

            if (task.getDependencyIds() != null && !task.getDependencyIds().isEmpty()) {
                for (String depID : task.getDependencyIds()) {
                    task.getDependencies().add(idDataMap.get(depID));
                }
            }
        }

        // support up to 10 indent levels
        int indentLevel = 1;
        while (!reinsertList.isEmpty() && indentLevel <= 10) {
            Iterator<TaskWidgetData> reinsertIterator = reinsertList.iterator();
            while (reinsertIterator.hasNext()) {
                TaskWidgetData taskWidgetData = reinsertIterator.next();
                if (taskWidgetData.getIndentLevel() == indentLevel) {
                    TaskWidgetData parent = taskWidgetData.getParent();
                    int parentIndex = displayModel.indexOf(parent);
                    displayModel.add(parentIndex + 1, taskWidgetData);
                    reinsertIterator.remove();
                }
            }

            indentLevel++;
        }
        displayModel.addAll(reinsertList);
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<TaskWidgetData> valueChangeHandler) {
        return manager.addHandler(ValueChangeEvent.getType(), valueChangeHandler);
    }

    private final CellTreeTable<TaskWidgetData> table;
    private final HandlerManager manager;

    private final GanntGridController detailsController;
    private final GanntCanvasController canvasWidget;
    private GanntCalendarController calendarController;
    private final Map<String, TaskWidgetData> idDataMap;
}
