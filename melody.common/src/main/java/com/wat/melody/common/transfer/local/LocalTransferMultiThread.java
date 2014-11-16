package com.wat.melody.common.transfer.local;

import java.util.List;

import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.LocalFileSystem;
import com.wat.melody.common.threads.MelodyThreadFactory;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferMultiThread;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocalTransferMultiThread extends TransferMultiThread {

	public LocalTransferMultiThread(List<ResourcesSpecification> rss,
			int maxPar, TemplatingHandler th, MelodyThreadFactory tf) {
		super(rss, maxPar, th, tf);
	}

	@Override
	public String getTransferProtocolDescription() {
		return "file copy";
	}

	@Override
	public String getThreadName() {
		return "copy";
	}

	@Override
	public String getSourceSystemDescription() {
		return "local file system";
	}

	@Override
	public String getDestinationSystemDescription() {
		return "local file system";
	}

	@Override
	public FileSystem newSourceFileSystem() {
		return new LocalFileSystem();
	}

	@Override
	public TransferableFileSystem newDestinationFileSystem() {
		return new LocalFileSystem4Transfer(getTemplatingHandler());
	}

}