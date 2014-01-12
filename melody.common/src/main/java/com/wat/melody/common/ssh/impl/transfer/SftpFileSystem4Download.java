package com.wat.melody.common.ssh.impl.transfer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.wat.melody.common.ex.WrapperInterruptedIOException;
import com.wat.melody.common.files.LocalFileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperDirectoryNotEmptyException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.impl.Messages;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.exception.TemplatingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SftpFileSystem4Download extends LocalFileSystem implements
		TransferableFileSystem {

	private ChannelSftp _channel;
	private TemplatingHandler _templatingHandler;

	public SftpFileSystem4Download(ChannelSftp channel, TemplatingHandler th) {
		_channel = channel;
		setTemplatingHandler(th);
	}

	protected TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	protected TemplatingHandler setTemplatingHandler(TemplatingHandler th) {
		if (th == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TemplatingHandler.class.getCanonicalName() + ".");
		}
		TemplatingHandler previous = getTemplatingHandler();
		_templatingHandler = th;
		return previous;
	}

	protected ChannelSftp getChannel() {
		return _channel;
	}

	@Override
	public void release() {
		_channel.disconnect();
		super.release();
	}

	@Override
	public void transferRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		download(src, dest);
		setAttributes(dest, attrs);
	}

	@Override
	public void transformRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws TemplatingException, IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		// download the file
		download(src, dest);
		// expand the into itself
		getTemplatingHandler().doTemplate(dest, dest);
		setAttributes(dest, attrs);
	}

	private void download(Path source, Path destination) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		download(SftpFileSystem.convertToUnixPath(source),
				destination.toString());
	}

	private void download(String source, String destination)
			throws IOException, InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		// must validate source
		if (source == null || source.trim().length() == 0) {
			throw new IllegalArgumentException(source + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		/*
		 * /!\ do not release this FS ! cause it shares with this object the
		 * same underlying connection to the remote system.
		 */
		SftpFileSystem remotefs = new SftpFileSystem(getChannel());
		// fail if source doesn't exists
		SftpFileAttributes sourceAttrs = remotefs.readAttributes(source);
		// fail if source is a directory
		if (sourceAttrs.isDirectory()) {
			throw new WrapperDirectoryNotEmptyException(source);
		}
		// Fail if destination is a directory
		if (isDirectory(destination)) {
			throw new WrapperDirectoryNotEmptyException(destination);
		}
		try {
			/*
			 * if interrupted: may throw a 'java.io.IOException: Pipe closed', a
			 * 'java.net.SocketException: Broken pipe', or a
			 * 'java.io.InterruptedIOException', wrapped in an SftpException
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
				throw new InterruptedIOException(
						Messages.SfptEx_GET_INTERRUPTED);
			} else if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(source);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(source, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE
					&& Ex.getCause() instanceof InterruptedIOException) {
				throw new WrapperInterruptedIOException(
						Messages.SfptEx_GET_INTERRUPTED, Ex.getCause());
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE
					&& Ex.getCause() instanceof FileNotFoundException) {
				String msg = Ex.getCause().getMessage();
				if (msg != null
						&& msg.indexOf(" (No such file or directory)") != -1) {
					throw new WrapperNoSuchFileException(destination,
							Ex.getCause());
				} else if (msg != null
						&& msg.indexOf(" (Permission denied)") != -1) {
					throw new WrapperAccessDeniedException(destination,
							Ex.getCause());
				} else if (msg != null
						&& msg.indexOf(" (Is a directory)") != -1) {
					throw new WrapperDirectoryNotEmptyException(destination,
							Ex.getCause());
				} else {
					throw new WrapperNoSuchFileException(destination,
							Ex.getCause());
				}
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE
					&& Ex.getCause() != null) {
				throw new IOException(Msg.bind(Messages.SfptEx_GET, source,
						destination), Ex.getCause());
			} else {
				throw new IOException(Msg.bind(Messages.SfptEx_GET, source,
						destination), Ex);
			}
		}
	}

}