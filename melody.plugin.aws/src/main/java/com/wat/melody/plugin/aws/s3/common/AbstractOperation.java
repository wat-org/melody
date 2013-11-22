package com.wat.melody.plugin.aws.s3.common;

import com.amazonaws.services.s3.AmazonS3;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.melody.api.ITask;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.plugin.aws.common.AwsPlugInConfiguration;
import com.wat.melody.plugin.aws.s3.common.exception.AwsPlugInS3Exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractOperation implements ITask {

	public static final String BUCKET_NAME_ATTR = "name";

	private AmazonS3 _s3Connection = null;
	private BucketName _bucketName = null;

	protected AwsPlugInConfiguration getAwsPlugInConfiguration()
			throws AwsPlugInS3Exception {
		try {
			return AwsPlugInConfiguration.get();
		} catch (PlugInConfigurationException Ex) {
			throw new AwsPlugInS3Exception(Ex);
		}
	}

	@Override
	public void validate() throws AwsPlugInS3Exception {
	}

	protected AmazonS3 getS3Connection() {
		return _s3Connection;
	}

	protected AmazonS3 setS3Connection(AmazonS3 s3) {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		AmazonS3 previous = getS3Connection();
		_s3Connection = s3;
		return previous;
	}

	public BucketName getBucketName() {
		return _bucketName;
	}

	@Attribute(name = BUCKET_NAME_ATTR, mandatory = true)
	public BucketName setBucketName(BucketName bucketName) {
		if (bucketName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + BucketName.class.getCanonicalName()
					+ ".");
		}
		BucketName previous = getBucketName();
		_bucketName = bucketName;
		return previous;
	}

}