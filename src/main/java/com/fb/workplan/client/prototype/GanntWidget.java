package com.fb.workplan.client.prototype;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.fb.workplan.client.ContentEditable;
import com.fb.workplan.client.DateFormatUtils;
import com.fb.workplan.client.DateUtils;
import com.fb.workplan.client.GanntFloater;
import com.fb.workplan.client.GanntFloaterParentWidget;
import com.fb.workplan.client.GanntFloaterWidget;
import com.fb.workplan.client.StringUtils;
import com.fb.workplan.client.TaskDetailsWidget;
import com.fb.workplan.client.TaskWidgetData;
import com.fb.workplan.client.release.ChartType;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;


/**
 * TODO: this code needs serious refactoring. needs to be split up in two sections. grid + gannt
 */
@SuppressWarnings({"deprecation"})
public class GanntWidget extends FlowPanel implements GanntView {

    FlexTable grid;
    List<TaskWidgetData> displayModel;
    Map<TaskWidgetData, GanntFloater> floaterMap = new HashMap<TaskWidgetData, GanntFloater>();
    Map<String, TaskWidgetData> idDataMap = new HashMap<String, TaskWidgetData>();

    Canvas canvas;
    private DateBox.Format dateBoxFormatter = new DateFormatUtils.DateBoxFormatter();

    ChartType chartType = ChartType.MONTHLY;

    LinkedHashSet<GanntColumnWidget> columnList = new LinkedHashSet<GanntColumnWidget>();

    public GanntWidget() {
        super();
        getElement().getStyle().setPosition(Style.Position.RELATIVE);
        createGrid();
        add(grid);
    }

    private void createGrid() {
        grid = new FlexTable();
        grid.setCellSpacing(0);
        grid.setStyleName("gannt-table");
        displayModel = new ArrayList<TaskWidgetData>();
        canvas = Canvas.createIfSupported();
        canvas.addStyleName("dependency-canvas");
        add(canvas);
    }

    public void addTask(JSONObject task) {
        displayModel.add(TaskWidgetData.createFromJSON(task));
    }

    private void redraw() {
        recalculateDateRanges();
        fixAndReorderDisplayModel();
        refreshGrid();
        refreshGanntFloaters();
    }


    private void refreshGanntFloaters() {
        for (int i = 0; i < displayModel.size(); i++) {
            createGanntElement(i + 2, displayModel.get(i));
        }
        drawDepencencyCanvas();
    }

    private void drawDepencencyCanvas() {
        Scheduler.get().scheduleFinally(
                new Scheduler.ScheduledCommand() {
                    public void execute() {
                        Context2d context = canvas.getContext2d();
                        int height = getElement().getClientHeight();
                        int width = getElement().getClientWidth();
                        canvas.setHeight(height + "px");
                        canvas.setWidth(width - FIRST_DATE_OFFSET + "px");
                        canvas.setCoordinateSpaceHeight(height);
                        canvas.setCoordinateSpaceWidth(width - FIRST_DATE_OFFSET);
                        context.clearRect(0, 0, height, width);
                        context.setStrokeStyle("#666");
                        context.setLineWidth(2);
                        context.beginPath();
                        for (TaskWidgetData data : displayModel) {
                            List<TaskWidgetData> deps = data.getDependencies();
                            if (deps != null && !deps.isEmpty()) {
                                for (TaskWidgetData dep : deps) {
                                    drawDepencency(canvas.getElement().getOffsetParent(), context, data, dep);
                                }
                            }
                        }
                        context.stroke();
                    }
                }
        );

    }

    private void drawDepencency(Element canvasOffsetElement, Context2d context, TaskWidgetData data, TaskWidgetData dep) {
        GanntFloater startFloater = floaterMap.get(data);
        GanntFloater endFloater = floaterMap.get(dep);
        double startX = 0, startY = 0, endX = 0, endY = 0;
        Element offsetParent = startFloater.asWidget().getElement();
        while (!offsetParent.equals(canvasOffsetElement)) {
            startX += offsetParent.getOffsetLeft();
            startY += offsetParent.getOffsetTop();
            offsetParent = offsetParent.getOffsetParent();
        }
        startX += startFloater.asWidget().getElement().getClientWidth();
        startX -= FIRST_DATE_OFFSET;
        startY += startFloater.asWidget().getElement().getClientHeight() / 2;

        offsetParent = endFloater.asWidget().getElement();
        while (!offsetParent.equals(canvasOffsetElement)) {
            endX += offsetParent.getOffsetLeft();
            endY += offsetParent.getOffsetTop();
            offsetParent = offsetParent.getOffsetParent();
        }
        endX -= FIRST_DATE_OFFSET;
        endY += endFloater.asWidget().getElement().getClientHeight() / 2;
        drawDepencencyLine(context, startX, startY, endX, endY, startFloater.asWidget().getElement().getClientHeight());
    }

    private void drawDepencencyLine(Context2d context, double startX, double startY, double endX, double endY, int clientHeight) {
        context.moveTo(startX, startY);
        context.lineTo(startX + 5, startY);
        double nextY, nextX;
        if (startY > endY) {
            nextY = startY - clientHeight / 2 - 5;
        } else {
            nextY = startY + clientHeight / 2 + 5;
        }
        context.lineTo(startX + 5, nextY);
        if (startX > endX) {
            nextX = endX - 5;
            context.lineTo(nextX, nextY);
            context.lineTo(nextX, endY);
            context.lineTo(endX, endY);
        } else {
            context.lineTo(startX + 5, endY);
            context.lineTo(endX, endY);
        }

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

    private Date firstStartDate() {
        for (TaskWidgetData task : displayModel) {
            if (task.getStartDate() != null) {
                return task.getStartDate();
            }
        }
        return null;
    }


    private void recalculateDateRanges() {
        Date startDate = firstStartDate();
        Date minDate = startDate == null ? new Date() : startDate;
        Date maxDate = minDate;
        for (TaskWidgetData task : displayModel) {
            if (task.getStartDate() != null && task.getStartDate().before(minDate)) {
                minDate = task.getStartDate();
            }

            if (task.getDueDate() != null && task.getDueDate().after(maxDate)) {
                maxDate = task.getDueDate();
            }
        }

        // put a column for each month
        int numMonths = getMonthDiff(minDate, maxDate) + 6;
        for (int i = 1; i <= numMonths; i++) {
            int year = (minDate.getYear() + 1900) + i / 12;
            int month = minDate.getMonth() + i % 12;

            GanntColumnWidget column = new GanntColumnWidget(DateUtils.date(year, month, 1));
            columnList.add(column);
        }

    }

    private String format(Date startDate) {
        return startDate.getYear() + 1900 + "-" + startDate.getMonth() + "-" + startDate.getDate();
    }

    private int getMonthDiff(Date minDate, Date maxDate) {
        return Math.abs((maxDate.getYear() - minDate.getYear()) * 12 - (maxDate.getMonth() - minDate.getMonth()));
    }

    private GanntFloater createGanntElement(int insertRow, TaskWidgetData task) {
        GanntFloater widget = new GanntFloaterWidget();
        if (task.getChildren().size() > 0) {
            widget = new GanntFloaterParentWidget();
        }
        floaterMap.put(task, widget);
        insertAndPositionGanntElement(widget, insertRow, task);
        return widget;
    }

    private void insertAndPositionGanntElement(GanntFloater widget, int insertRow, TaskWidgetData task) {
        widget.asWidget().getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        widget.asWidget().getElement().getStyle().setTop(0, Style.Unit.PX);
        int columnPosition = 4 + 4 * getInsertColumn(task);
        int offset = getColumnOffset(task);
        int width = getFloaterWidth(task);

        widget.asWidget().getElement().getStyle().setLeft(offset, Style.Unit.PX);

        widget.setWidth(width);
        widget.asWidget().getElement().getStyle().setZIndex(200);
        FlowPanel fp = (FlowPanel) grid.getWidget(insertRow, columnPosition);
        if (fp == null) {
            fp = new FlowPanel();
            fp.setStyleName("float-holder");
            grid.setWidget(insertRow, columnPosition, fp);
        }
        fp.add(widget);
    }

    private int getFloaterWidth(TaskWidgetData task) {
        int duration = 1;
        if (task.getDuration() > 0) {
            duration = task.getDuration();
        }
        double width = 0;

        if (chartType.equals(ChartType.MONTHLY)) {
            width = (duration / 30.0) * COLUMN_WIDTH;
        }
        if (chartType.equals(ChartType.DAILY)) {
            width = duration * COLUMN_WIDTH;
        }
        if (chartType.equals(ChartType.WEEKLY)) {
            width = (duration / 7.0) * COLUMN_WIDTH;
        }
        width = width < 20 ? 20 : width;
        return new Double(width).intValue();
    }

    private int getColumnOffset(TaskWidgetData task) {
        Date startDate = task.getStartDate();
        if (startDate == null) {
            return 0;
        }
        double offset = 0.0;
        if (chartType.equals(ChartType.MONTHLY)) {
            offset = startDate.getDate() / 30.0 * COLUMN_WIDTH;
        }
        if (chartType.equals(ChartType.DAILY)) {
            offset = 0;
        }
        if (chartType.equals(ChartType.WEEKLY)) {
            offset = startDate.getDay() / 7.0 * COLUMN_WIDTH;
        }
        return new Double(offset).intValue();
    }

    private int getInsertColumn(TaskWidgetData task) {
        if (chartType.equals(ChartType.MONTHLY)) {
            return getMonthlyInsertColumn(task);
        }
        if (chartType.equals(ChartType.DAILY)) {
            return getDailyInsertColumn(task);
        }
        if (chartType.equals(ChartType.WEEKLY)) {
            return getWeeklyInsertColumn(task);
        }
        return 0;
    }


    private int getWeeklyInsertColumn(TaskWidgetData task) {
        // TODO: finish this
        return 0;
    }

    private int getDailyInsertColumn(TaskWidgetData task) {
        // TODO: finish this
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    private int getMonthlyInsertColumn(TaskWidgetData task) {
        Date startDate = new Date(); // by default today
        if (task.getStartDate() != null) {
            startDate = task.getStartDate();
        }
        // we could do this more intelligently...
        Iterator<GanntColumnWidget> iterator = columnList.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            GanntColumnWidget column = iterator.next();
            if (DateUtils.isSameMonth(column.getDate(), startDate)) {
                return index;
            }
            index++;
        }
        return 0;
    }

    private void refreshGrid() {
        grid.insertRow(0);
        grid.insertRow(1);
        grid.getFlexCellFormatter().setColSpan(0, 0, 4);
        grid.setText(0, 0, "Task details");
        grid.setText(1, 0, "Description");
        grid.getCellFormatter().setWidth(1, 0, "400px");

        grid.setText(1, 1, "Due Date");
        grid.setText(1, 2, "Start Date");
        grid.setText(1, 3, "Duration");

        int i = 2;
        for (TaskWidgetData task : displayModel) {

            TaskDetailsWidget editableDescription = new TaskDetailsWidget(task);

            ContentEditable editableDuration = new ContentEditable();
            DateBox dueDateBox = new DateBox();
            dueDateBox.addValueChangeHandler(new GanntElementDueDateShifter(task));
            dueDateBox.getTextBox().setStyleName("date-editor");
            DateBox startDateBox = new DateBox();
            dueDateBox.addValueChangeHandler(new GanntElementStartDateShifter(task));
            startDateBox.getTextBox().setStyleName("date-editor");

            startDateBox.setFormat(dateBoxFormatter);
            dueDateBox.setFormat(dateBoxFormatter);
            grid.setWidget(i, 0, editableDescription);
            grid.setWidget(i, 1, dueDateBox);
            grid.setWidget(i, 2, startDateBox);
            grid.getCellFormatter().setWidth(i, 1, "80px");
            grid.getCellFormatter().setWidth(i, 2, "80px");

            grid.setWidget(i, 3, editableDuration);

            if (task.getDueDate() != null) {
                dueDateBox.setValue(task.getDueDate());
            }
            if (task.getStartDate() != null) {
                startDateBox.setValue(task.getStartDate());
            }
            if (task.getDuration() > 0) {
                editableDuration.setText(task.getDuration() + "");
            }
            i++;
        }
        int j = 1;
        for (GanntColumnWidget column : columnList) {
            grid.setWidget(0, j, column);
            grid.getCellFormatter().setWidth(0, j, COLUMN_WIDTH + "px");
            grid.getFlexCellFormatter().setColSpan(0, j, 4);

            for (int k = 0; k < 4; k++) {
                grid.insertCell(1, j * 4 + k);
                grid.setText(1, j * 4 + k, k * 7 + 1 + "");
                grid.getCellFormatter().setWidth(1, j * 4 + k, "20px");
                for (i = 0; i < displayModel.size(); i++) {
                    FlowPanel fp = new FlowPanel();
                    fp.setStyleName("float-holder");
                    grid.setWidget(i + 2, j * 4 + k, fp);
                    grid.getCellFormatter().setWidth(i + 2, j * 4 + k, "20px");
                }
            }
            j++;
        }
    }

    private class GanntElementDueDateShifter extends GanntElementShifter {
        public GanntElementDueDateShifter(TaskWidgetData task) {
            super(task);
        }

        public void onValueChange(ValueChangeEvent<Date> valueChangeEvent) {
            task.setDueDate(valueChangeEvent.getValue());
            super.onValueChange(valueChangeEvent);
        }
    }

    private class GanntElementStartDateShifter extends GanntElementShifter {
        public GanntElementStartDateShifter(TaskWidgetData task) {
            super(task);
        }

        public void onValueChange(ValueChangeEvent<Date> valueChangeEvent) {
            task.setStartDate(valueChangeEvent.getValue());
            super.onValueChange(valueChangeEvent);
        }
    }

    private class GanntElementShifter implements ValueChangeHandler<Date> {
        public GanntElementShifter(TaskWidgetData task) {
            this.task = task;
        }

        public void onValueChange(ValueChangeEvent<Date> valueChangeEvent) {
            GanntFloater floater = floaterMap.get(task);
            floater.asWidget().removeFromParent();
            insertAndPositionGanntElement(floater, displayModel.indexOf(task) + 2, task);
        }

        protected final TaskWidgetData task;
    }

    public Widget asWidget() {
        redraw();
        return this;
    }

    int COLUMN_WIDTH = 80;

    int FIRST_DATE_OFFSET = 510;    //UGLY hack, fix this
}
