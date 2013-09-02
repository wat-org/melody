package com.wat.melody.plugin.ssh;

import java.io.File;

import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;
import com.wat.melody.plugin.ssh.common.Transfer;
import com.wat.melody.plugin.ssh.common.types.LocalResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Upload extends Transfer {

	/**
	 * Task's name
	 */
	public static final String UPLOAD = "upload";

	public Upload() {
		super();
	}

	public void doTransfer(ISshSession session) throws SshSessionException,
			InterruptedException {
		session.upload(getResourcesSpecifications(), getMaxPar(), this);
	}

	public ResourcesSpecification newResourcesSpecification(File basedir) {
		return new LocalResourcesSpecification(basedir);
	}

}