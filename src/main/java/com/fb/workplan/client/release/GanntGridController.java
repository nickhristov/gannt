package com.fb.workplan.client.release;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.CellTreeTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.FlexHeader;
import com.google.gwt.user.cellview.client.Header;

class GanntGridController {

    public void insertData(TaskWidgetData node) {
        table.addItem(node);
    }

    List<Column<TaskWidgetData, ?>> detailColumns;

    public GanntGridController(CellTreeTable<TaskWidgetData> table, HandlerManager manager) {
        this.table = table;
        this.manager = manager;
    }

    public void init() {
        EditTextCell descriptionCell = new EditTextCell();
        Column<TaskWidgetData, String> descColumn = new Column<TaskWidgetData, String>(descriptionCell) {
            @Override
            public String getValue(TaskWidgetData object) {
                return object.getDescription();
            }
        };

        DatePickerCell startDateCell = new DatePickerCell();
        Column<TaskWidgetData, Date> startDateCol = new Column<TaskWidgetData, Date>(startDateCell) {
            @Override
            public Date getValue(TaskWidgetData object) {
                return object.getStartDate();
            }
        };
        startDateCol.setFieldUpdater(new FieldUpdater<TaskWidgetData, Date>() {
            public void update(int index, TaskWidgetData object, Date value) {
                // TODO: there is a whole lot of issues here to be resolved (conflicts, etc)
                object.setStartDate(value);
                manager.fireEvent(new TaskChangeEvent(object));
            }
        });

        DatePickerCell dueDateCell = new DatePickerCell();
        Column<TaskWidgetData, Date> dueDateCol = new Column<TaskWidgetData, Date>(dueDateCell) {
            @Override
            public Date getValue(TaskWidgetData object) {
                return object.getDueDate();
            }
        };

        dueDateCol.setFieldUpdater(new FieldUpdater<TaskWidgetData, Date>() {
            public void update(int index, TaskWidgetData object, Date value) {
                // TODO: there is a whole lot of issues here to be resolved (conflicts, etc)
                object.setDueDate(value);
                manager.fireEvent(new TaskChangeEvent(object));
            }
        });

        detailColumns = new ArrayList<Column<TaskWidgetData, ?>>(3);
        detailColumns.add(descColumn);
        detailColumns.add(startDateCol);
        detailColumns.add(dueDateCol);
        detailColumns = Collections.unmodifiableList(detailColumns);    // freeze these

        // set up standard headers
        final TextCell standardHeaderCell = new TextCell();
        final List<Header<?>> TOP_ROW = new ArrayList<Header<?>>(2);
        TOP_ROW.addAll(Arrays.asList(
                new FlexHeader<String>(standardHeaderCell, 3, "Description"),
                new FlexHeader<String>(standardHeaderCell, "Calendar")
        ));

        final List<Header<?>> SECOND_ROW = new ArrayList<Header<?>>();
        SECOND_ROW.addAll(Arrays.asList(
                new FlexHeader<String>(standardHeaderCell, "Task"),
                new FlexHeader<String>(standardHeaderCell, "Start Date"),
                new FlexHeader<String>(standardHeaderCell, "Due Date")
        ));

        table.addHeaderRow(TOP_ROW);
        table.addHeaderRow(SECOND_ROW);

        for (Column<TaskWidgetData, ?> column : detailColumns) {
            table.addColumn(column);
        }
    }

    private CellTreeTable<TaskWidgetData> table;
    private HandlerManager manager;
}
