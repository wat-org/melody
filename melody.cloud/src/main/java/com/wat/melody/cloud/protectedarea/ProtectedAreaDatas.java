package com.wat.melody.cloud.protectedarea;

import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaDatasException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProtectedAreaDatas {

	private String _region;
	private String _name;
	private String _description;

	public ProtectedAreaDatas(ProtectedAreaDatasValidator validator,
			String region, String name, String description)
			throws IllegalProtectedAreaDatasException {
		setRegion(region);
		setName(name);
		setDescription(description);
		validator.validateAndTransform(this);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("region:");
		str.append(getRegion());
		str.append(", name:");
		str.append(getName());
		str.append(", description:");
		str.append(getDescription());
		str.append(" }");
		return str.toString();
	}

	public String getRegion() {
		return _region;
	}

	public void setRegion(String region) {
		_region = region;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		_description = description;
	}

}