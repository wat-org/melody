package com.wat.melody.core.nativeplugin.property;

import com.wat.melody.api.IShareProperties;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Property extends com.wat.melody.common.properties.Property
		implements ITask, IShareProperties {

	private ITaskContext moContext;

	public Property() {
		initContext();
	}

	private void initContext() {
		moContext = null;
	}

	@Override
	public void validate() {
	}

	@Override
	public void doProcessing() {
		getContext().getProperties().put(this);
	}

	@Override
	public ITaskContext getContext() {
		return moContext;
	}

	@Override
	public void setContext(ITaskContext context) {
		if (context == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = context;
	}

}
