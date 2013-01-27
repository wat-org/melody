package com.wat.melody.core.internal;

import java.util.HashMap;

import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.IPlugInConfigurations;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PlugInConfigurations extends
		HashMap<Class<? extends IPlugInConfiguration>, IPlugInConfiguration>
		implements IPlugInConfigurations {

	private static final long serialVersionUID = -6796889705508292212L;

	public PlugInConfigurations() {
		super();
	}

	@Override
	public IPlugInConfiguration put(IPlugInConfiguration conf) {
		if (conf == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid "
					+ IPlugInConfiguration.class.getCanonicalName() + ".");
		}
		return super.put(conf.getClass(), conf);
	}

	@Override
	public IPlugInConfiguration get(Class<? extends IPlugInConfiguration> name) {
		if (name == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + Class.class.getCanonicalName() + "<"
					+ IPlugInConfiguration.class.getCanonicalName() + ">.");
		}
		return super.get(name);
	}

	@Override
	public boolean contains(Class<? extends IPlugInConfiguration> name) {
		if (name == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + Class.class.getCanonicalName() + "<"
					+ IPlugInConfiguration.class.getCanonicalName() + ">.");
		}
		return super.containsKey(name);
	}

}