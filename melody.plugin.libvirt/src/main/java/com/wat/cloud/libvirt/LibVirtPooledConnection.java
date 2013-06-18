package com.wat.cloud.libvirt;

import java.util.Hashtable;
import java.util.Map;

import org.libvirt.Connect;
import org.libvirt.ConnectAuth;
import org.libvirt.LibvirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LibVirtPooledConnection {

	private static Map<String, Connect> _connectionPool = new Hashtable<String, Connect>();

	/**
	 * @param region
	 *            is the requested region.
	 * 
	 * @return a {@link Connect} object which is already configured for the
	 *         requested region, or <tt>null</tt> if the requested region is not
	 *         valid.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given region is <tt>null</tt>.
	 */
	public synchronized static Connect getCloudConnection(String region,
			ConnectAuth cred) {
		if (region == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a LibVirt Connection URL).");
		}
		Connect connect = null;
		if (_connectionPool.containsKey(region)) {
			connect = _connectionPool.get(region);
		}
		if (connect == null) {
			try {
				if (cred == null) {
					connect = new Connect(region);
				} else {
					/*
					 * TODO :deal with ConnectAuth, to specify credentials.
					 */
					connect = new Connect(region, cred, 0);
				}
			} catch (LibvirtException Ex) {
				return null;
			}
			_connectionPool.put(region, connect);
		}
		return connect;
	}

}