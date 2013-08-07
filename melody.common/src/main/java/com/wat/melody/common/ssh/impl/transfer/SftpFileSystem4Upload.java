package com.wat.melody.common.ssh.impl.transfer;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
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
public class SftpFileSystem4Upload extends SftpFileSystem implements
		TransferableFileSystem {

	private TemplatingHandler _templatingHandler;

	public SftpFileSystem4Upload(ChannelSftp channel, TemplatingHandler th) {
		super(channel);
		_templatingHandler = th;
	}

	@Override
	public TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	@Override
	public void transferRegularFile(Path source, Path dest,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
			AccessDeniedException, IllegalFileAttributeException {
		upload(source, dest);
		setAttributes(dest, attrs);
	}

	private void upload(Path source, Path destination) throws IOException,
			NoSuchFileException, AccessDeniedException {
		upload(source.toString(), convertToUnixPath(destination));
	}

	private void upload(String source, String destination) throws IOException,
			NoSuchFileException, AccessDeniedException {
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
			getChannel().put(source, destination);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(destination);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(destination);
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE
					&& Ex.getCause() != null) {
				throw new IOException(Msg.bind(Messages.SfptEx_PUT, source,
						destination), Ex.getCause());
			} else {
				throw new IOException(Msg.bind(Messages.SfptEx_PUT, source,
						destination), Ex);
			}
		}
	}

}