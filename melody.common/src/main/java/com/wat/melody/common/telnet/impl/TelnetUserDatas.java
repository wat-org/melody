package com.wat.melody.common.telnet.impl;

import com.wat.melody.common.telnet.ITelnetUserDatas;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetUserDatas implements ITelnetUserDatas {

	private String _login;
	private String _password;

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("user:");
		str.append(getLogin());
		str.append(", password:");
		str.append(getPassword());
		str.append(" }");
		return str.toString();
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
		if (password == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getPassword();
		_password = password;
		return previous;
	}

}