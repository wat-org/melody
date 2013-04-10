package com.wat.melody.core.nativeplugin.property;

import com.wat.melody.api.IShareProperties;
import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Property extends com.wat.melody.common.properties.Property
		implements ITask, IShareProperties {

	public Property() {
	}

	@Override
	public void validate() {
	}

	@Override
	public void doProcessing() {
		Melody.getContext().getProperties().put(this);
	}

}
