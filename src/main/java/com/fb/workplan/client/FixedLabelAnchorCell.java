package com.fb.workplan.client;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class FixedLabelAnchorCell extends AnchorCell {

	private final String label;

	public FixedLabelAnchorCell(String label) {
		this.label = label;
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		super.render(context,label, sb);
	}

}
