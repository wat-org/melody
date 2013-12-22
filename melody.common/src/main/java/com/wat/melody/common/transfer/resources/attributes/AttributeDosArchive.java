package com.wat.melody.common.transfer.resources.attributes;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AttributeDosArchive extends AttributeDosBase {

	public static final String NAME = "dos:archive";

	public AttributeDosArchive() {
		super();
	}

	@Override
	public String name() {
		return NAME;
	}

}