package com.wat.melody.common.ssh.impl.transfer;

import java.io.IOException;
import java.nio.file.Path;

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

	public DownloaderNoThread(SftpFileSystem srcFS, FileSystem destFS,
			Transferable t, TemplatingHandler th) {
		super(srcFS, destFS, t, th);
	}

	@Override
	protected SftpFileSystem getSourceFileSystem() {
		return (SftpFileSystem) super.getSourceFileSystem();
	}

	@Override
	public void transferFile(Path source, Path dest) throws IOException {
		getSourceFileSystem().download(source, dest);
	}

}