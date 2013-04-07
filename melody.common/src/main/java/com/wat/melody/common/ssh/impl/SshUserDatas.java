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

	private String msLogin;
	private String msPassword;
	private KeyPairRepositoryPath moKeyPairRepository;
	private KeyPairName moKeyPairName;

	@Override
	public String toString() {
		return "{ user:"
				+ getLogin()
				+ (getPassword() != null ? ", password:" + getPassword() : "")
				+ (getKeyPairPath() != null ? ", keypair:" + getKeyPairPath()
						: "") + " }";
	}

	private String getKeyPairPath() {
		if (getKeyPairName() == null || getKeyPairRepositoryPath() == null) {
			return null;
		}
		return KeyPairRepository.getKeyPairRepository(getKeyPairRepositoryPath())
				.getPrivateKeyFile(getKeyPairName()).getPath();
	}

	@Override
	public String getLogin() {
		return msLogin;
	}

	@Override
	public String setLogin(String sLogin) {
		if (sLogin == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		if (sLogin.trim().length() == 0) {
			throw new IllegalArgumentException(": Not accepted. "
					+ "Cannot be empty String.");
		}
		String previous = getLogin();
		msLogin = sLogin;
		return previous;
	}

	@Override
	public String getPassword() {
		return msPassword;
	}

	@Override
	public String setPassword(String sPassword) {
		String previous = getPassword();
		msPassword = sPassword;
		return previous;
	}

	@Override
	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return moKeyPairRepository;
	}

	@Override
	public KeyPairRepositoryPath setKeyPairRepositoryPath(
			KeyPairRepositoryPath keyPairRepository) {
		KeyPairRepositoryPath previous = getKeyPairRepositoryPath();
		moKeyPairRepository = keyPairRepository;
		return previous;
	}

	@Override
	public KeyPairName getKeyPairName() {
		return moKeyPairName;
	}

	@Override
	public KeyPairName setKeyPairName(KeyPairName keyPairName) {
		KeyPairName previous = getKeyPairName();
		moKeyPairName = keyPairName;
		return previous;
	}
}
