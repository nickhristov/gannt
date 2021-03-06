package com.google.gwt.user.cellview.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.Range;

public class CellTreeTable<T> extends Widget implements IsWidget {

    public void insertHeaders(int headerRow, int insertIndex, List<Header<?>> insertHeaders) {
		if (insertHeaders.isEmpty()) {
			return;
		}
        // logical insert:
        flexHeaderRows.get(headerRow).addAll(insertIndex, insertHeaders);
		
		// physical insert:
		for(int i = 0; i < insertHeaders.size(); i++) {
			Header<?> headerColumn = insertHeaders.get(i);
			int renderIndex = i + insertIndex;
			TableRowElement rowElement = ensureRow(thead, headerRow);
			renderHeader(headerRow, renderIndex, rowElement, headerColumn, true);
		}
    }

	public void refreshHeaderRow(int headerRow) {
		TableRowElement row = ensureRow(thead, headerRow);
		renderHeaderRow(headerRow, row, flexHeaderRows.get(headerRow), false);
	}

	private class RenderingCommand implements Scheduler.ScheduledCommand {
        public void execute() {
            // clear state:
            deferredRenderer = null;
            renderAllColumns();
        }
    }

    private final TableElement table;
    private final List<List<Header<?>>> flexHeaderRows;
    private final List<Column<T, ?>> columns;
    private CellStyleProvider<T> styleProvider;

    public CellTreeTable() {
        table = Document.get().createTableElement();
        thead = table.createTHead();
        setElement(table);
        flexHeaderRows = new LinkedList<List<Header<?>>>();
        columns = new LinkedList<Column<T, ?>>();
        rootNode = new DataNode<T>();
        tbody = createBodyIfNecessary();
        sinkEvents(Event.FOCUSEVENTS | Event.MOUSEEVENTS | Event.KEYEVENTS);
        table.setClassName("CellTreeTable");
    }

    public void setStyleProvider(CellStyleProvider<T> styleProvider) {
        this.styleProvider = styleProvider;
    }

    public void addHeaderRow(List<Header<?>> headerRow) {
        flexHeaderRows.add(headerRow);
        renderHeaders();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public List<List<Header<?>>> getHeaderRows() {
        return Collections.unmodifiableList(flexHeaderRows);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setHeaderRows(List<List<Header<?>>> newHeaders) {
        flexHeaderRows.clear();
        flexHeaderRows.addAll(newHeaders);
        renderHeaders();
    }

    /**
     * Replaces columns at index startIndex. All columns from
     * startIndex are replaced with the contents of newColumns parameter.
     * 
     * @param startIndex
     * @param newColumns
     */
    public void replaceColumns(int startIndex, List<Column<T, ?>> newColumns) {
        if (columns == null) {
            throw new NullPointerException("Cannot set null columns");
        }
        if (startIndex < 0) {
            throw new IllegalArgumentException("Invalid start index, must be >= 0, given index was: " + startIndex);
        }
        startIndex = Math.min(columns.size(), startIndex);
        int columnsToRemove = columns.size() - startIndex;
        for(int i = 0; i < columnsToRemove; i++) {
            columns.remove(startIndex);
        }
        columns.addAll(newColumns);
        for(int i = startIndex; i < columns.size(); i++) {
            Column<T, ?> column = columns.get(i);
            renderColumnAtIndex(i, column, true, false);
            sinkEvents(column);
        }
    }

    public void refresh(final Range columnRange, final Range rowRange) {
        assert columns != null && columns.size() >= columnRange.getLength();
        
        Visitor<T> renderer = new Visitor<T>() {
            public boolean visit(int row, DataNode<T> visitNode, int indentLevel) {
                if ( row >= rowRange.getStart() && row < (rowRange.getStart() + rowRange.getLength())) {
                    int columnEnd = columnRange.getStart() + columnRange.getLength();
                    for(int ci = columnRange.getStart(); ci < columnEnd; ci++) {
                        Column<T, ?> column = columns.get(ci);
                        render(tbody, visitNode, row, ci, column, indentLevel, false);
                    }
                }
                return row < (rowRange.getLength() + rowRange.getStart());
            }
        };

        CellTreeTableModelManager.walk(rootNode.getFirstChild(), renderer, true, 0, 0);
    }

    public void refresh(T value) {
        TreeContext<T> data = CellTreeTableModelManager.findInTree(rootNode.getFirstChild(), value);
        int startIndex = data.rowIndex;
        Range rowRange = new Range(startIndex, 1);
        Range allColumnsRange = new Range(0, columns.size());
        refresh(allColumnsRange, rowRange);
    }

    public void addColumn(Column<T, ?> column) {
        int index = columns.size();
        columns.add(column);
        renderHeaders();
        renderColumnAtIndex(index, column, true, false);
        sinkEvents(column);
    }

    public void insertColumn(int position, Column<T, ?> column) {
        if (position < 0) {
            position = columns.size() + ( position % columns.size() ) ;
            insertColumn(position, column);
            return;
        }
        if (position == columns.size()) {
            addColumn(column);
            return;
        }
        if (position > columns.size()) {
            throw new IllegalArgumentException("Position index is larger than existing column size of " + columns.size());
        }
        columns.add(position, column);
        renderColumnAtIndex(position, column, true, true);
        sinkEvents(column);
    }

    private void sinkEvents(Column<T, ?> column) {
        Set<String> consumedEvents = new HashSet<String>();
		Set<String> cellEvents = column.getCell().getConsumedEvents();
        if (cellEvents != null) {
            consumedEvents.addAll(cellEvents);
        }
        CellBasedWidgetImpl.get().sinkEvents(this, consumedEvents);

    }

    private TableSectionElement createBodyIfNecessary() {
        NodeList<TableSectionElement> bodies = table.getTBodies();
        TableSectionElement body = bodies.getItem(0);
        if (body == null) {
            body = Document.get().createTBodyElement();
            table.appendChild(body);
        }
        return body;
    }

    private void renderColumnAtIndex(final int index, final Column<T, ?> column, boolean renderSiblings, final boolean forceInsert) {
        // TODO: refactor this method out, replace with progressive rendering method
        if (rootNode.getFirstChild() != null) {
            final TableSectionElement body = createBodyIfNecessary();
            Visitor<T> visitor = new Visitor<T>() {
                public boolean visit(int row, DataNode<T> renderNode, int indentLevel) {
                    render(body, renderNode, row, index, column, index == 0 ? indentLevel : 0, forceInsert);
                    return true;
                }
            };
            CellTreeTableModelManager.walk(rootNode.getFirstChild(), visitor, renderSiblings, 0, 0);
        }
    }


    private TableRowElement ensureRow(TableSectionElement body, int rowIndex) {
        TableRowElement rowElement;
        if (body.getRows().getLength() < rowIndex) {
            ensureRow(body, rowIndex-1);
        }
        if (body.getRows().getLength() == rowIndex) {
            rowElement = body.insertRow(rowIndex);
        } else {
            rowElement = body.getRows().getItem(rowIndex);
        }
        return rowElement;
    }

    private TableCellElement ensureCell(TableRowElement rowElement, int columnIndex, boolean forceInsert) {
        TableCellElement cell;

        if (rowElement.getCells().getLength() < columnIndex) {
            ensureCell (rowElement, columnIndex - 1, false);
        }
        if (rowElement.getCells().getLength() <= columnIndex) {
            cell = rowElement.insertCell(columnIndex);
        } else {
            if (forceInsert) {
                cell = rowElement.insertCell(columnIndex);
            } else {
                cell = rowElement.getCells().getItem(columnIndex);
            }
        }
        return cell;
    }


    private void render(TableSectionElement body, TreeContext<T> context, int columnIndex, boolean forceInsert) {
        render(body, context.getData(), context.getRowIndex(), columnIndex, columns.get(columnIndex), context.getIndentLevel(), forceInsert);
    }
    
    private void render(TableSectionElement body, DataNode<T> renderNode,
                        int rowIndex, int columnIndex, Column<T, ?> column, int indent, boolean forceInsert) {
        TableRowElement rowElement = ensureRow(body, rowIndex);
        TableCellElement cell = ensureCell(rowElement, columnIndex, forceInsert);
        if (indent > 0 && columnIndex == 0) {
            cell.getStyle().setPaddingLeft(indent * 1.2, Style.Unit.EM);
        } else {
            cell.getStyle().clearPaddingLeft();
        }
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        SafeHtmlBuilder outerDivBuilder = new SafeHtmlBuilder();

        renderShell(outerDivBuilder, renderNode, columnIndex);
        outerDivBuilder.append(builder.toSafeHtml());
        cell.setInnerHTML(outerDivBuilder.toSafeHtml().asString());
        DivElement wrapper = getWrapper(cell);
        wrapper.removeAttribute("class");
        if (styleProvider != null) {
            String[] styleNames = styleProvider.getStyleNames(renderNode.getValue(), column);
            if (styleNames!= null && styleNames.length > 0) {
                for(String styleClass: styleNames) {
                    wrapper.addClassName(styleClass);
                }
            }
        }
        Cell.Context context = new Cell.Context(rowIndex, columnIndex, null);

        Cell renderCell = column.getCell();
        Object value = column.getValue(renderNode.getValue());
		assert value != null : "Value cannot be null for column at index : " + columnIndex;
        renderCell.setValue(context, wrapper, value);
//        column.render(context, renderNode.getValue(), builder);

    }

    private void renderShell(SafeHtmlBuilder outerDivBuilder, DataNode<T> renderNode, int columnIndex) {
        String classes = "Cell";
        if (columnIndex == 0) {
            classes = "Cell FirstCell";
        }
        outerDivBuilder.appendHtmlConstant("<div class=\"" + classes + "\"><div></div>");

        if (renderNode.getFirstChild() != null && columnIndex == 0) {
            if (renderNode.isExpanded()) {
                outerDivBuilder.appendHtmlConstant("<img class=\"CellTreeTableCollapseElement\" src=\"" + imageResources.expanded().getURL() + "\"/>");
            } else {
                outerDivBuilder.appendHtmlConstant("<img class=\"CellTreeTableCollapseElement\" src=\"" + imageResources.collapsed().getURL() + "\"/>");
            }
        }
        outerDivBuilder.appendHtmlConstant("</div>");
    }

    public void renderHeaders() {
        isRefreshing = true;
        int numRows = thead.getRows().getLength();
        if (numRows > 0) {
            for (int j = 0; j < numRows; j++) {
                thead.deleteRow(0);
            }
        }
        int i = 0;
        for (List<? extends Header<?>> flexHeaderRow : flexHeaderRows) {
            TableRowElement element = thead.insertRow(-1);
            renderHeaderRow(i, element, flexHeaderRow, true);
            i++;
        }
        isRefreshing = false;
    }

    protected void renderHeaderRow(int headerRow, TableRowElement element, List<? extends Header<?>> headers, boolean forceInsert) {
        int i = 0;
        for (Header<?> headerColumn : headers) {
			renderHeader(headerRow, i, element, headerColumn, forceInsert);
            i++;
        }
    }

	@SuppressWarnings({"unchecked"})
	private void renderHeader(int headerRow, int i, TableRowElement element,  Header<?> headerColumn, boolean forceInsert) {
		TableCellElement cell = ensureCell(element,i,forceInsert);
		if (headerColumn instanceof FlexHeader) {
			FlexHeader flexHeader = (FlexHeader) headerColumn;
			cell.setColSpan(flexHeader.getColSpan());
		} else {
			cell.setColSpan(1);
		}
		Cell.Context context = new Cell.Context(headerRow, i, null);
		Object value = headerColumn.getValue();
		Cell rendercell = headerColumn.getCell();
		rendercell.setValue(context, cell, value);
	}

	private void renderAllColumns() {
        // render from right to left in order to avoid any browser layout issues
        isRefreshing = true;
        for (int i = 0; i < columns.size(); i++) {
            Column<T, ?> column = columns.get(i);
            renderColumnAtIndex(i, column, true, false);
        }
        isRefreshing = false;
    }

    /**
     * Add an item at the end of the root level of items.
     *
     * @param value object to add to the tree
     */
    public void addItem(T value) {
        addItem(value, -1);
    }


    /**
     * Add an item in the tree under the specified parent.
     *
     * @param parent parent of the value to add
     * @param value  value to add to the tree
     */
    public void addItem(T parent, T value) {
        addItem(parent, value, -1);
    }


    /**
     * Add an item in the tree under the specified parent.
     *
     * @param parent parent of the value to add
     * @param value  value to add to the tree
     * @param offset offset at which to add the parent. can be negative for end of list
     */
    public void addItem(T parent, T value, int offset) {
        TreeContext<T> parentContext = CellTreeTableModelManager.findInTree(rootNode, parent);
        CellTreeTableModelManager.logicalInsert(parentContext.getData(), value, offset);

        scheduleRendering();
    }

    private void scheduleRendering() {
        if (deferredRenderer == null) {
            deferredRenderer = new RenderingCommand();
            Scheduler.get().scheduleFinally(deferredRenderer);
        }
    }

    public void addItem(T value, int offset) {
        CellTreeTableModelManager.logicalInsert(rootNode, value, offset);
        scheduleRendering();
    }

    /**
     * Copy pasted from CellTable
     *
     * @param elem element where the event fired
     * @return cell element belonging to table
     */
    private TableCellElement findNearestParentCell(Element elem) {
        while ((elem != null) && (elem != table)) {
            String tagName = elem.getTagName();
            if ("td".equalsIgnoreCase(tagName) || "th".equalsIgnoreCase(tagName)) {
                return elem.cast();
            }
            elem = elem.getParentElement();
        }
        return null;
    }

    /**
     * Mostly copy-pasted from CellTable
     *
     * @param event the event that should be handled
     * @see com.google.gwt.user.cellview.client.CellTable#onBrowserEvent2(com.google.gwt.user.client.Event)
     */
    protected void onBrowserEvent2(Event event) {

        String eventType = event.getType();
        EventTarget eventTarget = event.getEventTarget();
        if (!Element.is(eventTarget)) {
            return;
        }
        final Element target = event.getEventTarget().cast();

        // Find the cell where the event occurred.
        TableCellElement tableCell = findNearestParentCell(target);
        if (tableCell == null) {
            return;
        }

        // Determine if we are in the header, footer, or body. Its possible that
        // the table has been refreshed before the current event fired (ex. change
        // event refreshes before mouseup fires), so we need to check each parent
        // element.
        Element trElem = tableCell.getParentElement();
        if (trElem == null) {
            return;
        }
        TableRowElement tr = TableRowElement.as(trElem);
        Element sectionElem = tr.getParentElement();
        if (sectionElem == null) {
            return;
        }
        TableSectionElement section = TableSectionElement.as(sectionElem);

        // Forward the event to the associated header, footer, or column.
        int col = tableCell.getCellIndex();
        if (section == thead) {
            Header<?> header = getHeader(tr.getRowIndex(), col);
            if (header != null) {
                // Fire the event to the header.
                if (cellConsumesEventType(header.getCell(), eventType)) {
                    Cell.Context context = new Cell.Context(0, col, header.getKey());
                    header.onBrowserEvent(context, tableCell, event);
                }

                // TODO: deal with sorting here
            }
        } else if (section == tbody) {
            DataNode<T> node = traverseToIndex(tr.getSectionRowIndex());
            int columnIndex = tableCell.getCellIndex();
            Column<T, ?> column = columns.get(columnIndex);
			if (columnIndex == 0 && isCollapserElement(target) && eventType.equals("click")) {
				node.setExpanded(!node.isExpanded());
				renderColumnAtIndex(0, columns.get(0), true, false);
				handleRowBelowNode(section, node, tr.getSectionRowIndex());
			} else if (cellConsumesEventType(column.getCell(), eventType)) {
				Cell.Context context = new Cell.Context(tr.getSectionRowIndex(), columnIndex, "k" + columnIndex);
				DivElement wrapper = getWrapper(tableCell);
				fireEventToCell(event, wrapper, node.getValue(), context, columns.get(columnIndex));

			}
        }
    }

    private DivElement getWrapper(TableCellElement cell) {
        return cell.getFirstChildElement().getFirstChildElement().cast();
    }

    private boolean isCollapserElement(Element target) {
        return target.getTagName().toLowerCase().equals("img") &&
                target.getClassName().equals("CellTreeTableCollapseElement");
    }

    private void handleRowBelowNode(final TableSectionElement section, DataNode<T> node, int sectionRowIndex) {
        DataNode<T> child = node.firstChild;
        sectionRowIndex++;
        final Style.Display display = node.expanded ? null : Style.Display.NONE;

        Visitor<T> visitor = new Visitor<T>() {
            public boolean visit(int row, DataNode<T> visitNode, int indentLevel) {
                if (display == Style.Display.NONE ) {
                    TableRowElement rowItem = section.getRows().getItem(row);
                    rowItem.getStyle().setDisplay(display);
                    if (visitNode.firstChild != null) {
                        visitNode.setExpanded(false);
                    }
                } else if (indentLevel == 0) {
                    TableRowElement rowItem = section.getRows().getItem(row);
                    rowItem.getStyle().clearDisplay();
                }
                return true;
            }
        };
        CellTreeTableModelManager.walk(child, visitor, true, 0, sectionRowIndex);
    }

    private DataNode<T> traverseToIndex(final int rowIndex) {
        /// TODO: this is performing at N, should find a shortcut.
        /// should run in constant time. otherwise every time a click happens at the bottom
        // of the table it will be slow.
        if (rootNode.getFirstChild() == null) {
            return null;
        }
        Visitor<T> visitor = new Visitor<T>() {
            public boolean visit(int row, DataNode<T> visitNode, int indentLevel) {
                return row != rowIndex;
            }
        };

		return CellTreeTableModelManager.walk(rootNode.getFirstChild(), visitor, true, 0, 0).getData();
    }

    public List<Column<T, ?>> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    private Header<?> getHeader(int rowIndex, int col) {
        if (rowIndex >= flexHeaderRows.size()) {
            throw new IllegalStateException("row index is invalid (out of bounds)");
        }
        List<? extends Header<?>> headerRow = flexHeaderRows.get(rowIndex);
        int headerColumn = 0;
        for (Header<?> header : headerRow) {
            if (col == headerColumn) {
                return header;
            }
            if (header instanceof FlexHeader) {
                FlexHeader flexHeader = (FlexHeader) header;
                if (col < (headerColumn + flexHeader.getColSpan())) {
                    return flexHeader;
                }
                headerColumn += flexHeader.getColSpan() > 0 ? flexHeader.getColSpan() : 1;
            } else {
                headerColumn += 1;
            }
        }
        throw new IllegalArgumentException("column index is invalid (out of bounds)");
    }

    private void fireEventToCell(Event event, Element tableCell, T value, Cell.Context context, Column<T, ?> tColumn) {
        tColumn.onBrowserEvent(context, tableCell, value, event);
    }

    private boolean cellConsumesEventType(Cell<?> cell, String eventType) {
        return cell.getConsumedEvents() != null && cell.getConsumedEvents().contains(eventType);
    }

    /**
     * mostly copy pasted from CellTable
     *
     * @see com.google.gwt.user.cellview.client.CellTable#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public final void onBrowserEvent(Event event) {
        if (isRefreshing) {
            return;
        }
        // Verify that the target is still a child of this widget. IE fires focus
        // events even after the element has been removed from the DOM.
        EventTarget eventTarget = event.getEventTarget();
        if (!Element.is(eventTarget)
                || !getElement().isOrHasChild(Element.as(eventTarget))) {
            return;
        }
        super.onBrowserEvent(event);
        onBrowserEvent2(event);
    }
    /*
    public static <T> int getInsertIndex(DataNode<T> rootNode, DataNode<T> node) {
        LinkedList<DataNode<T>> queue = new LinkedList<DataNode<T>>();
        queue.add(rootNode);
        int i = 0;
        while (! queue.isEmpty()) {
            DataNode<T> current = queue.poll();
            if (node.getParent() != null &&
                current.getParent() != null &&
                current.getParent().equals(node.getParent())) {
                return i + getLastSiblingIndex(current, node);   // last child under current parent
            }
            if (node.getFirstChild() != null) {
                queue.add(node.getFirstChild());
            }
            if (node.getNextSibling() != null) {
                queue.add(node.getNextSibling());
            }
            i++;
        }
        return i;
    }

    private static <T> int getLastSiblingIndex(DataNode<T> current, DataNode<T> node) {
        int i = 0;
        while(current != null) {
            current = current.getNextSibling();
            i++;
        }
        return i;
    }
    */

    interface Resources extends ClientBundle {
        @Source("collapsed.png")
        ImageResource collapsed();

        @Source("expanded.png")
        ImageResource expanded();
    }

    Resources imageResources = GWT.create(Resources.class);

    private final DataNode<T> rootNode;
    private final TableSectionElement tbody;
    private final TableSectionElement thead;
    private boolean isRefreshing = false;

    Scheduler.ScheduledCommand deferredRenderer = null;

    public Widget asWidget() {
        return this;
    }

}
