package com.fb.workplan.client.release;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fb.workplan.client.CanvasCellRenderer;
import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.CellTreeTable;

class GanntCanvasRenderer implements CanvasCellRenderer<List<TaskWidgetData>> {

	public GanntCanvasRenderer(CellTreeTable<TaskWidgetData> table, List<TaskWidgetData> taskList, Map<TaskWidgetData, Element> elementMap) {
		this.table = table;
		this.taskList = taskList;
		this.elementMap = elementMap;
	}

	public void render(CanvasElement canvasElement, List<TaskWidgetData> value, Cell.Context context, Element parent) {

		// TODO: resize properly

		// collect all dependency coordinates in format <start_x,start_y>, <end_x, end_y>
		List<Point[]> dependencies = generateDependencyCoordinates(value);


	}

	private List<Point[]> generateDependencyCoordinates(List<TaskWidgetData> value) {
		List<TaskWidgetData[]> dependencies = generateDependencyList(value);
		return lookupDependencyCoordinates(dependencies);
	}

	private List<Point[]> lookupDependencyCoordinates(List<TaskWidgetData[]> dependencies) {
		//
	}

	private List<TaskWidgetData[]> generateDependencyList(List<TaskWidgetData> tasks) {
		List<TaskWidgetData[]> result = new LinkedList<TaskWidgetData[]>();
		for (TaskWidgetData task : tasks) {
			if (!task.getDependencies().isEmpty()) {
				for (TaskWidgetData dependency : task.getDependencies()) {
					TaskWidgetData[] dep_tuple = new TaskWidgetData[2];
					dep_tuple[0] = task;
					dep_tuple[1] = dependency;
					result.add(dep_tuple);
				}
			}
		}
		return result;
	}

	private class Point {
		double x;
		double y;
	}

	private HandlerManager manager;
	private int insertColumn;
	private final CellTreeTable<TaskWidgetData> table;
	private final List<TaskWidgetData> taskList;
	private final Map<TaskWidgetData, Element> elementMap;
}
