package com.wat.melody.common.ssh.impl.transfer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferNoThread;
import com.wat.melody.common.transfer.Transferable;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class DownloaderNoThread extends TransferNoThread {

	/*
	 * TODO : The download logic should be implemented in a transfer method,
	 * provided by a DownloaderSftpFileSystem. This will also allow to pass
	 * FileAttribute<?>[] in the transfer method.
	 */

	public DownloaderNoThread(SftpFileSystem srcFS, FileSystem destFS,
			Transferable t, TemplatingHandler th) {
		super(srcFS, destFS, t, th);
	}

	@Override
	protected SftpFileSystem getSourceFileSystem() {
		return (SftpFileSystem) super.getSourceFileSystem();
	}

	@Override
	public void transferFile(Path source, Path dest, FileAttribute<?>... attrs)
			throws IOException {
		getSourceFileSystem().download(source, dest);
		getDestinationFileSystem().setAttributes(dest, attrs);
	}

}