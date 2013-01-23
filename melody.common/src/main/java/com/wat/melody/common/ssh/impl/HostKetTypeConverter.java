package com.wat.melody.common.ssh.impl;

import com.wat.melody.common.ssh.types.HostKeyType;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
abstract class HostKetTypeConverter {

	protected static HostKeyType convert(String jschHostKeyType) {
		switch (jschHostKeyType) {
		case "ssh-dss":
			return HostKeyType.DSS;
		case "ssh-rsa":
			return HostKeyType.RSA;
		default:
			throw new IllegalArgumentException(jschHostKeyType
					+ ": Not accepted. "
					+ "Must be one of { ssh-rsa, ssh-dss }.");
		}
	}

	protected static String convert(HostKeyType type) {
		switch (type) {
		case DSS:
			return "ssh-dss";
		case RSA:
			return "ssh-rsa";
		default:
			throw new RuntimeException("BUG ! '" + type
					+ "' is not supported. "
					+ "This method should handle this.");
		}
	}
}
