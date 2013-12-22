package com.wat.melody.plugin.aws.s3.common;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.plugin.aws.s3.common.exception.AwsPlugInS3Exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractBucketOperation extends AbstractOperation {

	public static final String BUCKET_REGION_ATTR = "region";

	private String _bucketRegion = null;

	public AbstractBucketOperation() {
		super();
	}

	@Override
	public void validate() throws AwsPlugInS3Exception {
		super.validate();
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

}