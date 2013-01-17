package com.wat.melody.common.ssh;

import java.io.OutputStream;
import java.util.List;

import com.jcraft.jsch.HostKey;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.types.SimpleResource;

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

	public void connect() throws SshSessionException, InterruptedException;

	public void disconnect();

	public boolean isConnected();

	public int execRemoteCommand(String sCommand, OutputStream out,
			OutputStream err) throws SshSessionException, InterruptedException;

	public void upload(List<SimpleResource> r, int maxPar, TemplatingHandler th)
			throws SshSessionException, InterruptedException;

	/*
	 * TODO : remove everything which concern JSch !
	 */
	public HostKey getHostKey();

}
