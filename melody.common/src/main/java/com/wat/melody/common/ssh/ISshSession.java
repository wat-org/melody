package com.wat.melody.common.ssh;

import java.io.OutputStream;
import java.util.List;

import com.wat.melody.common.ssh.exception.InvalidCredentialException;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.types.Resources;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ISshSession {

	public ISshSessionConfiguration getSessionConfiguration();

	public ISshSessionConfiguration setSessionConfiguration(
			ISshSessionConfiguration sc);

	public ISshUserDatas getUserDatas();

	public ISshUserDatas setUserDatas(ISshUserDatas ud);

	public ISshConnectionDatas getConnectionDatas();

	public ISshConnectionDatas setConnectionDatas(ISshConnectionDatas cd);

	public void connect() throws SshSessionException,
			InvalidCredentialException, InterruptedException;

	public void disconnect();

	public boolean isConnected();

	public int execRemoteCommand(String sCommand, boolean requiretty,
			OutputStream out, OutputStream err) throws SshSessionException,
			InterruptedException;

	public void upload(List<Resources> r, int maxPar, TemplatingHandler th)
			throws SshSessionException, InterruptedException;

	public void download(List<Resources> r, int maxPar)
			throws SshSessionException, InterruptedException;

	public IHostKey getHostKey();

}