package com.wat.cloud.aws.s3;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.BucketLoggingConfiguration;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.DeleteVersionRequest;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest;
import com.amazonaws.services.s3.model.VersionListing;
import com.wat.cloud.aws.s3.exception.BucketAlreadyOwnedByYouException;
import com.wat.cloud.aws.s3.exception.BucketDoesNotExistsException;
import com.wat.cloud.aws.s3.exception.DeleteKeyException;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.WrapperInterruptedException;
import com.wat.melody.common.messages.Msg;

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
	 *            can be <tt>null</tt>. If <tt>null</tt>, the default region
	 *            (e.g. 'us-east-1') will be used.
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
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an Aws S3 Bucket Name).");
		}

		boolean created = false;
		int nbtry = 3;
		while (!created){
			try {
				if (bucketRegion == null || bucketRegion.equals("us-east-1")) {
					s3.createBucket(bucketName);
				} else {
					s3.createBucket(bucketName, bucketRegion);
				}
				created = true;
			} catch (AmazonServiceException Ex) {
				if (Ex.getErrorCode() == null) {
					throw Ex;
				} else if (Ex.getErrorCode().indexOf("BucketAlreadyOwnedByYou") != -1) {
					// Means that the given bucket already exists and is yours
					throw new BucketAlreadyOwnedByYouException(Ex);
				} else if (Ex.getErrorCode().indexOf("OperationAborted") != -1) {
					// Means that another operation is in progress on this bucket
					// Retrying
					if (--nbtry < 0) {
						throw Ex;
					}
				} else {
					throw Ex;
				}
			}
		}
	}

	/**
	 * @param s3
	 *            is an {@link AmazonS3} object.
	 * @param bucketName
	 * @param loggingBucket
	 * @param logPrefix
	 * 
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
	 * @throws IllegalArgumentException
	 *             if loggingBucket is <code>null</code>.
	 */
	public static void enableLogging(AmazonS3 s3, String bucketName,
			String loggingBucket, String logPrefix)
			throws AmazonServiceException, AmazonClientException {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		if (bucketName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the Aws S3 Bucket Name to active logging on).");
		}
		if (loggingBucket == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the Aws S3 Bucket Name which will receive logging).");
		}
		if (logPrefix == null) {
			logPrefix = "";
		}

		/*
		 * Must give the log-delivery group WRITE and READ_ACP permissions to
		 * the target bucket. Bucket ACL is updated only if needed.
		 */
		AccessControlList acl = s3.getBucketAcl(loggingBucket);
		boolean updACL = false;
		Set<Grant> grants = acl.getGrants();
		Grant write = new Grant(GroupGrantee.LogDelivery, Permission.Write);
		if (!grants.contains(write)) {
			acl.grantAllPermissions(write);
			updACL = true;
		}
		Grant read = new Grant(GroupGrantee.LogDelivery, Permission.ReadAcp);
		if (!grants.contains(read)) {
			acl.grantAllPermissions(read);
			updACL = true;
		}

		if (updACL == true) {
			try {
				s3.setBucketAcl(loggingBucket, acl);
			} catch (AmazonServiceException Ex) {
				if (Ex.getErrorCode() == null) {
					throw Ex;
				} else {
					throw Ex;
				}
			}
		}

		BucketLoggingConfiguration lc = null;
		lc = new BucketLoggingConfiguration(loggingBucket, logPrefix);
		SetBucketLoggingConfigurationRequest sblcreq = null;
		sblcreq = new SetBucketLoggingConfigurationRequest(bucketName, lc);

		try {
			s3.setBucketLoggingConfiguration(sblcreq);
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
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
	 * @throws AmazonServiceException
	 *             if the operation fails (typically when the requested bucket
	 *             already exists and is not owned by you).
	 * @throws AmazonClientException
	 *             if the operation fails (typically when network communication
	 *             encountered problems).
	 * @throws IllegalArgumentException
	 *             if s3 is <code>null</code>.
	 */
	public static void disableLogging(AmazonS3 s3, String bucketName)
			throws AmazonServiceException, AmazonClientException {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		if (bucketName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the Aws S3 Bucket Name to active logging on).");
		}

		/*
		 * If either the targetBucketName or logfilePrefix are null, this object
		 * represents a disabled logging configuration
		 */
		BucketLoggingConfiguration lc = null;
		lc = new BucketLoggingConfiguration(null, null);
		SetBucketLoggingConfigurationRequest sblcreq = null;
		sblcreq = new SetBucketLoggingConfigurationRequest(bucketName, lc);

		try {
			s3.setBucketLoggingConfiguration(sblcreq);
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
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
			throws BucketDoesNotExistsException, DeleteKeyException,
			InterruptedException, AmazonClientException,
			AmazonServiceException, AmazonS3Exception {
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
			throws InterruptedException, DeleteKeyException,
			AmazonClientException, AmazonServiceException, AmazonS3Exception {
		removeAllKeysInVersionningEnabledBucket(s3, bucketName, null);
	}

	public static void removeAllKeysInVersionningEnabledBucket(
			final AmazonS3 s3, final String bucketName, final String prefix)
			throws InterruptedException, DeleteKeyException,
			AmazonClientException, AmazonServiceException, AmazonS3Exception {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		if (bucketName == null) {
			return;
		}

		// Set up a new thread pool to delete 20 objects at a time.
		ExecutorService _pool = Executors.newFixedThreadPool(10);

		// place all errors in a Consolidated exception
		final ConsolidatedException full = new ConsolidatedException(
				Messages.DeleteKeyEx_ERROR_SUMMARY);
		boolean interrupted = false;

		// List all key in the bucket
		VersionListing listing = s3.listVersions(bucketName, prefix);
		List<S3VersionSummary> summaries = listing.getVersionSummaries();
		while (summaries != null && summaries.size() > 0) {
			final CountDownLatch latch = new CountDownLatch(summaries.size());
			for (final S3VersionSummary objectSummary : summaries) {
				_pool.execute(new Runnable() {
					@Override
					public void run() {
						String keyName = null;
						String versionId = null;
						try {
							keyName = objectSummary.getKey();
							versionId = objectSummary.getVersionId();

							log.debug(Msg.bind(
									Messages.DeleteKeyMsg_DELETING_VERSION,
									bucketName, keyName, versionId));

							s3.deleteVersion(new DeleteVersionRequest(
									bucketName, keyName, versionId));

							log.info(Msg.bind(
									Messages.DeleteKeyMsg_DELETED_VERSION,
									bucketName, keyName, versionId));
						} catch (Throwable Ex) {
							full.addCause(new DeleteKeyException(Msg.bind(
									Messages.DeleteKeyEx_FAILED_VERSION,
									bucketName, keyName, versionId), Ex));
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
				full.addCause(new WrapperInterruptedException(
						Messages.DeleteKeyEx_INTERRUPTED, Ex));
				interrupted = true;
				break;
			}

			// Paging over all S3 keys...
			listing = s3.listNextBatchOfVersions(listing);
			summaries = listing.getVersionSummaries();
		}

		_pool.shutdown();

		// raise error
		if (full.countCauses() != 0) {
			if (interrupted == true && full.countCauses() == 1) {
				throw new WrapperInterruptedException(full);
			} else {
				throw new DeleteKeyException(full);
			}
		}
	}

	public static void removeAllKeysInVersionningDisabledBucket(
			final AmazonS3 s3, final String bucketName)
			throws InterruptedException, DeleteKeyException,
			AmazonClientException, AmazonServiceException, AmazonS3Exception {
		removeAllKeysInVersionningDisabledBucket(s3, bucketName, null);
	}

	public static void removeAllKeysInVersionningDisabledBucket(
			final AmazonS3 s3, final String bucketName, final String prefix)
			throws InterruptedException, DeleteKeyException,
			AmazonClientException, AmazonServiceException, AmazonS3Exception {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		if (bucketName == null) {
			return;
		}
		// Set up a new thread pool to delete 20 objects at a time.
		ExecutorService pool__ = Executors.newFixedThreadPool(10);

		// place all errors in a Consolidated exception
		final ConsolidatedException full = new ConsolidatedException(
				Messages.DeleteKeyEx_ERROR_SUMMARY);
		boolean interrupted = false;

		ObjectListing listing = s3.listObjects(bucketName, prefix);
		List<S3ObjectSummary> summaries = listing.getObjectSummaries();
		while (summaries != null && summaries.size() > 0) {
			// List all key in the bucket
			/*
			 * Create a new CountDownLatch with a size of how many objects we
			 * fetched. Each worker thread will decrement the latch on
			 * completion; the parent waits until all workers are finished
			 * before starting a new batch of delete worker threads.
			 */
			final CountDownLatch latch = new CountDownLatch(summaries.size());
			for (final S3ObjectSummary object : summaries) {
				pool__.execute(new Runnable() {
					@Override
					public void run() {
						String keyName = null;
						try {
							keyName = object.getKey();

							log.debug(Msg.bind(Messages.DeleteKeyMsg_DELETING,
									bucketName, keyName));

							s3.deleteObject(bucketName, keyName);

							log.info(Msg.bind(Messages.DeleteKeyMsg_DELETED,
									bucketName, keyName));
						} catch (Throwable Ex) {
							full.addCause(new DeleteKeyException(Msg.bind(
									Messages.DeleteKeyEx_FAILED, bucketName,
									keyName), Ex));
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
				full.addCause(new WrapperInterruptedException(
						Messages.DeleteKeyEx_INTERRUPTED, Ex));
				interrupted = true;
				break;
			}

			// Paging over all S3 keys...
			listing = s3.listNextBatchOfObjects(listing);
			summaries = listing.getObjectSummaries();
		}

		pool__.shutdown();

		// raise error
		if (full.countCauses() != 0) {
			if (interrupted == true && full.countCauses() == 1) {
				throw new WrapperInterruptedException(full);
			} else {
				throw new DeleteKeyException(full);
			}
		}
	}

}