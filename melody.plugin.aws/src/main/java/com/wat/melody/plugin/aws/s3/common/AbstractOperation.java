package com.wat.melody.plugin.aws.s3.common;

import com.amazonaws.services.s3.AmazonS3;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.melody.api.ITask;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.plugin.aws.common.AwsPlugInConfiguration;
import com.wat.melody.plugin.aws.s3.common.exception.AwsPlugInS3Exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractOperation implements ITask {

	public static final String BUCKET_NAME_ATTR = "name";

	public static final String BUCKET_REGION_ATTR = "region";

	private BucketName _bucketName = null;
	private String _bucketRegion = null;
	private AmazonS3 _s3Connection = null;

	@Override
	public void validate() throws AwsPlugInS3Exception {
		setS3Connection(getAwsPlugInConfiguration().getAwsS3Connection());
	}

	protected AwsPlugInConfiguration getAwsPlugInConfiguration()
			throws AwsPlugInS3Exception {
		try {
			return AwsPlugInConfiguration.get();
		} catch (PlugInConfigurationException Ex) {
			throw new AwsPlugInS3Exception(Ex);
		}
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

	public String getBucketRegion() {
		return _bucketRegion;
	}

	@Attribute(name = BUCKET_REGION_ATTR)
	public String setBucketRegion(String bucketRegion)
			throws AwsPlugInS3Exception {
		if (bucketRegion == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}

		if (getAwsPlugInConfiguration().getAwsEc2Connection(bucketRegion) == null) {
			throw new AwsPlugInS3Exception(Msg.bind(
					Messages.S3Ex_INVALID_REGION, bucketRegion));
		}
		String previous = getBucketRegion();
		_bucketRegion = bucketRegion;
		return previous;
	}

	protected AmazonS3 getS3Connection() {
		return _s3Connection;
	}

	protected AmazonS3 setS3Connection(AmazonS3 ec2) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		AmazonS3 previous = getS3Connection();
		_s3Connection = ec2;
		return previous;
	}

}