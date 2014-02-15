package com.wat.melody.common.ssh.impl;

import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KnownHosts;
import com.jcraft.jsch.UserInfo;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.ssh.IHostKey;
import com.wat.melody.common.ssh.IKnownHostsRepository;
import com.wat.melody.common.ssh.types.HostKeyType;

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

	/**
	 * return all {@link HostKey} this Host Key Repository contains.
	 */
	@Override
	public HostKey[] getHostKey() {
		return getHostKey(null, null);
	}

	/**
	 * @param host
	 *            The fqdn or ip address of the host keys to search. Can be
	 *            <tt>null</tt>.
	 * @param keyType
	 *            The type of the host keys to search. Can be <tt>null</tt>.
	 * 
	 * @return all {@link HostKey} corresponding to the given host and type this
	 *         Host Key Repository contains. If the given type is <tt>null</tt>,
	 *         return all {@link HostKey} corresponding to the given host. If
	 *         the given host is <tt>null</tt>, return all {@link HostKey}
	 *         corresponding to the given type. If both host and type are
	 *         <null>, return all {@link HostKey} this Host Key Repository
	 *         contains. If nothing match or if this Host Key Repository is
	 *         empty, return <tt>null</tt> (as
	 *         {@link KnownHosts#getHostKey(String, String)} do).
	 * 
	 * @throw IllegalArgumentException if the given type is not <tt>null</tt>
	 *        and is neither equals to 'ssh-rsa' nor 'ssh-dss' or if the given
	 *        host is not <tt>null</tt> and is not a valid fqdn or ip address.
	 */
	@Override
	public HostKey[] getHostKey(String host, String keyType) {
		// convert keyType (if defined)
		HostKeyType cKeyType = null;
		if (keyType != null) {
			cKeyType = HostKetTypeConverter.convert(keyType);
		}

		// convert host (if defined)
		Host cHost = null;
		if (host != null) {
			try {
				cHost = Host.parseString(host);
			} catch (IllegalHostException Ex) {
				throw new IllegalArgumentException(Ex);
			}
		}

		// retrieve matching host keys
		List<IHostKey> ihks = new ArrayList<IHostKey>();
		ihks = _kh.get(cHost, cKeyType);

		// return null if no match
		if (ihks == null || ihks.size() == 0) {
			return null;
		}

		// convert each IHostKey to HostKey
		HostKey[] hks = new HostKey[ihks.size()];
		for (int i = 0; i < ihks.size(); i++) {
			hks[i] = HostKeyAdapter.convertToHostKey(ihks.get(i));
		}
		return hks;
	}

	@Override
	public String getKnownHostsRepositoryID() {
		return null;
	}

	/**
	 * Remove all {@link HostKey} corresponding to the given host and type this
	 * Host Key Repository contains. If the given type is <tt>null</tt>, remove
	 * all {@link HostKey} corresponding to the given host. If the given host is
	 * <tt>null</tt>, don't do anything.
	 * 
	 * @param host
	 *            The fqdn or ip address of the host keys to remove. Can be
	 *            <tt>null</tt>.
	 * @param keyType
	 *            The type of the host keys to remove. Can be <tt>null</tt>.
	 * 
	 * @throw IllegalArgumentException if the given type is not <tt>null</tt>
	 *        and is neither equals to 'ssh-rsa' nor 'ssh-dss' or if the given
	 *        host is not <tt>null</tt> and is not a valid fqdn or ip address.
	 */
	@Override
	public void remove(String host, String keyType) {
		if (host == null) {
			// if the given host is invalid, exit without failure
			return;
		}
		// convert host (if defined)
		Host cHost = null;
		try {
			cHost = Host.parseString(host);
		} catch (IllegalHostException Ex) {
			throw new IllegalArgumentException(Ex);
		}

		// convert keyType (if defined)
		HostKeyType cKeyType = null;
		if (keyType != null) {
			cKeyType = HostKetTypeConverter.convert(keyType);
		}

		// remove matching host keys
		_kh.remove(cHost, cKeyType);
	}

	/**
	 * See {@link #remove(String, String)}.
	 */
	@Override
	public void remove(String host, String type, byte[] key) {
		remove(host, type);
	}

}