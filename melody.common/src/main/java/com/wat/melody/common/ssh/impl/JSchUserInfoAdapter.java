package com.wat.melody.common.ssh.impl;

import com.jcraft.jsch.UserInfo;
import com.wat.melody.common.ssh.ISshConnectionDatas;
import com.wat.melody.common.ssh.ISshUserDatas;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class JSchUserInfoAdapter implements UserInfo {

	private ISshUserDatas _user;
	private ISshConnectionDatas _cnx;

	JSchUserInfoAdapter(ISshUserDatas user, ISshConnectionDatas cnx) {
		_user = user;
		_cnx = cnx;
	}

	@Override
	public String getPassphrase() {
		return _user.getPassword();
	}

	@Override
	public String getPassword() {
		return _user.getPassword();
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
	public boolean promptYesNo(String message) {
		return _cnx.getTrust();
	}

	@Override
	public void showMessage(String message) {
		// don't don anything
	}

}
