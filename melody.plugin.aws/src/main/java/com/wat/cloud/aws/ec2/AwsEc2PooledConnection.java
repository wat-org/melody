package com.wat.cloud.aws.ec2;

import java.util.Hashtable;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AwsEc2PooledConnection {

	private static Map<String, AmazonEC2> _connectionPool = new Hashtable<String, AmazonEC2>();

	/**
	 * @param region
	 *            is the requested region.
	 * @param cred
	 *            is the {@link AWSCredentials}, necessary to open the Cloud
	 *            Connection to the requested region.
	 * @param cc
	 *            is the {@link ClientConfiguration} to apply to the Cloud
	 *            Connection to the requested region. Can be <tt>null</tt>. If
	 *            <tt>null</tt>, default settings will be used.
	 * 
	 * @return a {@link AmazonEC2} object which is already configured for the
	 *         requested region, or <tt>null</tt> if the requested region is not
	 *         valid.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given region is <tt>null</tt> ;</li>
	 *             <li>if the given {@link AWSCredentials} is <tt>null</tt> ;</li>
	 *             </ul>
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 */
	public static AmazonEC2 getCloudConnection(String region,
			AWSCredentials cred, ClientConfiguration cc) {
		if (region == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an AWS EC2 Region).");
		}
		if (cred == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AWSCredentials.class.getCanonicalName() + ".");
		}
		AmazonEC2 connect = null;
		if (_connectionPool.containsKey(region)) {
			connect = _connectionPool.get(region);
		}
		if (connect == null) {
			connect = new AmazonEC2Client(cred, cc);
			String ep = AwsEc2Cloud.getEndpoint(connect, region);
			if (ep == null) {
				return null;
			}
			connect.setEndpoint(ep);
			_connectionPool.put(region, connect);
		}
		return connect;
	}

}