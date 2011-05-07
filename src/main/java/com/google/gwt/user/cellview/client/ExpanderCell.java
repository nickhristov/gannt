package com.google.gwt.user.cellview.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

class ExpanderCell<T> implements Cell<DataNode<T>> {


    ExpanderCell (CellTreeTable<T> treeTable) {
        this.treeTable = treeTable;
    }

    public boolean dependsOnSelection() {
        return false;
    }

    public Set<String> getConsumedEvents() {
        Set<String> events = new HashSet<String>();
        events.add("click");
        return Collections.unmodifiableSet(events);
    }

    public boolean handlesSelection() {
        return false;
    }

    public boolean isEditing(Context context, Element parent, DataNode<T> value) {
        return false;
    }

    public void onBrowserEvent(Context context, Element parent, DataNode<T> value,
                               NativeEvent event, ValueUpdater<DataNode<T>> treeTableRowValueUpdater) {
        if (value.getFirstChild() != null) {
            if (event.getType().equals("click")) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                render(context, value, builder);
                parent.setInnerHTML(builder.toSafeHtml().asString());
            }
        }
    }

    public void render(Context context, DataNode<T> value, SafeHtmlBuilder sb) {
        if (value.getFirstChild() != null) {
            if (value.isExpanded()) {
                sb.appendHtmlConstant("<img src=\""+imageResources.expanded().getURL()+"\"/>");
            } else {
                sb.appendHtmlConstant("<img src=\""+imageResources.collapsed().getURL()+"\"/>");
            }
        }
    }

    public boolean resetFocus(Context context, Element parent, DataNode<T> value) {
        return false;
    }

    public void setValue(Context context, Element parent, DataNode<T> value) {
        // TODO: consider whether we need this
    }

    public interface CellTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<img src=\"{0}\"/>")
        SafeHtml img(String url);
    }

    interface Resources extends ClientBundle {
        @Source("collapsed.png")
        ImageResource collapsed();

        @ClientBundle.Source("expanded.png")
        ImageResource expanded();
    }

    CellTemplates cellTemplates = GWT.create(CellTemplates.class);

    Resources imageResources = GWT.create(Resources.class);


    private CellTreeTable<T> treeTable;
}
