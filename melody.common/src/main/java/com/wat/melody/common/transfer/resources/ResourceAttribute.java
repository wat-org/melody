package com.wat.melody.common.transfer.resources;

import java.nio.file.attribute.FileAttribute;

import com.wat.melody.common.properties.Property;
import com.wat.melody.common.properties.exception.IllegalPropertyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourceAttribute extends Property implements
		FileAttribute<String> {
	/*
	 * TODO : should create dedicated PosixPermission, PosixGroup, ...
	 */

	public ResourceAttribute() {
	}

	public ResourceAttribute(String name, String value)
			throws IllegalPropertyException {
		super(name, value);
	}

	@Override
	public String toString() {
		return name() + ":" + value();
	}

	@Override
	public String name() {
		return super.getName().getValue();
	}

	@Override
	public String value() {
		return super.getValue();
	}

}