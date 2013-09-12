package com.wat.melody.plugin.aws.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.wat.cloud.aws.s3.AwsS3Cloud;
import com.wat.cloud.aws.s3.exception.BucketDoesNotExistsException;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
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
@Task(name = DeleteBucket.DELETE_BUCKET)
public class DeleteBucket extends AbstractOperation {

	private static Logger log = LoggerFactory.getLogger(DeleteBucket.class);

	public static final String DELETE_BUCKET = "delete-bucket";

	public static final String CLEAN_ATTR = "remove-all-objects";

	private boolean _clean = true;

	@Override
	public void validate() throws AwsPlugInS3Exception {
		super.validate();
	}

	@Override
	public void doProcessing() throws AwsPlugInS3Exception,
			InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			if (getClean() == true) {
				AwsS3Cloud.removeAllKeys(getS3Connection(), getBucketName()
						.getValue());
			}
			AwsS3Cloud.deleteBucket(getS3Connection(), getBucketName()
					.getValue());
		} catch (BucketDoesNotExistsException Ex) {
			log.info(Msg.bind(Messages.DeleteBucketMsg_NOT_EXISTS,
					getBucketName()));
		} catch (AmazonClientException Ex) {
			throw new AwsPlugInS3Exception(Msg.bind(
					Messages.DeleteBucketEx_GENERIC_FAIL, getBucketName()), Ex);
		}

	}

	public boolean getClean() {
		return _clean;
	}

	@Attribute(name = CLEAN_ATTR)
	public boolean setClean(boolean b) {
		boolean previous = getClean();
		_clean = b;
		return previous;
	}

}