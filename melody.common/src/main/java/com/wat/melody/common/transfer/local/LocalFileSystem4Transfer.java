package com.wat.melody.common.transfer.local;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;

import com.wat.melody.common.ex.WrapperInterruptedIOException;
import com.wat.melody.common.files.LocalFileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperDirectoryNotEmptyException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.Messages;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.exception.TemplatingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocalFileSystem4Transfer extends LocalFileSystem implements
		TransferableFileSystem {

	private TemplatingHandler _templatingHandler;

	public LocalFileSystem4Transfer(TemplatingHandler th) {
		super();
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

	@Override
	public void transferRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		copy(src, dest);
		setAttributes(dest, attrs);
	}

	@Override
	public void transformRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws TemplatingException, IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		// expand src into a tmpfile and copy the tmpfile into dest
		copy(getTemplatingHandler().doTemplate(src, null), dest);
		setAttributes(dest, attrs);
	}

	private void copy(Path source, Path destination) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		if (isDirectory(source)) {
			throw new WrapperDirectoryNotEmptyException(source.toString());
		}
		if (isDirectory(destination)) {
			throw new WrapperDirectoryNotEmptyException(destination.toString());
		}
		try {
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (DirectoryNotEmptyException Ex) {
			throw new WrapperDirectoryNotEmptyException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		} catch (InterruptedIOException Ex) {
			throw new WrapperInterruptedIOException(
					Messages.LocalFSEx_COPY_INTERRUPTED, Ex);
		} catch (IOException Ex) {
			throw new IOException(Msg.bind(Messages.LocalFSEx_COPY, source,
					destination), Ex);
		}
	}

}