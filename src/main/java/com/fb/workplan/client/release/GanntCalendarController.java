package com.fb.workplan.client.release;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.fb.workplan.client.DateFormatUtils;
import com.fb.workplan.client.DateUtils;
import com.fb.workplan.client.PropertyDidChangeEvent;
import com.fb.workplan.client.PropertyDidChangeEventHandler;
import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.CellTreeTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.FlexHeader;
import com.google.gwt.user.cellview.client.Header;

import static com.fb.workplan.client.DateUtils.*;

/**
 * Deals with floaters and calendar columns in the cell table.
 */
class GanntCalendarController {

	public GanntCalendarController(CellTreeTable<TaskWidgetData> table, List<TaskWidgetData> displayModel, HandlerManager manager, ElementMapper elementMapper) {
		this.table = table;
		this.displayModel = displayModel;
		chartType = ChartType.WEEKLY;
		textCell = new TextCell();
		renderedMonths = new ArrayList<Date>(12 * 2); // two years
		manager.addHandler(PropertyDidChangeEvent.getType(), refreshTableOnTaskChange);
		this.elementMapper = elementMapper;
	}

	void updateCalendar() {
		rebuildCalendarTable();
	}

	private Date firstStartDate() {
		for (TaskWidgetData task : displayModel) {
			if (task.getStartDate() != null) {
				return task.getStartDate();
			}
		}
		return null;
	}


	private int getMonthDiff(Date minDate, Date maxDate) {
		return Math.abs(getMonthDifference(minDate, maxDate));
	}

	private void refreshCalendarHeaders(Date startDate, Date dueDate) {
		int insertedColumn = 0;
		Date startMonth;
		int numMonthsToInsert;
		if (renderedMonths.isEmpty()) {
			startMonth = startDate;
			numMonthsToInsert = DateUtils.getMonthDifference(startMonth, dueDate);
		} else {
			Date currentMinMonth = renderedMonths.get(0);
			Date currentMaxMonth = renderedMonths.get(renderedMonths.size() - 1);
			if (startDate.before(currentMinMonth)) {
				startMonth = DateUtils.beginningOfMonth(startDate);
				numMonthsToInsert = DateUtils.getMonthDifference(startMonth, currentMinMonth);
			} else if (dueDate.after(currentMaxMonth)) {
				startMonth = DateUtils.rollMonth(currentMaxMonth, 1);
				numMonthsToInsert = DateUtils.getMonthDifference(startMonth, dueDate);
				insertedColumn = renderedMonths.size() - 1;
			} else {
				return;	// no change in calendar dates
			}
		}
		insertColumn(insertedColumn, startMonth, numMonthsToInsert);
	}

	/**
	 * inserts a column in the specified index , shifting the existing columns if necessary
	 *
	 * @param index
	 * @param startDate
	 * @param numMonthsToInsert
	 */
	private void insertColumn(int index, Date startDate, int numMonthsToInsert) {
		List<Header<?>> wrapper = new ArrayList<Header<?>>();
		int k = 0;
		for (int i = 0; i < numMonthsToInsert; i++) {
			int insertionIndex = index + i;
			Date month = DateUtils.rollMonth(startDate, i);
			renderedMonths.add(insertionIndex, month);

			List<Date> dateBreakdowns = getMonthlyDateBreakdowns(month);
			List<Header<?>> breakdownHeaders = convertToHeaders(dateBreakdowns);
			int monthHeaderColspan = dateBreakdowns.size() > 0 ? dateBreakdowns.size() : 1;

			Header<String> monthHeader = getMonthHeader(month, monthHeaderColspan);
			List<Column<TaskWidgetData, TaskWidgetData>> columns = getRenderColumns(month, dateBreakdowns);

			wrapper.clear();
			wrapper.add(monthHeader);

			table.insertHeaders(TOP_ROW, insertionIndex + 1, wrapper);
			table.insertHeaders(DETAIL_ROW, insertionIndex + 5, breakdownHeaders);

			for (Column<TaskWidgetData, TaskWidgetData> column : columns) {
				table.insertColumn(insertionIndex + 5 + k, column);
				k++;
			}
		}
	}

	private void rebuildCalendarTable() {

		// TODO: this function needs to be refactored. header rows and columns should be added in such a way as to not interfere with grid headers/columns
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
		int numMonths = getMonthDiff(minDate, maxDate) + 1;
		List<List<Header<?>>> headers = table.getHeaderRows();
		assert headers != null : "Header rows must have been initialized by this point";
		assert headers.size() == 3 : "Header rows do not have the expected size";
		List<Header<?>> canvasRow = headers.get(CANVAS_ROW);
		List<Header<?>> topHeader = headers.get(TOP_ROW);
		List<Header<?>> bottomHeader = headers.get(DETAIL_ROW);

		List<Header<?>> replacementTopHeaders = new LinkedList<Header<?>>();
		List<Header<?>> replacementBottomHeaders = new LinkedList<Header<?>>();
		List<Column<TaskWidgetData, ?>> ganntColumns = new LinkedList<Column<TaskWidgetData, ?>>();
		for (int i = 1; i <= numMonths; i++) {
			int year = (minDate.getYear() + 1900) + i / 12;
			int month = minDate.getMonth() + i % 12;
			Date monthDate = date(year, month, 1);

			List<Date> dateBreakdowns = getMonthlyDateBreakdowns(monthDate);
			List<Header<?>> breakdownHeaders = convertToHeaders(dateBreakdowns);
			int monthHeaderColspan = dateBreakdowns.size() > 0 ? dateBreakdowns.size() : 1;

			Header<String> monthHeader = getMonthHeader(monthDate, monthHeaderColspan);
			replacementTopHeaders.add(monthHeader);
			replacementBottomHeaders.addAll(breakdownHeaders);

			List<Column<TaskWidgetData, TaskWidgetData>> columns = getRenderColumns(monthDate, dateBreakdowns);
			ganntColumns.addAll(columns);

			renderedMonths.add(monthDate);
		}

		final int numBottomHeaders = Math.min(bottomHeader.size(), NUMBER_OF_GRID_COLUMNS);
		replacementBottomHeaders.addAll(0, bottomHeader.subList(0, numBottomHeaders));
		replacementTopHeaders.add(0, topHeader.get(0));
		List<List<Header<?>>> allHeaders = new LinkedList<List<Header<?>>>();
		allHeaders.add(canvasRow);
		allHeaders.add(replacementTopHeaders);
		allHeaders.add(replacementBottomHeaders);
		table.setHeaderRows(allHeaders);
		table.replaceColumns(NUMBER_OF_GRID_COLUMNS, ganntColumns);
	}

	private List<Column<TaskWidgetData, TaskWidgetData>> getRenderColumns(Date month, List<Date> dateBreakdowns) {
		List<Column<TaskWidgetData, TaskWidgetData>> result;
		int arraySize = dateBreakdowns != null && !dateBreakdowns.isEmpty() ? dateBreakdowns.size() : 1;
		result = new ArrayList<Column<TaskWidgetData, TaskWidgetData>>(arraySize);
		if (chartType.equals(ChartType.MONTHLY)) {
			result.add(createColumn(month));
		} else {
			assert dateBreakdowns != null;
			for (Date date : dateBreakdowns) {
				result.add(createColumn(date));
			}
		}
		return result;
	}

	private Column<TaskWidgetData, TaskWidgetData> createColumn(Date date) {
		Date durationEndDate = getDurationEndDate(date);
		Date durationStartDate = getDurationStartDate(date);
		GanntFloaterCell cell = new GanntFloaterCell(durationStartDate, durationEndDate, chartType, elementMapper);
		return new SimpleTaskColumn(cell);
	}

	private Date getDurationStartDate(Date date) {
		if (chartType.equals(ChartType.MONTHLY)) {
			return beginningOfMonth(date);
		}
		if (chartType.equals(ChartType.WEEKLY)) {
			return beginningOfWeek(date, true);
		}
		if (chartType.equals(ChartType.DAILY)) {
			return date(year(date), month(date), dayOfMonth(date));
		}
		throw new IllegalArgumentException("Unknown chart type: " + chartType.name());
	}

	private Date getDurationEndDate(Date date) {
		date = date(year(date), month(date), dayOfMonth(date)); // resets the clock at 0:0:0:0000 HRS
		if (chartType.equals(ChartType.MONTHLY)) {
			Date nextMonth = rollMonth(beginningOfMonth(date), 1);
			return new Date(nextMonth.getTime() - 1);
		}
		if (chartType.equals(ChartType.DAILY)) {
			Date tomorrow = DateUtils.rollDays(date, 1);
			return new Date(tomorrow.getTime() - 1);
		}
		if (chartType.equals(ChartType.WEEKLY)) {
			Date nextMonday = rollDays(beginningOfWeek(date, true), 7);
			return new Date(nextMonday.getTime() - 1);
		}
		throw new IllegalArgumentException("Unknown chart type: " + chartType.name());
	}

	private List<Date> getMonthlyDateBreakdowns(Date month) {
		if (chartType.equals(ChartType.MONTHLY)) {
			return Collections.emptyList();
		}
		if (chartType.equals(ChartType.DAILY)) {
			return getDatesForMonth(month);
		}
		if (chartType.equals(ChartType.WEEKLY)) {
			return getMondaysForMonth(month);
		}
		throw new IllegalArgumentException("Unknown chart type: " + chartType.name());
	}

	private Header<String> getMonthHeader(final Date month, int numMondays) {
		final String formattedDate = DateFormatUtils.monthFormat(month);

		return new FlexHeader<String>(textCell, numMondays, formattedDate);

	}

	private List<Header<?>> convertToHeaders(List<Date> breakdownDates) {
		List<Header<?>> result = new ArrayList<Header<?>>(breakdownDates.size());
		for (Date date : breakdownDates) {
			if (chartType.equals(ChartType.WEEKLY)) {
				result.add(new FlexHeader<String>(textCell, DateFormatUtils.formatWeek(date)));
			}
			if (chartType.equals(ChartType.DAILY)) {
				result.add(new FlexHeader<String>(textCell, DateFormatUtils.formatDay(date)));
			}
			if (chartType.equals(ChartType.MONTHLY)) {
				// empty header for month
				result.add(new FlexHeader<String>(textCell, ""));
			}
		}
		return result;
	}

	private class SimpleTaskColumn extends Column<TaskWidgetData, TaskWidgetData> {
		public SimpleTaskColumn(Cell<TaskWidgetData> taskWidgetDataCell) {
			super(taskWidgetDataCell);
		}

		@Override
		public TaskWidgetData getValue(TaskWidgetData object) {
			return object;
		}
	}

	private final ChartType chartType;
	private final CellTreeTable<TaskWidgetData> table;
	private final List<TaskWidgetData> displayModel;
	private final List<Date> renderedMonths;
	private final ElementMapper elementMapper;

	private final TextCell textCell;
	private final static int NUMBER_OF_GRID_COLUMNS = 5;	// desc, start date, end date, duration

	final int CANVAS_ROW = 0;
	final int TOP_ROW = 1;
	final int DETAIL_ROW = 2;

	private final PropertyDidChangeEventHandler<TaskWidgetData> refreshTableOnTaskChange = new PropertyDidChangeEventHandler<TaskWidgetData>() {
		@Override
		public void onPropertyChange(TaskWidgetData owner, String propertyName, Object oldValue, Object newValue) {
			refreshCalendarHeaders(owner.getStartDate(), owner.getDueDate());
			table.refresh(owner);
			table.refreshHeaderRow(CANVAS_ROW);
		}
	};
}
