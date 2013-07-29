package com.wat.melody.common.transfer.resources.attributes;

import java.nio.file.attribute.GroupPrincipal;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PosixGroup extends ResourceAttribute<GroupPrincipal> {

	public static final String VIEW_NAME = "posix:group";

	private GroupPrincipal _group = null;

	public PosixGroup() {
	}

	@Override
	public String name() {
		return VIEW_NAME;
	}

	@Override
	public String setStringValue(String value) {
		String previous = super.setStringValue(value);
		/*
		 * TODO : validate input string
		 */
		// convert input String to groupPrincipal
		_group = new GroupPrincipal() {

			@Override
			public String getName() {
				return getStringValue();
			}

		};
		return previous;
	}

	@Override
	public GroupPrincipal value() {
		return _group;
	}

}