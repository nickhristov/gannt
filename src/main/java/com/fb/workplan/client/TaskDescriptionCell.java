package com.fb.workplan.client;

import com.fb.workplan.client.TaskWidgetData;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class TaskDescriptionCell extends AbstractCell<TaskWidgetData> {

	interface MyTemplate extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div class=\"tdesc-box\"><div class=\"ContentEditableCellBlock\" contenteditable=\"\">{0}</div><div class=\"tdesc-controls\">{1}{2}</div></div>")
		SafeHtml contents(String contents, SafeHtml img, SafeHtml img2);
	}


	@Override
	public void render(Context context, TaskWidgetData value, SafeHtmlBuilder sb) {
		SafeHtml img1 = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(images.writedocument()).getHTML());
		SafeHtml img2 = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(images.discussion()).getHTML());
		sb.append(template.contents(value.getDescription(), img1, img2));
	}

	
	interface Images extends ClientBundle {
		@ClientBundle.Source("writedocument.png")
		public ImageResource writedocument();

		@ClientBundle.Source("discussion.png")
		public ImageResource discussion();
	}

	Images images = GWT.create(Images.class);
	MyTemplate template = GWT.create(MyTemplate.class);
}
