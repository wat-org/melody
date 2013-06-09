package com.wat.melody.common.ssh.impl;

import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.ssh.ISshUserDatas;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshUserDatas implements ISshUserDatas {

	private String _login;
	private String _password;
	private KeyPairRepositoryPath _keyPairRepositoryPath;
	private KeyPairName _keyPairName;

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("user:");
		str.append(getLogin());
		if (getPassword() != null) {
			str.append(", password:");
			str.append(getPassword());
		}
		if (getKeyPairPath() != null) {
			str.append(", keypair:");
			str.append(getKeyPairPath());
		}
		str.append(" }");
		return str.toString();
	}

	private String getKeyPairPath() {
		if (getKeyPairName() == null || getKeyPairRepositoryPath() == null) {
			return null;
		}
		return KeyPairRepository
				.getKeyPairRepository(getKeyPairRepositoryPath())
				.getPrivateKeyFile(getKeyPairName()).getPath();
	}

	@Override
	public String getLogin() {
		return _login;
	}

	@Override
	public String setLogin(String login) {
		if (login == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		if (login.trim().length() == 0) {
			throw new IllegalArgumentException(": Not accepted. "
					+ "Cannot be empty String.");
		}
		String previous = getLogin();
		_login = login;
		return previous;
	}

	@Override
	public String getPassword() {
		return _password;
	}

	@Override
	public String setPassword(String password) {
		String previous = getPassword();
		_password = password;
		return previous;
	}

	@Override
	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return _keyPairRepositoryPath;
	}

	@Override
	public KeyPairRepositoryPath setKeyPairRepositoryPath(
			KeyPairRepositoryPath keyPairRepository) {
		KeyPairRepositoryPath previous = getKeyPairRepositoryPath();
		_keyPairRepositoryPath = keyPairRepository;
		return previous;
	}

	@Override
	public KeyPairName getKeyPairName() {
		return _keyPairName;
	}

	@Override
	public KeyPairName setKeyPairName(KeyPairName keyPairName) {
		KeyPairName previous = getKeyPairName();
		_keyPairName = keyPairName;
		return previous;
	}
}