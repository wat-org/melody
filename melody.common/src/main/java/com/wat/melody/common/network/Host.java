package com.wat.melody.common.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.wat.melody.common.messages.Msg;
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
	 * @param host
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link Host} object, which is equal to the given input
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalHostException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is not a valid {@link Host}
	 *             (e.g. : is neither an ipv4, nor an ipv6, nor an hostname, nor
	 *             a full qualified domain name) ;</li>
	 *             </ul>
	 */
	public static Host parseString(String host) throws IllegalHostException {
		return new Host(host);
	}

	private InetAddress _inetAddress;

	public Host(String host) throws IllegalHostException {
		setValue(host);
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

	private InetAddress setValue(String host) throws IllegalHostException {
		if (host == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (which represents a "
					+ "HostName, FQND, InetV4Address or InetV6Address)");
		}
		if (host.trim().length() == 0) {
			throw new IllegalHostException(
					Msg.bind(Messages.HostEx_EMPTY, host));
		}
		InetAddress previous = getValue();
		try {
			_inetAddress = InetAddress.getByName(host);
		} catch (UnknownHostException Ex) {
			throw new IllegalHostException(Msg.bind(Messages.HostEx_INVALID,
					host), Ex);
		}
		return previous;
	}

}