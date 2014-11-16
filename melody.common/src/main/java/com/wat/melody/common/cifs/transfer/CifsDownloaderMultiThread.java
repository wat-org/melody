package com.wat.melody.common.cifs.transfer;

import java.util.List;

import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.threads.MelodyThreadFactory;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class CifsDownloaderMultiThread extends CifsBaseTransferMultiThread {

	public CifsDownloaderMultiThread(String location, String domain,
			String username, String password, List<ResourcesSpecification> rss,
			int maxPar, TemplatingHandler th, MelodyThreadFactory tf) {
		super(location, domain, username, password, rss, maxPar, th, tf);
	}

	@Override
	public String getThreadName() {
		return "downloader";
	}

	@Override
	public String getSourceSystemDescription() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("host:");
		str.append(getLocation());
		str.append(" }");
		return str.toString();
	}

	@Override
	public String getDestinationSystemDescription() {
		return "local file system";
	}

	@Override
	public FileSystem newSourceFileSystem() {
		return new CifsFileSystem(getLocation(), getDomain(), getUserName(),
				getPassword());
	}

	@Override
	public TransferableFileSystem newDestinationFileSystem()
			throws InterruptedException {
		return new CifsFileSystem4Download(getLocation(), getDomain(),
				getUserName(), getPassword(), getTemplatingHandler());
	}

}