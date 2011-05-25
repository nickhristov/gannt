package com.fb.workplan.client;

import com.google.gwt.event.shared.GwtEvent;

public class PropertyDidChangeEvent<O> extends GwtEvent<PropertyDidChangeEventHandler<O>> {

	public PropertyDidChangeEvent(O owner, String propertyName, Object oldValue, Object newValue) {
		this.owner = owner;
		this.propertyName = propertyName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	private static final Type<PropertyDidChangeEventHandler<?>> TYPE = new Type<PropertyDidChangeEventHandler<?>>();

	@SuppressWarnings({"unchecked"})
	@Override
	public Type getAssociatedType() {
		return TYPE;
	}

	public static Type getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(PropertyDidChangeEventHandler<O> handler) {
		handler.onPropertyChange(owner, propertyName, oldValue, newValue);
	}

	private final O owner;
	private final String propertyName;
	private final Object oldValue;
	private final Object newValue;
}
