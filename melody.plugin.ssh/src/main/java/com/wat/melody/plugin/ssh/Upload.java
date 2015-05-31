package com.wat.melody.plugin.ssh;

import java.io.File;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
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
@Conditions({
		@Condition({ @Match(expression = "§[@provider]§", value = "sftp") }),
		@Condition({ @Match(expression = "§[machine.os.name]§", value = "rhel") }) })
public class Upload extends Transfer {

	/**
	 * Task's name
	 */
	public static final String UPLOAD = "upload";

	public Upload() {
		super();
	}

	@Override
	public void doTransfer(ISshSession session) throws SshSessionException,
			InterruptedException {
		session.upload(getResourcesSpecifications(), getMaxPar(), this,
				Melody.getThreadFactory());
	}

	@Override
	public ResourcesSpecification newResourcesSpecification(File basedir) {
		return new LocalResourcesSpecification(basedir);
	}

}