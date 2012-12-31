package com.wat.melody.core.internal;

import java.util.HashMap;

import com.wat.melody.api.IPlugInConfigurations;
import com.wat.melody.api.IPlugInConfiguration;

public class PlugInConfigurations extends
		HashMap<Class<? extends IPlugInConfiguration>, IPlugInConfiguration>
		implements IPlugInConfigurations {

	private static final long serialVersionUID = -6796889705508292212L;

	public PlugInConfigurations() {
		super();
	}

	@Override
	public IPlugInConfiguration get(Class<? extends IPlugInConfiguration> key) {
		return super.get(key);
	}

	@Override
	public boolean containsKey(Class<? extends IPlugInConfiguration> key) {
		return super.containsKey(key);
	}

	@Override
	public IPlugInConfiguration put(Class<? extends IPlugInConfiguration> key,
			IPlugInConfiguration value) {
		return super.put(key, value);
	}
}
