package com.wat.melody.common.ssh.impl;

import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KnownHosts;
import com.jcraft.jsch.UserInfo;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.ssh.IHostKey;
import com.wat.melody.common.ssh.IKnownHosts;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.exception.KnownHostsException;
import com.wat.melody.common.ssh.types.HostKeyCheckState;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KnownHostsFile implements IKnownHosts {

	private HostKeyRepository _kh;

	public KnownHostsFile(String path) throws KnownHostsException {
		try {
			FS.validateFileExists(path);
		} catch (IllegalFileException Ex) {
			throw new KnownHostsException(Messages.bind(
					Messages.KnownHostsEx_INVALID_PATH, path), Ex);
		}
		try {
			KnownHosts kh = new KnownHosts();
			kh.setKnownHosts(path);
			_kh = kh;
		} catch (JSchException Ex) {
			throw new KnownHostsException(Messages.bind(
					Messages.KnownHostsEx_INVALID_CONTENT, path), Ex);
		}
	}

	@Override
	public List<IHostKey> getAll() {
		HostKey[] hks = _kh.getHostKey();
		List<IHostKey> res = new ArrayList<IHostKey>();
		for (HostKey hk : hks) {
			res.add(new HostKeyAdapter(hk));
		}
		return res;
	}

	@Override
	public IHostKey get(Host host) {
		HostKey hk = null;
		HostKey[] hks = _kh.getHostKey(host.getAddress(), null);
		if (hks != null && hks.length > 0) {
			hk = hks[0];
		} else if (isHostNameDefined(host)) {
			hks = _kh.getHostKey(host.getName(), null);
			if (hks != null && hks.length > 0) {
				hk = hks[0];
			}
		}
		return hk != null ? new HostKeyAdapter(hk) : null;
	}

	@Override
	public HostKeyCheckState check(IHostKey hk) {
		int state = _kh.check(hk.getHost().getAddress(), hk.getBytes());
		return HostKeyCheckStateConverter.convert(state);

	}

	@Override
	public void add(IHostKey hk) {
		add(hk.getHost().getAddress(), hk.getBytes());
		if (isHostNameDefined(hk.getHost())) {
			add(hk.getHost().getName(), hk.getBytes());
		}
	}

	@Override
	public void remove(Host host) {
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