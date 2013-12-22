package com.wat.melody.common.transfer.resources.attributes;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AttributeDosReadOnly extends AttributeDosBase {

	public static final String NAME = "dos:readonly";

	public AttributeDosReadOnly() {
		super();
	}

	@Override
	public String name() {
		return NAME;
	}

}