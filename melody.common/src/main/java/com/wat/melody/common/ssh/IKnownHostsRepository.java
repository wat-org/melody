package com.wat.melody.common.ssh;

import java.util.List;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.ssh.types.HostKeyCheckState;
import com.wat.melody.common.ssh.types.HostKeyType;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface IKnownHostsRepository {

	public List<IHostKey> getAll();

	public HostKeyCheckState check(IHostKey hostkey);

	/**
	 * @param host
	 *            The fqdn or ip address of the host keys to search. Can be
	 *            <tt>null</tt>.
	 * @param keyType
	 *            The type of the host keys to search. Can be <tt>null</tt>.
	 * 
	 * @return a {@link List} of {@link IHostKey} corresponding to the given
	 *         host and type this Host Key Repository contains. If the given
	 *         type is <tt>null</tt>, return all {@link IHostKey} corresponding
	 *         to the given host. If the given host is <tt>null</tt>, return all
	 *         {@link IHostKey} corresponding to the given type. If both host
	 *         and type are <null>, return all {@link IHostKey} this Host Key
	 *         Repository contains. If nothing match or if this Host Key
	 *         Repository is empty, return an empty list.
	 */
	public List<IHostKey> get(Host host, HostKeyType keyType);

	public void add(IHostKey hostkey);

	/**
	 * Remove all {@link IHostKey} corresponding to the given host and type this
	 * Host Key Repository contains. If the given type is <tt>null</tt>, remove
	 * all {@link IHostKey} corresponding to the given host. If the given host
	 * is <tt>null</tt>, don't do anything.
	 * 
	 * @param host
	 *            The fqdn or ip address of the host keys to remove. Can be
	 *            <tt>null</tt>.
	 * @param keyType
	 *            The type of the host keys to remove. Can be <tt>null</tt>.
	 */
	public void remove(Host host, HostKeyType keyType);

}