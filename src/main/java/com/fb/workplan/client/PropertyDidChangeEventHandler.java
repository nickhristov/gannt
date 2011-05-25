package com.fb.workplan.client;

import com.google.gwt.event.shared.EventHandler;

public interface PropertyDidChangeEventHandler<O> extends EventHandler {
	public void onPropertyChange(O owner, String propertyName, Object oldValue, Object newValue);
}
