package com.wat.melody.common.network;

import java.util.LinkedHashSet;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Addresses extends LinkedHashSet<Address> {

	private static final long serialVersionUID = -2798809865423798432L;

	public Addresses(Address... addresses) {
		super();
		setAddresses(addresses);
	}

	private void setAddresses(Address... addresses) {
		clear();
		if (addresses == null) {
			return;
		}
		for (Address address : addresses) {
			if (address == null) {
				continue;
			} else {
				add(address);
			}
		}
	}

}