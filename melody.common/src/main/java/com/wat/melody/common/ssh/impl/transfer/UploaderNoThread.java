package com.wat.melody.common.ssh.impl.transfer;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferNoThread;
import com.wat.melody.common.transfer.Transferable;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class UploaderNoThread extends TransferNoThread {

	/*
	 * TODO : The upload logic should be implemented in a transfer method,
	 * provided by a UploaderSftpFileSystem. This will also allow to pass
	 * FileAttribute<?>[] in the transfer method.
	 */

	public UploaderNoThread(FileSystem srcFS, SftpFileSystem destFS,
			Transferable t, TemplatingHandler th) {
		super(srcFS, destFS, t, th);
	}

	@Override
	protected SftpFileSystem getDestinationFileSystem() {
		return (SftpFileSystem) super.getDestinationFileSystem();
	}

	@Override
	public void transferFile(Path source, Path dest, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException, AccessDeniedException,
			IllegalFileAttributeException {
		getDestinationFileSystem().upload(source, dest);
		getDestinationFileSystem().setAttributes(dest, attrs);
	}

}