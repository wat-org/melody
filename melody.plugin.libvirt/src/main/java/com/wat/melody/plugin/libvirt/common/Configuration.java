package com.wat.melody.plugin.libvirt.common;

import java.util.Map;

import com.wat.melody.api.IPluginConfiguration;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.plugin.libvirt.common.exception.ConfigurationException;

public class Configuration implements IPluginConfiguration {

	public static final String NAME = "LIBVIRT";

	public static Configuration get(IProcessorManager pm)
			throws ConfigurationException {
		Map<String, IPluginConfiguration> pcs = pm.getPluginConfigurations();
		IPluginConfiguration pc = null;
		pc = pcs.get(NAME);
		if (pc == null) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_CONF_NOT_REGISTERED, NAME));
		}
		try {
			return (Configuration) pc;
		} catch (ClassCastException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_CONF_REGISTRATION_ERROR,
					new Object[] { NAME,
							IPluginConfiguration.PLUGIN_CONF_CLASS,
							pc.getFilePath(),
							Configuration.class.getCanonicalName() }));
		}
	}

	private String msConfigurationFilePath;

	public Configuration() {
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getFilePath() {
		return msConfigurationFilePath;
	}

	private void setFilePath(String fp) {
		if (fp == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a LIBVIRT Plug-In "
					+ "Configuration file path).");
		}
		msConfigurationFilePath = fp;
	}

	@Override
	public void load(PropertiesSet ps) throws ConfigurationException {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid PropertiesSet.");
		}
		setFilePath(ps.getFilePath());

		// load and validate each configuration directives

		validate();
	}

	private void validate() throws ConfigurationException {
		// validate all configuration directives
	}

}