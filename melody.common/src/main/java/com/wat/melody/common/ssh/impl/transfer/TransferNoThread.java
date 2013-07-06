package com.wat.melody.common.ssh.impl.transfer;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.exception.TemplatingException;
import com.wat.melody.common.ssh.filesfinder.EnhancedFileAttributes;
import com.wat.melody.common.ssh.filesfinder.Resource;
import com.wat.melody.common.ssh.types.TransferBehavior;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class TransferNoThread {

	private static Logger log = LoggerFactory.getLogger(TransferNoThread.class);

	private ChannelSftp _channel;
	private Resource _resource;
	private TemplatingHandler _templatingHandler;

	public TransferNoThread(ChannelSftp channel, Resource r,
			TemplatingHandler th) {
		setChannel(channel);
		setResource(r);
		setTemplatingHandler(th);
	}

	public void transfer() throws TransferException {
		Resource r = getResource();
		log.debug(Msg.bind(Messages.UploadMsg_BEGIN, r));
		try {
			// ensure parent directory exists
			createParentDirectory();

			// deal with resource, regarding its type
			if (r.isSymbolicLink()) {
				ln();
			} else if (r.isDirectory()) {
				createDirectory();
				chmodDir();
				chgrp();
			} else if (r.isRegularFile()) {
				template();
				chmodFile();
				chgrp();
			} else {
				log.warn(Msg.bind(Messages.UploadMsg_NOTFOUND, r));
				return;
			}
		} catch (IOException | SshSessionException Ex) {
			throw new TransferException(Ex);
		}
		log.info(Msg.bind(Messages.UploadMsg_END, r));
	}

	protected void ln() throws IOException, SshSessionException {
		switch (getResource().getLinkOption()) {
		case KEEP_LINKS:
			createSymlink();
			break;
		case COPY_LINKS:
			ln_copy();
			break;
		case COPY_UNSAFE_LINKS:
			ln_copy_unsafe();
			break;
		}
	}

	protected void ln_copy_unsafe() throws IOException, SshSessionException {
		if (getResource().isSafeLink()) {
			createSymlink();
		} else {
			ln_copy();
		}
	}

	protected void ln_copy() throws IOException, SshSessionException {
		Resource r = getResource();
		if (!r.exists()) {
			deleteDestination();
			log.warn(Messages
					.bind(Messages.UploadMsg_COPY_UNSAFE_IMPOSSIBLE, r));
		} else if (r.isRegularFile()) {
			template();
			chmodFile();
			chgrp();
		} else {
			createDirectory();
			chmodDir();
			chgrp();
		}
	}

	public abstract void deleteDestination() throws IOException,
			SshSessionException;

	public abstract void createParentDirectory() throws IOException,
			SshSessionException;

	public abstract void createDirectory() throws IOException,
			SshSessionException;

	public abstract void transferFile(Path source,
			EnhancedFileAttributes localFileAttrs, Path dest,
			TransferBehavior tb) throws IOException, SshSessionException;

	public abstract void createSymlink() throws IOException,
			SshSessionException;

	public abstract void chmodFile() throws IOException, SshSessionException;

	public abstract void chmodDir() throws IOException, SshSessionException;

	public abstract void chgrp() throws IOException, SshSessionException;

	protected void template() throws IOException, SshSessionException {
		Resource r = getResource();
		if (r.getTemplate() == true) {
			if (getTemplatingHandler() == null) {
				throw new SshSessionException(
						Messages.UploadEx_NO_TEMPLATING_HANDLER);
			}
			Path template;
			try {
				template = getTemplatingHandler().doTemplate(r.getPath());
			} catch (TemplatingException Ex) {
				throw new SshSessionException(Ex);
			}
			transferFile(template, r.getAttributes(), r.getDestination(),
					r.getTransferBehavior());
		} else {
			transferFile(r.getPath(), r.getAttributes(), r.getDestination(),
					r.getTransferBehavior());
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

	protected Resource getResource() {
		return _resource;
	}

	private Resource setResource(Resource lr) {
		if (lr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Resource.class.getCanonicalName()
					+ ".");
		}
		Resource previous = getResource();
		_resource = lr;
		return previous;
	}

	protected TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	private TemplatingHandler setTemplatingHandler(TemplatingHandler th) {
		TemplatingHandler previous = getTemplatingHandler();
		_templatingHandler = th;
		return previous;
	}

}