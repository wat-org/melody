package com.wat.melody.common.telnet;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITelnetUserDatas {

	public String getLogin();

	/**
	 * Cannot be null or empty.
	 */
	public String setLogin(String sLogin);

	public String getPassword();

	/**
	 * Cannot be null or empty. If the connection to remote system should be
	 * done with an empty password, this should be set to an empty
	 * <tt>String</tt>.
	 */
	public String setPassword(String sPassword);

}