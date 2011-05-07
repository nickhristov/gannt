package com.fb.workplan.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.ui.Widget;

public class GanntFloaterParentWidget implements GanntFloater {

    Canvas canvas;
    int height = 15;
    int triangleSideLength = 10;
    private String _fillCollor = "#FF9B2D";
    private String _innerFillCollor = "#FFBD4D";

    int _padding = 1;
    int paddingBottom = 3;

    public GanntFloaterParentWidget() {
        canvas = Canvas.createIfSupported();
        canvas.setWidth("20px");
        canvas.setHeight("20px");
        resize(20);
    }

    public void setWidth(int pixels) {
        resize(pixels);
        canvas.setWidth(pixels + 5 +"px");
    }

    private void resize(int length) {
        canvas.setCoordinateSpaceWidth(length + 5);
        canvas.setCoordinateSpaceHeight(height);

        Context2d context = canvas.getContext2d();
        context.clearRect(0, 0, length, height);

        draw(context, length, _padding, _fillCollor, _innerFillCollor);
    }

    private void draw(Context2d context, int length, int padding, String strokeColor, String fillColor) {
        context.setFillStyle(fillColor);
        context.setLineWidth(2);
        context.setStrokeStyle(strokeColor);

        context.beginPath();
        context.moveTo(padding, padding);
        context.lineTo(padding, height- padding - paddingBottom);
        context.lineTo(padding + triangleSideLength, height - padding - triangleSideLength);
        context.lineTo(length - padding - triangleSideLength, height - padding - triangleSideLength);
        context.lineTo(length - padding, height - padding - paddingBottom);
        context.lineTo(length - padding, padding);
        context.lineTo(padding,padding);
        context.stroke();
        context.closePath();
        context.fill();
    }

    public Widget asWidget() {
        return canvas;
    }
}
