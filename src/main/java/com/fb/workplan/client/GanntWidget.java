package com.fb.workplan.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fb.workplan.client.DateUtils;
import com.fb.workplan.client.PropertyDidChangeEvent;
import com.fb.workplan.client.PropertyDidChangeEventHandler;
import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.CellTreeTable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mostly orchestrates events and data across several subwidgets.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class GanntWidget extends Composite {

	public void addAll(List<TaskWidgetData> all) {
		for (TaskWidgetData data : all) {
			addRecursively(data);
		}
		for (TaskWidgetData data : all) {
			if (!data.getChildren().isEmpty()) {
				fillParentDates(data);
			}
		}
		calendarController.updateCalendar();
	}

	private void addRecursively(TaskWidgetData data) {
		if (data.getParent() != null) {
			if (data.getParent().getState() == 0) {
				addRecursively(data.getParent());
			}
			table.addItem(data.getParent(), data);
		} else {
			table.addItem(data);
		}
		data.setState(1);
		displayModel.add(data);
	}

	private class InnerMapElementMapper implements ElementMapper {
		@Override
		public void setElementForValue(Element element, TaskWidgetData value) {
			GWT.log("placed element for task: " + value.getId() + " element: " + element);
			taskElementMap.put(value.getId(), element);
		}
	}

	List<TaskWidgetData> displayModel = new ArrayList<TaskWidgetData>(50);
	FlowPanel wrapperPanel;
	Map<String, Element> taskElementMap;
	final ElementMapper elementMapper;
	final ListBox calendarDropDown;

	@SuppressWarnings({"unchecked"})
	public GanntWidget() {
		table = new CellTreeTable<TaskWidgetData>();
		table.addStyleName("gannt-table");
		manager = new HandlerManager(this);
		taskElementMap = new HashMap<String, Element>();
		elementMapper = new InnerMapElementMapper();
		calendarDropDown = new ListBox();

		GanntCanvasRenderer canvasRenderer = new GanntCanvasRenderer(table, taskElementMap);
		detailsController = new GanntGridController(table, displayModel, manager, canvasRenderer);
		calendarController = new GanntCalendarController(table, displayModel, manager, elementMapper);
		initListBox();
		wrapperPanel = new FlowPanel();
		wrapperPanel.add(table);

		FlowPanel calendarPanel = new FlowPanel();
		calendarPanel.add(new InlineLabel("Calendar setting:"));
		calendarPanel.add(calendarDropDown);
		calendarPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
		calendarPanel.getElement().getStyle().setTop(0, Style.Unit.EM);
		calendarPanel.getElement().getStyle().setLeft(2, Style.Unit.EM);
		initWidget(wrapperPanel);
		wrapperPanel.add(calendarPanel);
		wrapperPanel.getElement().getStyle().setPadding(2, Style.Unit.EM);
		wrapperPanel.getElement().getStyle().setPosition(Style.Position.RELATIVE);

		manager.addHandler(PropertyDidChangeEvent.getType(), createUpdateDependenciesHandler());
		manager.addHandler(PropertyDidChangeEvent.getType(), createRenderHandler());
	}

	private void initListBox() {
		// TODO: i18n
		calendarDropDown.addItem("Monthly", ChartType.MONTHLY.name());
		calendarDropDown.addItem("Weekly", ChartType.WEEKLY.name());
		calendarDropDown.addItem("Daily", ChartType.DAILY.name());

		calendarDropDown.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String selectionName = calendarDropDown.getValue(calendarDropDown.getSelectedIndex());
				ChartType selectionType = ChartType.valueOf(selectionName);
				calendarController.setChartType(selectionType);
				calendarController.updateCalendar();
			}
		});

	}

	protected void initWidget(Widget table) {
		super.initWidget(table);
		detailsController.init();
	}

	// TODO: this code is getting duplicated in cell tree because of model management issues, investigate and fix
	public void addTask(TaskWidgetData data) {
		if (!data.getChildren().isEmpty()) {
			fillParentDates(data);
		}
		if (data.getParent() != null) {
			table.addItem(data.getParent(), data);
		} else {
			table.addItem(data);
		}
		displayModel.add(data);
		calendarController.updateCalendar();
	}

	private void fillParentDates(TaskWidgetData data) {
		Date startDate = getStartDateRecursive(data);
		Date dueDate = getDueDateRecursive(data);
		data.setDuration(0);
		data.setStartDate(startDate);
		data.setDueDate(dueDate);
		data.setDuration(DateUtils.getDaysDiff(startDate, dueDate));
	}

	private Date getDueDateRecursive(TaskWidgetData data) {
		if (data.getChildren().isEmpty()) {
			return data.getDueDate();
		} else {
			List<TaskWidgetData> children = data.getChildren();
			Date maxDate = getDueDateRecursive(children.get(0));
			assert maxDate != null : "Non-parent nodes cannot have null startDate";
			for (int i = 1; i < children.size(); i++) {
				Date tdate = getDueDateRecursive(children.get(i));
				maxDate = tdate.after(maxDate) ? tdate : maxDate;
			}
			return maxDate;
		}
	}

	private Date getStartDateRecursive(TaskWidgetData data) {
		if (data.getChildren().isEmpty()) {
			return data.getStartDate();
		} else {
			List<TaskWidgetData> children = data.getChildren();
			Date minDate = getStartDateRecursive(children.get(0));
			assert minDate != null : "Non-parent nodes cannot have null startDate";
			for (int i = 1; i < children.size(); i++) {
				Date tdate = getStartDateRecursive(children.get(i));
				minDate = tdate.before(minDate) ? tdate : minDate;
			}
			return minDate;
		}
	}

	@SuppressWarnings({"unchecked"})
	public HandlerRegistration addValueChangeHandler(PropertyDidChangeEventHandler<TaskWidgetData> valueChangeHandler) {
		return manager.addHandler(PropertyDidChangeEvent.getType(), valueChangeHandler);
	}

	private final CellTreeTable<TaskWidgetData> table;
	private final HandlerManager manager;

	private final GanntGridController detailsController;
	private final GanntCalendarController calendarController;

	private PropertyDidChangeEventHandler<TaskWidgetData> createUpdateDependenciesHandler() {
		return new PropertyDidChangeEventHandler<TaskWidgetData>() {
			@Override
			public void onPropertyChange(TaskWidgetData owner, String propertyName, Object oldValue, Object newValue) {
				TaskWidgetData parent = owner.getParent();
				if (parent != null) {
					if (propertyName.equals("startDate")) {
						Date parentStartDate = parent.getStartDate();
						Date taskMinDate = (Date) newValue;
						if (taskMinDate.before(parentStartDate)) {
							parent.setStartDate(taskMinDate);
							manager.fireEvent(new PropertyDidChangeEvent<TaskWidgetData>(parent, propertyName, parentStartDate, taskMinDate));
							recalculateParentDuration(parent, true);
						}
					} else if (propertyName.equals("dueDate")) {
						Date parentDueDate = parent.getDueDate();
						Date taskDueDate = (Date) newValue;
						if (taskDueDate.after(parentDueDate)) {
							parent.setDueDate(taskDueDate);
							manager.fireEvent(new PropertyDidChangeEvent<TaskWidgetData>(parent, propertyName, parentDueDate, taskDueDate));
							recalculateParentDuration(parent, true);
						}
					} else if (propertyName.equals("duration")) {
						recalculateParentDuration(parent, true);
					}
				}
				if (!owner.getDependencies().isEmpty()) {
					Date newDueDate = null;
					if (propertyName.equals("startDate")) {
						newDueDate = DateUtils.rollDays((Date) newValue, owner.getDuration());
					} else if (propertyName.equals("dueDate")) {
						newDueDate = (Date) newValue;
					}
					if (newDueDate != null) {
						for (TaskWidgetData dependency : owner.getDependencies()) {
							if (newDueDate.after(dependency.getStartDate())) {
								Date oldDependencyStartDate = dependency.getStartDate();
								dependency.setStartDate(newDueDate);
								manager.fireEvent(new PropertyDidChangeEvent<TaskWidgetData>(dependency, "startDate", oldDependencyStartDate, newDueDate));
							}
						}
					}
				}
			}
		};
	}

	private void recalculateParentDuration(TaskWidgetData parent, boolean fire) {
		Integer oldDuration = parent.getDuration();
		Date startDate = getStartDateRecursive(parent);
		Date dueDate = getDueDateRecursive(parent);
		int duration = DateUtils.getDaysDiff(startDate, dueDate);
		parent.setDuration(duration);
		manager.fireEvent(new PropertyDidChangeEvent<TaskWidgetData>(parent, "duration", oldDuration, duration));
	}

	private PropertyDidChangeEventHandler<TaskWidgetData> createRenderHandler() {
		return new PropertyDidChangeEventHandler<TaskWidgetData>() {
			@Override
			public void onPropertyChange(TaskWidgetData owner, String propertyName, Object oldValue, Object newValue) {
				dirtyItems.add(owner);
				if (renderCommand == null) {
					renderCommand = new Scheduler.ScheduledCommand() {
						@Override
						public void execute() {
							for (TaskWidgetData dirty : dirtyItems) {
								table.refresh(dirty);
							}
							dirtyItems.clear();
							renderCommand = null;
						}
					};
					Scheduler.get().scheduleFinally(renderCommand);
				}
			}
		};
	}

	Scheduler.ScheduledCommand renderCommand = null;
	List<TaskWidgetData> dirtyItems = new LinkedList<TaskWidgetData>();
}
