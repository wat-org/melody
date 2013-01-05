package com.wat.melody.plugin.ssh.common.jsch;

import com.jcraft.jsch.UserInfo;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface SshConnectionDatas extends UserInfo {

	public String getLogin();

	public Host getHost();

	public Port getPort();

	public KeyPairName getKeyPairName();

	public KeyPairRepository getKeyPairRepository();

}
