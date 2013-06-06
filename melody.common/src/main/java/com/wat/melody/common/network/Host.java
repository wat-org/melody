package com.wat.melody.common.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.wat.melody.common.network.exception.IllegalHostException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Host {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to an {@link Host} object.
	 * </p>
	 * 
	 * @param sHost
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link Host} object, whose equal to the given input
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalHostException
	 *             if the given <tt>String</tt> is not a valid {@link Host}
	 *             (e.g. : is neither an ipv4, nor an ipv6, nor an hostname, nor
	 *             a full qualified domain name).
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 */
	public static Host parseString(String sHost) throws IllegalHostException {
		return new Host(sHost);
	}

	private InetAddress _inetAddress;

	public Host(String sHost) throws IllegalHostException {
		setValue(sHost);
	}

	@Override
	public int hashCode() {
		return getAddress().hashCode();
	}

	@Override
	public String toString() {
		return getAddress();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof Host) {
			Host host = (Host) anObject;
			return getAddress().equals(host.getAddress());
		}
		return false;
	}

	public String getName() {
		return _inetAddress.getHostName();
	}

	public String getAddress() {
		return _inetAddress.getHostAddress();
	}

	private InetAddress getValue() {
		return _inetAddress;
	}

	private InetAddress setValue(String sHost) throws IllegalHostException {
		if (sHost == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (which represents a "
					+ "HostName, FQND, InetV4Address or InetV6Address)");
		}
		if (sHost.trim().length() == 0) {
			throw new IllegalHostException(Messages.bind(Messages.HostEx_EMPTY,
					sHost));
		}
		InetAddress previous = getValue();
		try {
			_inetAddress = InetAddress.getByName(sHost);
		} catch (UnknownHostException Ex) {
			throw new IllegalHostException(Messages.bind(
					Messages.HostEx_INVALID, sHost), Ex);
		}
		return previous;
	}

}