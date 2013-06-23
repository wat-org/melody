package com.wat.melody.common.ssh.impl;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.types.RemoteResource;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class DownloaderNoThread {

	private static Log log = LogFactory.getLog(DownloaderNoThread.class);

	private ChannelSftp _channel;
	private RemoteResource _remoteResource;

	protected DownloaderNoThread(ChannelSftp channel, RemoteResource r) {
		setChannel(channel);
		setRemoteResource(r);
	}

	protected void download() throws DownloaderException {
		log.debug(Msg.bind(Messages.DownloadMsg_BEGIN, getRemoteResource()));

		/*
		 * TODO : implement download logic
		 * 
		 * parents dir creation should be done in the get method, before getting
		 * the file
		 */
		// create the local-basedir if it doesn't exists
		File dest = getRemoteResource().getLocalBaseDir();
		synchronized (getLock()) {
			if (!dest.exists() && !dest.mkdirs()) {
				throw new DownloaderException(Msg.bind(
						Messages.DownloadEx_MKDIRS, dest));
			}
		}

		log.info(Msg.bind(Messages.DownloadMsg_END, getRemoteResource()));
	}

	private static Object BASIC_LOCK = new Integer(0);

	/**
	 * @return a session scope lock.
	 */
	private Object getLock() {
		try {
			return getChannel().getSession();
		} catch (JSchException Ex) {
			return BASIC_LOCK;
		}
	}

	protected ChannelSftp getChannel() {
		return _channel;
	}

	private ChannelSftp setChannel(ChannelSftp channel) {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ChannelSftp.class.getCanonicalName()
					+ ".");
		}
		ChannelSftp previous = getChannel();
		_channel = channel;
		return previous;
	}

	protected RemoteResource getRemoteResource() {
		return _remoteResource;
	}

	private RemoteResource setRemoteResource(RemoteResource aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ RemoteResource.class.getCanonicalName() + ".");
		}
		RemoteResource previous = getRemoteResource();
		_remoteResource = aft;
		return previous;
	}

}