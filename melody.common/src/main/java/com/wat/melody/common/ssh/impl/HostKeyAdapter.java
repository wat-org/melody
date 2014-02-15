package com.wat.melody.common.ssh.impl;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSchException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.ssh.IHostKey;
import com.wat.melody.common.ssh.types.HostKeyType;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class HostKeyAdapter implements IHostKey {

	public static HostKey convertToHostKey(IHostKey ihk) {
		try {
			return new HostKey(ihk.getHost().getAddress(), ihk.getBytes());
		} catch (JSchException Ex) {
			throw new RuntimeException("Unexpected error while converting "
					+ "an IHostKey to an HostKey. "
					+ "Since the given IHostKey is valid "
					+ ", such error cannot happened. "
					+ "Source code has certainly been modified "
					+ "and a bug have been introduced.", Ex);
		}
	}

	private HostKey _hk;

	protected HostKeyAdapter(HostKey hk) {
		if (hk == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + HostKey.class.getCanonicalName()
					+ ".");
		}
		_hk = hk;
	}

	public byte[] getBytes() {
		return _hk.getKeyBytes();
	}

	public HostKeyType getType() {
		return HostKetTypeConverter.convert(_hk.getType());
	}

	public Host getHost() {
		try {
			return Host.parseString(_hk.getHost());
		} catch (IllegalHostException Ex) {
			return null;
		}
	}

}