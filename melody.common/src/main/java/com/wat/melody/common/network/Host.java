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
	 * Convert the given <code>String</code> to an {@link Host} object.
	 * </p>
	 * 
	 * @param sHost
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an {@link Host} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalHostException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link Host} (e.g. : is neither an ipv4, nor an ipv6, nor an
	 *             hostname, nor a full qualified domain name).
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Host parseString(String sHost) throws IllegalHostException {
		return new Host(sHost);
	}

	private InetAddress moInetAddress;

	public Host(String sHost) throws IllegalHostException {
		setValue(sHost);
	}

	@Override
	public String toString() {
		return getAddress();
	}

	public String getName() {
		return moInetAddress.getHostName();
	}

	public String getAddress() {
		return moInetAddress.getHostAddress();
	}

	private InetAddress getValue() {
		return moInetAddress;
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
			moInetAddress = InetAddress.getByName(sHost);
		} catch (UnknownHostException Ex) {
			throw new IllegalHostException(Messages.bind(
					Messages.HostEx_INVALID, sHost), Ex);
		}
		return previous;
	}

}
