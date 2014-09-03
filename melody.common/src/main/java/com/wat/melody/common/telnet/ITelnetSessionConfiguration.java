package com.wat.melody.common.telnet;

import com.wat.melody.common.telnet.types.ConnectionRetry;
import com.wat.melody.common.telnet.types.ConnectionTimeout;
import com.wat.melody.common.telnet.types.ReadTimeout;
import com.wat.melody.common.telnet.types.ReceiveBufferSize;
import com.wat.melody.common.telnet.types.SendBufferSize;
import com.wat.melody.common.telnet.types.SoLinger;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITelnetSessionConfiguration {

	public ConnectionTimeout getConnectionTimeout();

	public ConnectionTimeout setConnectionTimeout(ConnectionTimeout val);

	public ConnectionRetry getConnectionRetry();

	public ConnectionRetry setConnectionRetry(ConnectionRetry val);

	public ReadTimeout getReadTimeout();

	public ReadTimeout setReadTimeout(ReadTimeout val);

	public SoLinger getSoLinger();

	public SoLinger setSoLinger(SoLinger soLinger);

	public boolean getTcpNoDelay();

	public boolean setTcpNoDelay(boolean on);

	public SendBufferSize getSendBufferSize();

	public SendBufferSize setSendBufferSize(SendBufferSize size);

	/**
	 * Return <tt>0</tt> if undefined.
	 */
	public ReceiveBufferSize getReceiveBufferSize();

	public ReceiveBufferSize setReceiveBufferSize(ReceiveBufferSize size);

}