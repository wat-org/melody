package com.wat.melody.plugin.aws.s3;

import com.amazonaws.AmazonClientException;
import com.wat.cloud.aws.s3.AwsS3Cloud;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.plugin.aws.s3.common.AbstractOperation;
import com.wat.melody.plugin.aws.s3.common.Messages;
import com.wat.melody.plugin.aws.s3.common.exception.AwsPlugInS3Exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = EnableBucketLogging.ENABLE_BUCKET_LOGGING)
public class EnableBucketLogging extends AbstractOperation {

	public static final String ENABLE_BUCKET_LOGGING = "enable-bucket-logging";

	public static final String DESTINATION_BUCKET_NAME = "destination-bucket-name";

	public static final String LOG_FILE_PREFIX = "log-file-prefix";

	private BucketName _destinationBucketName = null;
	private String _logFilePrefix = null;

	@Override
	public void validate() throws AwsPlugInS3Exception {
		super.validate();

		// Assign a default value
		if (getLogFilePrefix() == null) {
			setLogFilePrefix("log_" + getBucketName() + "/");
		}
	}

	@Override
	public void doProcessing() throws TaskException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			AwsS3Cloud.enableLogging(getS3Connection(), getBucketName()
					.getValue(), getDestinationBucketName().getValue(),
					getLogFilePrefix());
		} catch (AmazonClientException Ex) {
			throw new AwsPlugInS3Exception(Msg.bind(
					Messages.EnableLoggingEx_GENERIC_FAIL, getBucketName(),
					getDestinationBucketName(), getLogFilePrefix()), Ex);
		}
	}

	public BucketName getDestinationBucketName() {
		return _destinationBucketName;
	}

	@Attribute(name = DESTINATION_BUCKET_NAME, mandatory = true)
	public BucketName setDestinationBucketName(BucketName bucketName) {
		if (bucketName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + BucketName.class.getCanonicalName()
					+ ".");
		}
		BucketName previous = getDestinationBucketName();
		_destinationBucketName = bucketName;
		return previous;
	}

	public String getLogFilePrefix() {
		return _logFilePrefix;
	}

	@Attribute(name = LOG_FILE_PREFIX)
	public String setLogFilePrefix(String prefix) {
		if (prefix == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getLogFilePrefix();
		_logFilePrefix = prefix;
		return previous;
	}

}