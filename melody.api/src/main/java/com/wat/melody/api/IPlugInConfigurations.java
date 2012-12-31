package com.wat.melody.api;

public interface IPlugInConfigurations {

	public IPlugInConfiguration get(Class<? extends IPlugInConfiguration> key);

	public boolean containsKey(Class<? extends IPlugInConfiguration> key);

	public IPlugInConfiguration put(Class<? extends IPlugInConfiguration> key,
			IPlugInConfiguration value);

}
