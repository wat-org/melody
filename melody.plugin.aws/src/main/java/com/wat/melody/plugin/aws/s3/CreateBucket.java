package com.wat.melody.plugin.aws.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.wat.cloud.aws.s3.AwsS3Cloud;
import com.wat.cloud.aws.s3.exception.BucketAlreadyOwnedByYouException;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.plugin.aws.s3.common.AbstractOperation;
import com.wat.melody.plugin.aws.s3.common.Messages;
import com.wat.melody.plugin.aws.s3.common.exception.AwsPlugInS3Exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = CreateBucket.CREATE_BUCKET)
public class CreateBucket extends AbstractOperation {

	private static Logger log = LoggerFactory.getLogger(CreateBucket.class);

	public static final String CREATE_BUCKET = "create-bucket";

	@Override
	public void validate() throws AwsPlugInS3Exception {
		super.validate();
	}

	@Override
	public void doProcessing() throws AwsPlugInS3Exception,
			InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			AwsS3Cloud.createBucket(getS3Connection(), getBucketName()
					.getValue(), getBucketRegion());
		} catch (BucketAlreadyOwnedByYouException Ex) {
			log.info(Msg.bind(Messages.CreateBucketMsg_ALREADY_EXISTS,
					getBucketName()));
		} catch (AmazonClientException Ex) {
			throw new AwsPlugInS3Exception(Msg.bind(
					Messages.CreateBucketEx_GENERIC_FAIL, getBucketName(),
					getBucketRegion()), Ex);
		}
	}

}