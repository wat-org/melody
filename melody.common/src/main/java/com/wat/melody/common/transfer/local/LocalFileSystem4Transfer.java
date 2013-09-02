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

import com.wat.melody.common.files.LocalFileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperDirectoryNotEmptyException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;

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
		_templatingHandler = th;
	}

	@Override
	public TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	@Override
	public void transferRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws IOException,
			InterruptedIOException, NoSuchFileException, AccessDeniedException,
			IllegalFileAttributeException {
		copy(src, dest);
		setAttributes(dest, attrs);
	}

	private void copy(Path source, Path destination) throws IOException,
			InterruptedIOException, NoSuchFileException, AccessDeniedException {
		try {
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (DirectoryNotEmptyException Ex) {
			throw new WrapperDirectoryNotEmptyException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

}