package com.wat.melody.common.telnet;

import java.io.OutputStream;

import com.wat.melody.common.telnet.exception.InvalidCredentialException;
import com.wat.melody.common.telnet.exception.TelnetSessionException;
import com.wat.melody.common.timeout.Timeout;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITetnetSession {

	public ITelnetSessionConfiguration getSessionConfiguration();

	public ITelnetSessionConfiguration setSessionConfiguration(
			ITelnetSessionConfiguration sc);

	public ITelnetUserDatas getUserDatas();

	public ITelnetUserDatas setUserDatas(ITelnetUserDatas ud);

	public ITelnetConnectionDatas getConnectionDatas();

	public ITelnetConnectionDatas setConnectionDatas(ITelnetConnectionDatas cd);

	public void connect() throws TelnetSessionException,
			InvalidCredentialException, InterruptedException;

	public void disconnect();

	public boolean isConnected();

	public int execRemoteCommand(String command, OutputStream out,
			Timeout<Long> killTimeout) throws TelnetSessionException,
			InterruptedException;

}