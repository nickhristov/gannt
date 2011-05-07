package com.fb.workplan.client.release;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fb.workplan.client.DateUtils;
import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.CellTreeTable;
import com.google.gwt.user.client.ui.FlexTable;

class GanntCanvasController {
    public GanntCanvasController(CellTreeTable<TaskWidgetData> table) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public GanntCanvasController(CellTreeTable<TaskWidgetData> table, List<TaskWidgetData> taskList) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public void insertData(int index, TaskWidgetData data) {
        //To change body of created methods use File | Settings | File Templates.
    }

    int MIN_NUM_COLUMNS = 3;

    Date minDate ;
    Date maxDate ;

    boolean addedInitialColumns;
    Canvas canvas;

    enum Mode { DIALY, WEEKLY } ;

    Mode mode = GanntCanvasController.Mode.WEEKLY;

    List<TaskWidgetData> internalModel = new ArrayList<TaskWidgetData>();

    public GanntCanvasController(HandlerManager manager, FlexTable table, int insertColumn) {
        this.manager = manager;
        this.insertColumn = insertColumn;
        minDate = new Date();
        addedInitialColumns = false;
        maxDate = DateUtils.rollMonth(minDate, MIN_NUM_COLUMNS);
        canvas = Canvas.createIfSupported();
        table.setWidget(0,1, canvas);
    }
    /*
    public void insertData(int index, TaskWidgetData data) {
        index = index+3;
        refreshColumnsAndDates(data);
        int insertColumn = getInsertColumn(data);
        GanntFloater floater = new GanntFloaterWidget();
        floater.setWidth(getFloaterWidth(data));
        FlowPanel panel;
        if ( ( table.getCellCount(index)) <= (insertColumn + this.insertColumn)) {
            panel = createFloatHolder();
            table.setWidget(index, insertColumn + this.insertColumn, panel);
        } else {
            panel = (FlowPanel) table.getWidget(index, insertColumn + this.insertColumn);
            if (panel == null) {
                panel = createFloatHolder();
            }
        }
        panel.add(floater);
    }

    private FlowPanel createFloatHolder() {
       FlowPanel panel = new FlowPanel();
       panel.setStyleName("float-holder");
       panel.add(new Label(""));
       return panel;
    }
    private int getFloaterWidth(TaskWidgetData data) {
        return 20;
    }

    private int getInsertColumn(TaskWidgetData data) {
        return DateUtils.getWeekDiff(minDate, data.getStartDate());
    }

    private void refreshColumnsAndDates(TaskWidgetData data) {
        if (! addedInitialColumns ) {
            addedInitialColumns = true;
            for(int i = 0; i <= MIN_NUM_COLUMNS; i++) {
                addColumn(0, DateUtils.rollMonth(maxDate, -i));
            }            
        }
        int numMonthsDiff = DateUtils.getMonthDifference(minDate, data.getStartDate());
        /// TODO: simplify this logic
        Date month = minDate;
        if (numMonthsDiff < 0) {
            for(int i = -1; i >= numMonthsDiff; i--) {
                month = DateUtils.rollMonth(month, i);
                addColumn(0, month);
            }
        }
        numMonthsDiff = DateUtils.getMonthDifference(maxDate, data.getDueDate());
        if (numMonthsDiff > 0) {
            month = maxDate;
            for(int i = 1; i <= numMonthsDiff; i++) {
                month = DateUtils.rollMonth(month, i);
                addColumn(getNumDisplayedMonths()+1, month);
            }
            maxDate = data.getDueDate();
        }

        if (data.getStartDate().before(minDate)) {
            minDate = DateUtils.beginningOfMonth(data.getStartDate());
        }
        if (data.getDueDate().after(maxDate)) {
            maxDate = data.getDueDate();
        }
        
        // ensure table is sized properly:
        int numWeeks = DateUtils.getWeekDiff(DateUtils.getFirstWeekdayOfMonth(minDate, true), maxDate);
        GWT.log("num weeks: " + numWeeks);
        int maxRow = table.getRowCount();
        for (int i = 3; i < maxRow; i++) {
            if (table.getCellCount(i) < (numWeeks + this.insertColumn)) {
                GWT.log("fixing row: "+ i + " with col: " + numWeeks);
                table.insertCell(i, numWeeks + this.insertColumn);
                table.setWidget(i, numWeeks + this.insertColumn, createFloatHolder());
            }
        }
    }

    private int getNumDisplayedMonths() {
        return DateUtils.getMonthDifference(minDate, maxDate);
    }

    private void addColumn(int i, Date month) {
        table.insertCell(1, 1 + i);
        table.setText(1, 1+i, DateFormatUtils.monthFormat(month));
        Date firstMonday = DateUtils.getFirstWeekdayOfMonth(month, true);
        int numCells = 0;
        Set<Date> dates = new HashSet<Date>();
        while (DateUtils.isSameMonth(firstMonday, month)) {
            dates.add(firstMonday);
            firstMonday = DateUtils.rollDays(firstMonday, 7);
            numCells++;
        }
        table.getFlexCellFormatter().setColSpan(1, 1+i, numCells);
        int j = dates.size() -1;
        for(Date date: dates) {
            table.insertCell(2, insertColumn +i +j);
            table.setText(2, insertColumn + i + j, (date.getMonth() + 1) + " / " + date.getDate() + "");
            table.getCellFormatter().addStyleName(2, insertColumn + i + j, "datelabel");
            j--;
        }

    }
    */
    private HandlerManager manager;
    private int insertColumn;
}
