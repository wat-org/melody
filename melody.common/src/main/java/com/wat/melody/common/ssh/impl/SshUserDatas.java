package com.wat.melody.common.ssh.impl;

import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.ssh.ISshUserDatas;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.exception.IllegalSshUserDatasException;

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
		return "{ user:" + getLogin() + ", password:" + getPassword()
				+ ", keypair repo:" + getKeyPairRepository() + ", keypairname:"
				+ getKeyPairName() + " }";
	}

	@Override
	public String getLogin() {
		return msLogin;
	}

	@Override
	public String setLogin(String sLogin) throws IllegalSshUserDatasException {
		if (sLogin == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		if (sLogin.trim().length() == 0) {
			throw new IllegalSshUserDatasException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
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
