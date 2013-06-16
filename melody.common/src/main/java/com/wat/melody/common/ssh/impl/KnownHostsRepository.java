package com.wat.melody.common.ssh.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KnownHosts;
import com.jcraft.jsch.UserInfo;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.ssh.IHostKey;
import com.wat.melody.common.ssh.IKnownHostsRepository;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.exception.KnownHostsException;
import com.wat.melody.common.ssh.types.HostKeyCheckState;

/**
 * <p>
 * A {@link KnownHostsRepository} stores remote systems host key.
 * </p>
 * 
 * <p>
 * A {@link KnownHostsRepository} is a file where each line contains the ip/fqdn
 * of the remote system and its corresponding host key.
 * </p>
 * 
 * <p>
 * A {@link KnownHostsRepository} is thread safe.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class KnownHostsRepository implements IKnownHostsRepository {

	private static Map<KnownHostsRepositoryPath, KnownHostsRepository> REGISTERED_REPOS = new HashMap<KnownHostsRepositoryPath, KnownHostsRepository>();

	public synchronized static KnownHostsRepository getKnownHostsRepository(
			KnownHostsRepositoryPath knownHostsRepositoryPath)
			throws KnownHostsException {
		if (knownHostsRepositoryPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KnownHostsRepositoryPath.class.getCanonicalName() + ".");
		}
		if (REGISTERED_REPOS.containsKey(knownHostsRepositoryPath)) {
			return REGISTERED_REPOS.get(knownHostsRepositoryPath);
		}
		KnownHostsRepository kpr = new KnownHostsRepository(
				knownHostsRepositoryPath);
		REGISTERED_REPOS.put(knownHostsRepositoryPath, kpr);
		return kpr;
	}

	private HostKeyRepository _kh;

	private KnownHostsRepository(KnownHostsRepositoryPath khrp)
			throws KnownHostsException {
		try {
			KnownHosts kh = new KnownHosts();
			kh.setKnownHosts(khrp.getPath());
			_kh = kh;
		} catch (JSchException Ex) {
			throw new KnownHostsException(Msg.bind(
					Messages.KnownHostsEx_INVALID_CONTENT, khrp), Ex);
		}
	}

	@Override
	public String toString() {
		return _kh.getKnownHostsRepositoryID();
	}

	@Override
	public synchronized List<IHostKey> getAll() {
		HostKey[] hks = _kh.getHostKey();
		List<IHostKey> res = new ArrayList<IHostKey>();
		for (HostKey hk : hks) {
			res.add(new HostKeyAdapter(hk));
		}
		return res;
	}

	@Override
	public synchronized IHostKey get(Host host) {
		if (host == null) {
			return null;
		}
		HostKey[] hks = _kh.getHostKey(host.getAddress(), null);
		if (hks != null && hks.length > 0) {
			return new HostKeyAdapter(hks[0]);
		}
		if (!isHostNameDefined(host)) {
			return null;
		}
		hks = _kh.getHostKey(host.getName(), null);
		if (hks != null && hks.length > 0) {
			return new HostKeyAdapter(hks[0]);
		}
		return null;
	}

	@Override
	public synchronized HostKeyCheckState check(IHostKey hk) {
		if (hk == null || hk.getHost() == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must ve ba valid " + IHostKey.class.getCanonicalName()
					+ ".");
		}
		int state = _kh.check(hk.getHost().getAddress(), hk.getBytes());
		return HostKeyCheckStateConverter.convert(state);

	}

	@Override
	public synchronized void add(IHostKey hk) {
		if (hk == null || hk.getHost() == null) {
			return;
		}
		add(hk.getHost().getAddress(), hk.getBytes());
		if (isHostNameDefined(hk.getHost())) {
			add(hk.getHost().getName(), hk.getBytes());
		}
	}

	@Override
	public synchronized void remove(Host host) {
		if (host == null) {
			return;
		}
		_kh.remove(host.getAddress(), null);
		if (isHostNameDefined(host)) {
			_kh.remove(host.getName(), null);
		}
	}

	private boolean isHostNameDefined(Host host) {
		return !host.getName().equals(host.getAddress());
	}

	private void add(String host, byte[] key) {
		HostKey hk = null;
		try {
			hk = new HostKey(host, key);
		} catch (JSchException Ex) {
			throw new IllegalArgumentException(Ex);
		}
		_kh.add(hk, AnswerYes.getInstance());
	}

}

class AnswerYes implements UserInfo {

	private static AnswerYes _instance;

	public synchronized static AnswerYes getInstance() {
		if (_instance == null) {
			_instance = new AnswerYes();
		}
		return _instance;
	}

	private AnswerYes() {
	}

	@Override
	public void showMessage(String message) {
	}

	@Override
	public boolean promptYesNo(String message) {
		return true;
	}

	@Override
	public boolean promptPassword(String message) {
		return true;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return true;
	}

	@Override
	public String getPassword() {
		return "yes";
	}

	@Override
	public String getPassphrase() {
		return "yes";
	}

}