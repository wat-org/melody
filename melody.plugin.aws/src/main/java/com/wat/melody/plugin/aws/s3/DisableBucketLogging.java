package com.wat.melody.plugin.aws.s3;

import com.amazonaws.AmazonClientException;
import com.wat.cloud.aws.s3.AwsS3Cloud;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.plugin.aws.s3.common.AbstractBucketOperation;
import com.wat.melody.plugin.aws.s3.common.Messages;
import com.wat.melody.plugin.aws.s3.common.exception.AwsPlugInS3Exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = DisableBucketLogging.DISABLE_BUCKET_LOGGING)
public class DisableBucketLogging extends AbstractBucketOperation {

	public static final String DISABLE_BUCKET_LOGGING = "disable-bucket-logging";

	@Override
	public void validate() throws AwsPlugInS3Exception {
		super.validate();
	}

	@Override
	public void doProcessing() throws TaskException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			AwsS3Cloud.disableLogging(getS3Connection(), getBucketName()
					.getValue());
		} catch (AmazonClientException Ex) {
			throw new AwsPlugInS3Exception(Msg.bind(
					Messages.DisableLoggingEx_GENERIC_FAIL, getBucketName()),
					Ex);
		}
	}

}