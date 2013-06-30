package com.wat.melody.common.ssh.impl.downloader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.filesfinder.RemoteResource;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class DownloaderNoThread {

	private static Log log = LogFactory.getLog(DownloaderNoThread.class);

	private ChannelSftp _channel;
	private RemoteResource _remoteResource;

	protected DownloaderNoThread(ChannelSftp channel, RemoteResource rr) {
		setChannel(channel);
		setRemoteResource(rr);
	}

	protected void download() throws DownloaderException {
		log.debug(Msg.bind(Messages.DownloadMsg_BEGIN, getRemoteResource()));

		/*
		 * TODO : implement download logic
		 */

		log.info(Msg.bind(Messages.DownloadMsg_END, getRemoteResource()));
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

	private RemoteResource setRemoteResource(RemoteResource rr) {
		if (rr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ RemoteResource.class.getCanonicalName() + ".");
		}
		RemoteResource previous = getRemoteResource();
		_remoteResource = rr;
		return previous;
	}

}