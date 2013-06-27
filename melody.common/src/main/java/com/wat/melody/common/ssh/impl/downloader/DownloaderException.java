package com.wat.melody.common.ssh.impl.downloader;

import com.wat.melody.common.ssh.exception.SshSessionException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DownloaderException extends SshSessionException {

	private static final long serialVersionUID = -4876866535323525654L;

	public DownloaderException(String msg) {
		super(msg);
	}

	public DownloaderException(Throwable cause) {
		super(cause);
	}

	public DownloaderException(String msg, Throwable cause) {
		super(msg, cause);
	}

}