package com.wat.melody.common.ssh.impl;

import com.jcraft.jsch.HostKey;
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