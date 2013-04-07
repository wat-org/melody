package com.wat.melody.common.ssh.impl;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.ssh.IKnownHostsRepository;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class KnownHostsAdapter implements HostKeyRepository {

	private IKnownHostsRepository _kh;

	protected KnownHostsAdapter(IKnownHostsRepository kh) {
		if (kh == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KnownHostsRepository.class.getCanonicalName() + ".");
		}
		_kh = kh;
	}

	@Override
	public void add(HostKey hk, UserInfo ui) {
		_kh.add(new HostKeyAdapter(hk));
	}

	@Override
	public int check(String host, byte[] key) {
		try {
			return HostKeyCheckStateConverter.convert(_kh
					.check(new HostKeyAdapter(new HostKey(host, key))));
		} catch (JSchException Ex) {
			throw new IllegalArgumentException(Ex);
		}
	}

	@Override
	public HostKey[] getHostKey() {
		throw new RuntimeException("not implemented.");
	}

	@Override
	public HostKey[] getHostKey(String arg0, String arg1) {
		throw new RuntimeException("not implemented.");
	}

	@Override
	public String getKnownHostsRepositoryID() {
		return null;
	}

	@Override
	public void remove(String host, String type) {
		try {
			_kh.remove(Host.parseString(host));
		} catch (IllegalHostException Ex) {
			return;
		}
	}

	@Override
	public void remove(String host, String type, byte[] key) {
		try {
			_kh.remove(Host.parseString(host));
		} catch (IllegalHostException Ex) {
			return;
		}
	}

}
