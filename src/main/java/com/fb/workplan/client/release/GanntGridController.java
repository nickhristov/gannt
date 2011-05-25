package com.fb.workplan.client.release;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fb.workplan.client.AnchorCell;
import com.fb.workplan.client.DateUtils;
import com.fb.workplan.client.FixedCompositeCell;
import com.fb.workplan.client.PropertyDidChangeEvent;
import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellStyleProvider;
import com.google.gwt.user.cellview.client.CellTreeTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ContentEditableCell;
import com.google.gwt.user.cellview.client.FlexHeader;
import com.google.gwt.user.cellview.client.Header;

import static java.util.Arrays.asList;

class GanntGridController {

    public void insertData(TaskWidgetData node) {
        table.addItem(node);
    }

    List<Column<TaskWidgetData, ?>> detailColumns;

    public GanntGridController(CellTreeTable<TaskWidgetData> table, HandlerManager manager) {
        this.table = table;
        this.manager = manager;
    }

	public Cell buildTaskCell() {
		ContentEditableCell descriptionCell = new ContentEditableCell(true);
		final Column<TaskWidgetData, String> descColumn = new Column<TaskWidgetData, String>(descriptionCell) {
			@Override
			public String getValue(TaskWidgetData object) {
				return object.getDescription();
			}
		};

		Cell<String> linkCell = new AnchorCell();

		Column<TaskWidgetData, String> link = new Column<TaskWidgetData, String>(linkCell) {
			@Override
			public String getValue(TaskWidgetData object) {
				return "\u24d8";
			}
		};

		return new FixedCompositeCell<TaskWidgetData>(Arrays.<HasCell<TaskWidgetData, ?>>asList(descColumn, link));
	}
    public void init() {
		Cell taskCell = buildTaskCell();
		final Column<TaskWidgetData, TaskWidgetData> taskColumn = new Column<TaskWidgetData, TaskWidgetData>(taskCell) {
			@Override
			public TaskWidgetData getValue(TaskWidgetData object) {
				return object;
			}
		};
        DatePickerCell startDateCell = new DatePickerCell(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT));
        final Column<TaskWidgetData, Date> startDateCol = new Column<TaskWidgetData, Date>(startDateCell) {
            @Override
            public Date getValue(TaskWidgetData object) {
                return object.getStartDate();
            }
        };
        startDateCol.setFieldUpdater(new FieldUpdater<TaskWidgetData, Date>() {
            public void update(int index, TaskWidgetData object, Date value) {
                // TODO: there is a whole lot of issues here to be resolved (conflicts, etc)
				Date oldStartDate =  object.getStartDate();
				object.setStartDate(value);
				if (adjustDates) {
					object.setDueDate(DateUtils.rollDays(object.getStartDate(), object.getDuration()));
				} else {
					object.setDuration(DateUtils.getDaysDiff(object.getStartDate(), object.getDueDate()));
				}

				GwtEvent event = new PropertyDidChangeEvent<TaskWidgetData>(object, "startDate", oldStartDate, value);
                manager.fireEvent(event);
            }
        });

        DatePickerCell dueDateCell = new DatePickerCell(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT));
        final Column<TaskWidgetData, Date> dueDateCol = new Column<TaskWidgetData, Date>(dueDateCell) {
            @Override
            public Date getValue(TaskWidgetData object) {
                return object.getDueDate();
            }
        };

        dueDateCol.setFieldUpdater(new FieldUpdater<TaskWidgetData, Date>() {
            public void update(int index, TaskWidgetData object, Date value) {
                // TODO: there is a whole lot of issues here to be resolved (conflicts, etc)
				Date oldDueDate = object.getDueDate();
				object.setDueDate(value);

				if (adjustDates) {
					object.setStartDate(DateUtils.rollDays(value, - object.getDuration()));
				} else {
					object.setDuration(DateUtils.getDaysDiff(object.getStartDate(), object.getDueDate()));
				}
				GwtEvent event = new PropertyDidChangeEvent<TaskWidgetData>(object, "dueDate", oldDueDate, value);
                manager.fireEvent(event);
            }
        });

        ContentEditableCell durationCell = new ContentEditableCell(false);
        final Column<TaskWidgetData, String> durationColumn = new Column<TaskWidgetData, String>(durationCell) {
            @Override
            public String getValue(TaskWidgetData object) {
                return String.valueOf(object.getDuration());
            }
        };

        durationColumn.setFieldUpdater(new FieldUpdater<TaskWidgetData, String>() {
            public void update(int index, TaskWidgetData object, String value) {
                // TODO: there is a whole lot of issues here to be resolved (conflicts, etc)
				int newDuration =  Integer.parseInt(value);
				int oldDuration = object.getDuration();
				object.setDuration(newDuration);

				if (adjustDates) {
					object.setDueDate(DateUtils.rollDays(object.getStartDate(), newDuration));
				}
				GwtEvent event = new PropertyDidChangeEvent<TaskWidgetData>(object, "duration", oldDuration, newDuration);
                manager.fireEvent(event);
            }
        });


        final Column<TaskWidgetData, String> progressColumn = new Column<TaskWidgetData, String>(durationCell) {
            @Override
            public String getValue(TaskWidgetData object) {
                return String.valueOf(object.getProgress());
            }
        };

        progressColumn.setFieldUpdater(new FieldUpdater<TaskWidgetData, String>() {
            public void update(int index, TaskWidgetData object, String value) {
				int newProgress = Integer.parseInt(value);
				int oldProgress = object.getProgress();
				object.setProgress(newProgress);
				GwtEvent event = new PropertyDidChangeEvent<TaskWidgetData>(object, "progress", oldProgress, newProgress);
                manager.fireEvent(event);
            }
        });
        
        detailColumns = new ArrayList<Column<TaskWidgetData, ?>>(3);
        detailColumns.add(taskColumn);
        detailColumns.add(startDateCol);
        detailColumns.add(dueDateCol);
        detailColumns.add(durationColumn);
        detailColumns.add(progressColumn);

        detailColumns = Collections.unmodifiableList(detailColumns);    // freeze these

        table.setStyleProvider(new CellStyleProvider<TaskWidgetData>() {

            public String[] getStyleNames(TaskWidgetData data, Column<TaskWidgetData, ?> taskWidgetDataColumn) {
                if (taskWidgetDataColumn == taskColumn) {
                    return DESC_STYLE_CLASS;
                }
                if (taskWidgetDataColumn == startDateCol) {
                    return SDATE_STYLE_CLASS;
                }
                if (taskWidgetDataColumn == dueDateCol) {
                    return DDATE_STYLE_CLASS;
                }
                if (taskWidgetDataColumn == durationColumn) {
                    return DUR_STYLE_CLASS;
                }
                if (taskWidgetDataColumn == progressColumn) {
                    return PROGRESS_STYLE_CLASS;
                }
                return null;
            }
        });
        // set up standard headers
        final TextCell standardHeaderCell = new TextCell();
        final List<Header<?>> TOP_ROW = new ArrayList<Header<?>>(2);
        TOP_ROW.addAll(asList(
				new FlexHeader<String>(standardHeaderCell, 5, "Description"),
				new FlexHeader<String>(standardHeaderCell, "Calendar")
		));

        final List<Header<?>> SECOND_ROW = new ArrayList<Header<?>>();
        SECOND_ROW.addAll(asList(
				new FlexHeader<String>(standardHeaderCell, "Task"),
				new FlexHeader<String>(standardHeaderCell, "Start Date"),
				new FlexHeader<String>(standardHeaderCell, "Due Date"),
				new FlexHeader<String>(standardHeaderCell, "# Days"),
				new FlexHeader<String>(standardHeaderCell, "Progress")
		));

        table.addHeaderRow(TOP_ROW);
        table.addHeaderRow(SECOND_ROW);

        for (Column<TaskWidgetData, ?> column : detailColumns) {
            table.addColumn(column);
        }
    }

    private CellTreeTable<TaskWidgetData> table;
    private HandlerManager manager;

    private static final String[] DESC_STYLE_CLASS = { "tdsc" };
    private static final String[] SDATE_STYLE_CLASS = { "tsd" };
    private static final String[] DDATE_STYLE_CLASS = { "tdd" };
    private static final String[] PROGRESS_STYLE_CLASS = { "tprog" };
    private static final String[] DUR_STYLE_CLASS = { "tdur" };

	private boolean adjustDates = true;
}
