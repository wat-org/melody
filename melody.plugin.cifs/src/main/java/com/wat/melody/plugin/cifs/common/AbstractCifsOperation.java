package com.wat.melody.plugin.cifs.common;

import com.wat.melody.api.ITask;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.network.Host;
import com.wat.melody.plugin.cifs.common.exception.CifsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractCifsOperation implements ITask {

	/**
	 * Defines the remote system ip or fqdn.
	 */
	public static final String HOST_ATTR = "host";

	/**
	 * Defines the domain-user to connect with on the remote system. Can be
	 * "domain\\username", "username@domain", or "username".
	 */
	public static final String LOGIN_ATTR = "login";

	/**
	 * Defines the password of the user used to connect to the remote system.
	 */
	public static final String PASS_ATTR = "password";

	private Host _host = null;
	private String _login = null;
	private String _password = null;

	public AbstractCifsOperation() {
	}

	@Override
	public void validate() throws CifsException {
	}

	public String getLocation() {
		return getHost().getAddress();
	}

	public String getDomain() {
		int sep = getLogin().indexOf('\\');
		if (sep != -1) {
			return getLogin().substring(0, sep);
		}
		sep = getLogin().indexOf('@');
		if (sep != -1) {
			return getLogin().substring(sep + 1);
		}
		return null;
	}

	public String getUserName() {
		int sep = getLogin().indexOf('\\');
		if (sep != -1) {
			return getLogin().substring(sep + 1);
		}
		sep = getLogin().indexOf('@');
		if (sep != -1) {
			return getLogin().substring(0, sep);
		}
		return getLogin();
	}

	public Host getHost() {
		return _host;
	}

	@Attribute(name = HOST_ATTR, mandatory = true)
	public Host setHost(Host host) {
		if (host == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Host.class.getCanonicalName() + ".");
		}
		Host previous = getHost();
		_host = host;
		return previous;
	}

	public String getLogin() {
		return _login;
	}

	@Attribute(name = LOGIN_ATTR, mandatory = true)
	public String setLogin(String login) throws CifsException {
		if (login == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a login).");
		}
		if (login.indexOf('\\') == login.length() - 1) {
			throw new CifsException("'" + login + "': Not accepted. "
					+ "Must respect the following format 'domain\\username',"
					+ " 'username@domain', or 'username'.");
		}
		if (login.indexOf('@') == 0) {
			throw new CifsException("'" + login + "': Not accepted. "
					+ "Must respect the following format 'domain\\username',"
					+ " 'username@domain', or 'username'.");
		}
		String previous = getLogin();
		_login = login;
		return previous;
	}

	public String getPassword() {
		return _password;
	}

	@Attribute(name = PASS_ATTR, mandatory = true)
	public String setPassword(String password) {
		if (password == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a password).");
		}
		String previous = getPassword();
		_password = password;
		return previous;
	}

}