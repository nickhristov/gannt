package com.fb.workplan.client.release;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fb.workplan.client.DateUtils;
import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

class GanntFloaterCell implements Cell<TaskWidgetData> {

    public GanntFloaterCell (Date periodStartDate, Date periodEndDate, ChartType type) {
        if (template == null) {
            // lazily create the shared template
            template = GWT.create(CellTemplates.class);
        }
        this.type = type;
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        consumedEvents = new HashSet<String>();
        consumedEvents.add("click");
    }

    private Set<String> consumedEvents;
    public boolean dependsOnSelection() {
        return false;
    }

    public Set<String> getConsumedEvents() {
        return consumedEvents;
    }

    public boolean handlesSelection() {
        return false;
    }

    public boolean isEditing(Context context, Element parent, TaskWidgetData value) {
        return false;
    }

    public void onBrowserEvent(Context context,
                               Element parent,
                               TaskWidgetData value,
                               NativeEvent event,
                               ValueUpdater<TaskWidgetData> dateValueUpdater) {
        // TODO: handle click, and drag and drop
    }

    public void render(Context context, TaskWidgetData value, SafeHtmlBuilder sb) {
        if (value != null) {
            Date startDate = getStartDateOrDefault(value);
            int duration = getDurationOrDefault(value);

            if ( ( periodStartDate.before(startDate) ||
                   periodStartDate.equals(startDate))
                 &&
                   periodEndDate.after(startDate)
                ) {
                if (value.getChildren() != null && ! value.getChildren().isEmpty()) {
                    renderParentFloater(startDate, duration, sb);
                } else {
                    renderFloater(startDate, duration, value.getProgress(), sb);
                }
            }
        }
    }

    private int getDurationOrDefault (TaskWidgetData value) {
        int duration = value.getDuration();
        if (type.equals(ChartType.WEEKLY) && duration < 7) {
            duration = 7;
        }
        if (type.equals(ChartType.DAILY) && duration < 1) {
            duration = 1;
        }
        if (type.equals(ChartType.MONTHLY) && duration < 30) {
            duration = 10;
        }
        return duration;
    }
    
    private Date getStartDateOrDefault(TaskWidgetData value) {
        Date startDate = null;
        if (value.getStartDate() == null) {
            Date now = new Date();
            if (periodStartDate.before(now) && periodEndDate.after(now)) {
                startDate = now;
            }
        } else {
            startDate = value.getStartDate();
        }
        return startDate;
    }

    private int getPixelDuration(int duration) {
        if (type.equals(ChartType.DAILY)) {
            return duration * CELL_WIDTH_PX;
        }
        if (type.equals(ChartType.WEEKLY)) {
            return (int) (Math.ceil((duration * CELL_WIDTH_PX) / 7));
        }
        if (type.equals(ChartType.MONTHLY)) {
            return (int) (Math.ceil(duration  * CELL_WIDTH_PX) / 30);
        }
        throw new IllegalArgumentException("Unknown chart type");
    }

    private void renderFloater(Date startDate, int dayDuration, int percentComplete,  SafeHtmlBuilder sb) {
        SafeHtml rendered = template.floater(getPixelDuration(dayDuration), getPixelOffset(startDate), getPixelCompletion(dayDuration, percentComplete));
        sb.append(rendered);
    }

    private int getPixelCompletion(int dayDuration, int completionPercent) {
        int offset = getPixelDuration( (int) ( dayDuration * (completionPercent)/100.0D ) );
        return offset;
    }
    private int getPixelOffset(Date startDate) {
        int dayOffset = DateUtils.getDaysDiff(periodStartDate, startDate);
        return getPixelDuration(dayOffset);
    }

    private void renderParentFloater(Date startDate, int dayDuration, SafeHtmlBuilder sb) {
        SafeHtml rendered = template.parentFloater(getPixelDuration(dayDuration), getPixelOffset(startDate));
        sb.append(rendered);
    }

    public boolean resetFocus(Context context, Element parent, TaskWidgetData value) {
        return false;
    }

    public void setValue(Context context, Element parent, TaskWidgetData value) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        render(context, value, builder);
        String html = builder.toSafeHtml().asString();
        parent.setInnerHTML(html);
        if (html.indexOf("canvas") >= 0) {
            completeParentFloaterRendering(parent, value);
        }
        parent.addClassName("GanntCell");
    }

    private void completeParentFloaterRendering(Element parent, TaskWidgetData value) {
        CanvasElement canvas = parent.getFirstChildElement().cast();

        int width =  getPixelDuration(getDurationOrDefault(value));
        canvas.setHeight(CELL_HEIGHT);
        canvas.setWidth(width);
        Context2d context = canvas.getContext2d();
        drawParent(context, CELL_HEIGHT, width, 3, STROKE_COLOR, FILL_COLOR);
    }


    private void drawParent(Context2d context, int height, int length, int padding, String strokeColor, String fillColor) {
        context.setFillStyle(fillColor);
        context.setLineWidth(2);
        context.setStrokeStyle(strokeColor);

        context.beginPath();
        context.moveTo(padding, padding);
        context.lineTo(padding, height - padding - PADDING_BOTTOM);
        context.lineTo(padding + TRIANGLE_SIDE_LENGTH, height - padding - TRIANGLE_SIDE_LENGTH);
        context.lineTo(length - padding - TRIANGLE_SIDE_LENGTH, height - padding - TRIANGLE_SIDE_LENGTH);
        context.lineTo(length - padding, height - padding - PADDING_BOTTOM);
        context.lineTo(length - padding, padding);
        context.lineTo(padding,padding);
        context.stroke();
        context.closePath();
        context.fill();
    }


    public interface CellTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div class=\"floater\" style=\"left: {1}px; width: {0}px\"><div class=\"floater-filler\" style=\"left:{2}px;\"></div></div>")
        SafeHtml floater(int duration, int position, int completionOffset);

        @SafeHtmlTemplates.Template("<canvas class=\"parent-floater\" style=\"height: " + CELL_HEIGHT + "px;left: {1}px; width: {0}px\"/>")
        SafeHtml parentFloater(int duration, int position);
    }

    private static CellTemplates template = null;

    private final Date periodStartDate;
    private final Date periodEndDate;
    private final ChartType type;

    private static final int CELL_WIDTH_PX = 24;
    private static final int PADDING_BOTTOM = 3;
    private static final int TRIANGLE_SIDE_LENGTH = 7;
    private static final String FILL_COLOR = "#FF9B2D";
    private static final String STROKE_COLOR = "#FFBD4D";
    private static final int CELL_HEIGHT = 18;
}
