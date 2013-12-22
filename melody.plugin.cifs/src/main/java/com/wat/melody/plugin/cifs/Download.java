package com.wat.melody.plugin.cifs;

import java.io.File;

import com.wat.melody.common.cifs.transfer.CifsDownloaderMultiThread;
import com.wat.melody.common.transfer.exception.TransferException;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;
import com.wat.melody.plugin.cifs.common.Transfer;
import com.wat.melody.plugin.cifs.common.types.RemoteResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Download extends Transfer {

	/**
	 * Task's name
	 */
	public static final String DOWNLOAD = "download";

	public Download() {
		super();
	}

	public void doTransfer(String location, String domain, String username,
			String password) throws TransferException, InterruptedException {
		new CifsDownloaderMultiThread(location, domain, username, password,
				getResourcesSpecifications(), getMaxPar(), this).doTransfer();
	}

	public ResourcesSpecification newResourcesSpecification(File basedir) {
		return new RemoteResourcesSpecification(basedir);
	}

}