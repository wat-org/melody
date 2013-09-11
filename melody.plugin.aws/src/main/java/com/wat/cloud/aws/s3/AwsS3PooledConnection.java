package com.wat.cloud.aws.s3;

import java.util.Hashtable;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsS3PooledConnection {

	private static Map<String, AmazonS3> _connectionPool = new Hashtable<String, AmazonS3>();

	/**
	 * @param cred
	 *            is the {@link AWSCredentials}, necessary to open the S3
	 *            Connection.
	 * @param cc
	 *            is the {@link ClientConfiguration} to apply to the S3
	 *            Connection. Can be <tt>null</tt>. If <tt>null</tt>, default
	 *            settings will be used.
	 * 
	 * @return an {@link AmazonS3} object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link AWSCredentials} is <tt>null</tt>.
	 * @throws AmazonServiceException
	 *             if the operation fails (ex: credentials not valid).
	 * @throws AmazonClientException
	 *             if the operation fails (ex: network error).
	 */
	public static AmazonS3 getPooledConnection(AWSCredentials cred,
			ClientConfiguration cc) {
		if (cred == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AWSCredentials.class.getCanonicalName() + ".");
		}

		String hkey = cred.getAWSAccessKeyId();
		AmazonS3 connect = null;
		if (_connectionPool.containsKey(hkey)) {
			connect = _connectionPool.get(hkey);
		}
		if (connect == null) {
			connect = new AmazonS3Client(cred, cc);
			_connectionPool.put(hkey, connect);
		}
		return connect;
	}

}