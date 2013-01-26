package com.wat.melody.common.ssh.impl;

import java.io.File;

import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.ssh.ISshUserDatas;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshUserDatas implements ISshUserDatas {

	private String msLogin;
	private String msPassword;
	private KeyPairRepository moKeyPairRepository;
	private KeyPairName moKeyPairName;

	@Override
	public String toString() {
		return "{ user:"
				+ getLogin()
				+ (getPassword() != null ? ", password:" + getPassword() : "")
				+ (getKeyPairPath() != null ? ", keypair:" + getKeyPairPath()
						: "") + " }";
	}

	private File getKeyPairPath() {
		if (getKeyPairName() == null || getKeyPairRepository() == null) {
			return null;
		}
		return getKeyPairRepository().getPrivateKeyFile(getKeyPairName());
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
	public KeyPairRepository getKeyPairRepository() {
		return moKeyPairRepository;
	}

	@Override
	public KeyPairRepository setKeyPairRepository(
			KeyPairRepository keyPairRepository) {
		KeyPairRepository previous = getKeyPairRepository();
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
