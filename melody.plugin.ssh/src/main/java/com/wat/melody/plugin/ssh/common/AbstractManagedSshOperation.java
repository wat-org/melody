package com.wat.melody.plugin.ssh.common;

import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractManagedSshOperation extends AbstractSshOperation {

	@Override
	public void validate() throws SshException {
		super.validate();
	}

}
