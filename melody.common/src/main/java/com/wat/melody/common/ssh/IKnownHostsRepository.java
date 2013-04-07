package com.wat.melody.common.ssh;

import java.util.List;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.ssh.types.HostKeyCheckState;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface IKnownHostsRepository {

	public List<IHostKey> getAll();

	public HostKeyCheckState check(IHostKey hostkey);

	public IHostKey get(Host host);

	public void add(IHostKey hostkey);

	public void remove(Host host);

}
