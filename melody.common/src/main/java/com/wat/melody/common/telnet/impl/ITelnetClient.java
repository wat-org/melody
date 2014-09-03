package com.wat.melody.common.telnet.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import com.wat.melody.common.telnet.impl.exception.UnexpectedResultReceived;
import com.wat.melody.common.timeout.Timeout;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITelnetClient {

	public void setConnectTimeout(int connectTimeout);

	public void setSoTimeout(int timeout) throws SocketException;

	public void setSoLinger(boolean on, int val) throws SocketException;

	public void setTcpNoDelay(boolean on) throws SocketException;

	public void setSendBufferSize(int size) throws SocketException;

	public void setReceiveBufferSize(int size) throws SocketException;

	public void setKillTimeout(Timeout<Long> killTimeout);

	public void connect() throws IOException, InterruptedException;

	public boolean isConnected();

	public void disconnect() throws IOException;

	public char read() throws IOException, InterruptedException;

	public void waitUntil(OutputStream out, String startToTruncate,
			TelnetResponsesMatcher expected, TelnetResponsesMatcher unexpected)
			throws UnexpectedResultReceived, IOException, InterruptedException;

	public void send(String line) throws IOException;

	public boolean wasInterrupted();

}