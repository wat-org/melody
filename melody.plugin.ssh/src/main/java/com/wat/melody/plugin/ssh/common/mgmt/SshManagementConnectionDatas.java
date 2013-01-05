package com.wat.melody.plugin.ssh.common.mgmt;

import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface SshManagementConnectionDatas {

	public String getManagementMasterUser();

	public KeyPairName getManagementMasterKey();

	public String getManagementMasterPass();

	public KeyPairRepository getManagementKeyPairRepository();

}
