package com.wat.melody.plugin.libvirt.common;

import com.wat.cloud.libvirt.LibVirtCloudServicesEndpoint;
import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtPlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtPlugInConfiguration implements IPlugInConfiguration {

	public static LibVirtPlugInConfiguration get(IProcessorManager pm)
			throws PlugInConfigurationException {
		return (LibVirtPlugInConfiguration) pm
				.getPluginConfiguration(LibVirtPlugInConfiguration.class);
	}

	private String msConfigurationFilePath;

	public LibVirtPlugInConfiguration() {
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
	public void load(PropertiesSet ps)
			throws LibVirtPlugInConfigurationException {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid PropertiesSet.");
		}
		setFilePath(ps.getFilePath());

		// load and validate each configuration directives

		validate();
	}

	private void validate() throws LibVirtPlugInConfigurationException {
		// validate all configuration directives
		// almost nothing to

		// Start the LibVirtCloudServicesEndpoint
		LibVirtCloudServicesEndpoint.start();
	}

}