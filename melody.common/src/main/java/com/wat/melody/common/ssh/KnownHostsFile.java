package com.wat.melody.common.ssh;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KnownHosts;
import com.jcraft.jsch.UserInfo;
import com.wat.melody.common.ssh.exception.KnownHostsFileException;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.common.utils.exception.IllegalFileException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KnownHostsFile implements HostKeyRepository {

	private HostKeyRepository _kh;

	public KnownHostsFile(String path) throws KnownHostsFileException {
		try {
			Tools.validateFileExists(path);
		} catch (IllegalFileException Ex) {
			throw new KnownHostsFileException(Messages.bind(
					Messages.KnownHostsEx_INVALID_PATH, path), Ex);
		}
		try {
			KnownHosts kh = new KnownHosts();
			kh.setKnownHosts(path);
			_kh = kh;
		} catch (JSchException Ex) {
			throw new KnownHostsFileException(Messages.bind(
					Messages.KnownHostsEx_INVALID_CONTENT, path), Ex);
		}
	}

	public int check(String host, byte[] key) {
		return _kh.check(host, key);
	}

	public void add(HostKey hostkey, UserInfo ui) {
		_kh.add(hostkey, ui);
	}

	public void remove(String host, String type) {
		_kh.remove(host, type);
	}

	public void remove(String host, String type, byte[] key) {
		_kh.remove(host, type, key);
	}

	public String getKnownHostsRepositoryID() {
		return _kh.getKnownHostsRepositoryID();
	}

	public HostKey[] getHostKey() {
		return _kh.getHostKey();
	}

	public HostKey[] getHostKey(String host, String type) {
		return _kh.getHostKey(host, type);
	}

	public void add(String host, byte[] key) {
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