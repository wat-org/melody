package com.wat.melody.common.ssh.impl.transfer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ex.WrapperInterruptedIOException;
import com.wat.melody.common.files.LocalFileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.impl.Messages;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SftpFileSystem4Download extends LocalFileSystem implements
		TransferableFileSystem {

	private static Logger log = LoggerFactory
			.getLogger(SftpFileSystem4Download.class);

	private ChannelSftp _channel;
	private TemplatingHandler _templatingHandler;

	public SftpFileSystem4Download(ChannelSftp channel, TemplatingHandler th) {
		_channel = channel;
		_templatingHandler = th;
	}

	protected ChannelSftp getChannel() {
		return _channel;
	}

	@Override
	public TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	@Override
	public void release() {
		_channel.disconnect();
		super.release();
	}

	@Override
	public void transferRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws IOException,
			InterruptedIOException, NoSuchFileException, AccessDeniedException,
			IllegalFileAttributeException {
		download(src, dest);
		setAttributes(dest, attrs);
	}

	public void download(Path source, Path destination) throws IOException,
			InterruptedIOException, NoSuchFileException, AccessDeniedException {
		download(SftpFileSystem.convertToUnixPath(source),
				destination.toString());
	}

	public void download(String source, String destination) throws IOException,
			InterruptedIOException, NoSuchFileException, AccessDeniedException {
		if (source == null || source.trim().length() == 0) {
			throw new IllegalArgumentException(source + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (destination == null || destination.trim().length() == 0) {
			throw new IllegalArgumentException(destination + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			/*
			 * if interrupted: may throw a 'java.io.IOException: Pipe closed', a
			 * 'java.net.SocketException: Broken pipe', or a
			 * 'java.io.InterruptedIOException', wrapped in an SftpException.
			 */
			getChannel().get(
					source,
					destination,
					new ProgressMonitor(getChannel().getSession().getHost(),
							null), ChannelSftp.OVERWRITE);
		} catch (SftpException Ex) {
			if (Thread.interrupted()) {
				/*
				 * if 'java.io.IOException: Pipe closed' or
				 * 'java.net.SocketException: Broken pipe'
				 */
				throw new InterruptedIOException("transfer interrupted");
			} else if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(source);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(source);
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE
					&& Ex.getCause() instanceof InterruptedIOException) {
				throw new WrapperInterruptedIOException("transfer interrupted",
						(InterruptedIOException) Ex.getCause());
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE
					&& Ex.getCause() != null) {
				log.warn(MelodyException.getStackTrace(Ex));
				throw new IOException(Msg.bind(Messages.SfptEx_GET, source,
						destination), Ex.getCause());
			} else {
				log.warn(MelodyException.getStackTrace(Ex));
				throw new IOException(Msg.bind(Messages.SfptEx_GET, source,
						destination), Ex);
			}
		} catch (JSchException e) {
			throw new RuntimeException("Shouldn't happened.");
		}
	}
}