package com.fb.workplan.client.release;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fb.workplan.client.CanvasCellRenderer;
import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.cellview.client.CellTreeTable;

class GanntCanvasRenderer implements CanvasCellRenderer<List<TaskWidgetData>> {

	public GanntCanvasRenderer(CellTreeTable<TaskWidgetData> table, Map<String, Element> elementMap) {
		this.table = table;
		this.elementMap = elementMap;
	}

	public void render(CanvasElement canvasElement, List<TaskWidgetData> value, Cell.Context context, Element parent) {
		int height = table.getElement().getClientHeight();
		int width = table.getElement().getClientWidth();
		canvasElement.setHeight(height);
		canvasElement.setWidth(width);	// FIXME: need to pass this in and have the option to re-calculate it as the calendar changes

		// collect all dependency coordinates in format <start_x,start_y>, <end_x, end_y>
		List<Point[]> dependencies = generateDependencyCoordinates(canvasElement, value);

		Context2d context2d = canvasElement.getContext2d();
		for (Point[] tuple : dependencies) {
			render(canvasElement, context2d, tuple, width, height);
		}
	}

	private void render(CanvasElement canvasElement, Context2d context, Point[] tuple, int width, int height) {
		// FIXME: the client height of the floater elements should be pulled from the CSS
		context.clearRect(0, 0, height, width);
		context.setStrokeStyle("#666");
		context.setLineWidth(2);
		context.beginPath();

		drawDepencencyLine(context, tuple[0].x, tuple[0].y, tuple[1].x, tuple[1].y, 20);

		context.stroke();
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

	private List<Point[]> generateDependencyCoordinates(CanvasElement canvasElement, List<TaskWidgetData> value) {
		List<TaskWidgetData[]> dependencies = generateDependencyList(value);
		return lookupDependencyCoordinates(canvasElement, dependencies);
	}

	private List<Point[]> lookupDependencyCoordinates(CanvasElement canvasElement, List<TaskWidgetData[]> dependencies) {
		ArrayList<Point[]> result = new ArrayList<Point[]>();
		for (TaskWidgetData[] tuple : dependencies) {
			Point[] point_tuple = new Point[2];
			point_tuple[0] = lookupDependencyCoordinates(canvasElement, tuple[0], false);
			if (point_tuple[0] == null) {
				continue;
			}
			point_tuple[1] = lookupDependencyCoordinates(canvasElement, tuple[1], true);
			if (point_tuple[1] == null) {
				continue;
			}
			result.add(point_tuple);
		}
		return result;

	}

	private Point lookupDependencyCoordinates(CanvasElement canvasElement, TaskWidgetData task, boolean startOfElement) {
		Element element = elementMap.get(task.getId());
		if (element == null) {
			GWT.log("CANNOT FIND ELEMENT FOR TASK: " + task.getId());
			return null;
		}
		double x = getHorizontalOffsetToRoot(element);
		double baseX = getHorizontalOffsetToRoot(canvasElement);
		x = x - baseX;

		double y = getVerticalOffsetToRoot(element);
		double baseY = getVerticalOffsetToRoot(canvasElement);
		y = y - baseY;
		
		Point point = new Point(x, y);

		point.y += element.getClientHeight() / 2;
		if (!startOfElement) {
			point.x += element.getClientWidth();
		}
		return point;
	}


	private double getVerticalOffsetToRoot(Element element) {
		double y = 0;
		while (!element.equals(table.getElement().getOffsetParent())) {
			y += element.getOffsetTop();
			element = element.getOffsetParent();
		}
		return y;
	}

	private double getHorizontalOffsetToRoot(Element element) {
		double x = 0;
		while (!element.equals(table.getElement().getOffsetParent())) {
			x += element.getOffsetLeft();
			element = element.getOffsetParent();
		}
		return x;
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
		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		double x;
		double y;
	}

	private final CellTreeTable<TaskWidgetData> table;
	private final Map<String, Element> elementMap;
}
