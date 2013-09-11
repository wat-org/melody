package com.wat.cloud.aws.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.wat.cloud.aws.s3.exception.BucketAlreadyOwnedByYouException;
import com.wat.cloud.aws.s3.exception.BucketDoesNotExistsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AwsS3Cloud {

	/**
	 * @param s3
	 *            is an {@link AmazonS3} object.
	 * @param bucketName
	 * @param bucketRegion
	 * 
	 * @throws BucketAlreadyOwnedByYouException
	 *             if a bucket with the given name already exists and is yours.
	 * @throws AmazonServiceException
	 *             if the operation fails (typically when the requested bucket
	 *             already exists and is not owned by you).
	 * @throws AmazonClientException
	 *             if the operation fails (typically when network communication
	 *             encountered problems).
	 * @throws IllegalArgumentException
	 *             if s3 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if bucketName is <code>null</code>.
	 */
	public static void createBucket(AmazonS3 s3, String bucketName,
			String bucketRegion) throws BucketAlreadyOwnedByYouException,
			AmazonServiceException, AmazonClientException {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		if (bucketName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Aws S3 Bucket Name).");
		}

		try {
			if (bucketRegion == null || bucketRegion.equals("us-east-1")) {
				s3.createBucket(bucketName);
			} else {
				s3.createBucket(bucketName, bucketRegion);
			}
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("BucketAlreadyOwnedByYou") != -1) {
				// Means that the given bucket already exists and is yours
				throw new BucketAlreadyOwnedByYouException(Ex);
			} else {
				throw Ex;
			}
		}
	}

	/**
	 * @param s3
	 *            is an {@link AmazonS3} object.
	 * @param bucketName
	 * 
	 * @throws BucketDoesNotExistsException
	 *             if a bucket with the given name doesn't exists.
	 * @throws AmazonServiceException
	 *             if the operation fails (typically when the requested bucket
	 *             is not owned by you).
	 * @throws AmazonClientException
	 *             if the operation fails (typically when network communication
	 *             encountered problems).
	 * @throws IllegalArgumentException
	 *             if s3 is <code>null</code>.
	 */
	public static void deleteBucket(AmazonS3 s3, String bucketName)
			throws BucketDoesNotExistsException, AmazonServiceException,
			AmazonClientException {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		if (bucketName == null) {
			return;
		}
		try {
			// TODO : must delete all objects before
			s3.deleteBucket(bucketName);
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("NoSuchBucket") != -1) {
				// Means that the given bucket already not exists
				throw new BucketDoesNotExistsException(Ex);
			} else {
				throw Ex;
			}
		}
	}

}