package com.wat.cloud.aws.s3;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.DeleteVersionRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.wat.cloud.aws.s3.exception.BucketAlreadyOwnedByYouException;
import com.wat.cloud.aws.s3.exception.BucketDoesNotExistsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AwsS3Cloud {

	private static Logger log = LoggerFactory.getLogger(AwsS3Cloud.class);

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
	 *             is not owned by you, or when it is not empty).
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

	public static void removeAllKeys(AmazonS3 s3, String bucketName)
			throws BucketDoesNotExistsException, InterruptedException {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		if (bucketName == null) {
			return;
		}

		try {
			BucketVersioningConfiguration bvc = null;
			bvc = s3.getBucketVersioningConfiguration(bucketName);
			if (bvc.getStatus().equalsIgnoreCase(
					BucketVersioningConfiguration.OFF)) {
				removeAllKeysInVersionningDisabledBucket(s3, bucketName);
			} else {
				removeAllKeysInVersionningEnabledBucket(s3, bucketName);
			}
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

	public static void removeAllKeysInVersionningEnabledBucket(
			final AmazonS3 s3, final String bucketName)
			throws InterruptedException {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		if (bucketName == null) {
			return;
		}

		// Set up a new thread pool to delete 20 objects at a time.
		ExecutorService _pool = Executors.newFixedThreadPool(20);

		// List all key in the bucket
		VersionListing versionListing = s3.listVersions(bucketName, "");
		List<S3VersionSummary> versionSummaries = versionListing
				.getVersionSummaries();
		while (versionSummaries != null && versionSummaries.size() > 0) {
			final CountDownLatch latch = new CountDownLatch(
					versionSummaries.size());
			for (final S3VersionSummary objectSummary : versionSummaries) {
				_pool.execute(new Runnable() {
					@Override
					public void run() {
						String keyName = null;
						String versionId = null;
						try {
							keyName = objectSummary.getKey();
							versionId = objectSummary.getVersionId();

							log.info("Deleting object { bucket-name:"
									+ bucketName + ", key:" + keyName
									+ ", version-id:" + versionId + " }");

							s3.deleteVersion(new DeleteVersionRequest(
									bucketName, keyName, versionId));
						} catch (AmazonClientException e) {
							log.error("Failed to delete object { bucket-name:"
									+ bucketName + ", key:" + keyName
									+ ", version-id:" + versionId + " }");
							throw e;
						} finally {
							latch.countDown();
						}
					}
				});
			}

			/*
			 * After sending current batch of delete tasks, we block until Latch
			 * reaches zero, this allows to not over populate ExecutorService
			 * tasks queue.
			 */
			try {
				latch.await();
			} catch (InterruptedException Ex) {
				latch.await();
				throw Ex;
			}

			// Paging over all S3 keys...
			versionListing = s3.listNextBatchOfVersions(versionListing);
			versionSummaries = versionListing.getVersionSummaries();
		}

		_pool.shutdown();

	}

	public static void removeAllKeysInVersionningDisabledBucket(
			final AmazonS3 s3, final String bucketName)
			throws InterruptedException {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		if (bucketName == null) {
			return;
		}

		// Set up a new thread pool to delete 20 objects at a time.
		ExecutorService pool__ = Executors.newFixedThreadPool(20);

		List<S3ObjectSummary> objects = null;
		do {
			// List all key in the bucket
			objects = s3.listObjects(bucketName).getObjectSummaries();
			/*
			 * Create a new CountDownLatch with a size of how many objects we
			 * fetched. Each worker thread will decrement the latch on
			 * completion; the parent waits until all workers are finished
			 * before starting a new batch of delete worker threads.
			 */
			final CountDownLatch latch = new CountDownLatch(objects.size());
			for (final S3ObjectSummary object : objects) {
				pool__.execute(new Runnable() {
					@Override
					public void run() {
						String keyName = null;
						try {
							keyName = object.getKey();

							log.info("Deleting object { bucket-name:"
									+ bucketName + ", key:" + keyName + " }");

							s3.deleteObject(bucketName, keyName);
						} catch (AmazonClientException e) {
							log.error("Failed to delete object { bucket-name:"
									+ bucketName + ", key:" + keyName + " }");
							throw e;
						} finally {
							latch.countDown();
						}
					}
				});
			}
			/*
			 * After sending current batch of delete tasks, we block until Latch
			 * reaches zero, this allows to not over populate ExecutorService
			 * tasks queue.
			 */
			try {
				latch.await();
			} catch (InterruptedException Ex) {
				latch.await();
				throw Ex;
			}
		} while (objects != null && !objects.isEmpty());

		pool__.shutdown();
	}

}