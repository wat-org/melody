package com.wat.melody.common.ssh.impl;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.types.ResourceMatcher;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class DownloaderNoThread {

	private static Log log = LogFactory.getLog(DownloaderNoThread.class);

	private ChannelSftp _channel;
	private ResourceMatcher _resourceMatcher;

	protected DownloaderNoThread(ChannelSftp channel, ResourceMatcher r) {
		setChannel(channel);
		setResourceMatcher(r);
	}

	protected void download() throws DownloaderException {
		log.debug(Msg.bind(Messages.DownloadMsg_BEGIN, getResource()));
		// create the local-basedir if it doesn't exists
		File dest = getResource().getLocalBaseDir();
		synchronized (getLock()) {
			if (!dest.exists() && !dest.mkdirs()) {
				throw new DownloaderException(Msg.bind(
						Messages.DownloadEx_MKDIRS, dest));
			}
		}

		/*
		 * TODO : Recurs lists remote files from glob
		 */
		String glob = getResource().getRemoteBaseDir() + "/"
				+ getResource().getMatch();
		try {
			getChannel().ls(glob);
		} catch (SftpException Ex) {
			throw new DownloaderException(Msg.bind(Messages.DownloadEx_LIST,
					glob), Ex);
		}

		// TODO : implement download logic
		log.info(Msg.bind(Messages.DownloadMsg_END, getResource()));
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

	protected ResourceMatcher getResource() {
		return _resourceMatcher;
	}

	private ResourceMatcher setResourceMatcher(ResourceMatcher aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ResourceMatcher.class.getCanonicalName() + ".");
		}
		ResourceMatcher previous = getResource();
		_resourceMatcher = aft;
		return previous;
	}

}