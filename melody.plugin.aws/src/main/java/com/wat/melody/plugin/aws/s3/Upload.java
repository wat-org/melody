package com.wat.melody.plugin.aws.s3;

import java.io.File;

import com.amazonaws.services.s3.AmazonS3;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.cloud.aws.s3.transfer.AwsS3UploaderMultiThread;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.common.transfer.exception.TransferException;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;
import com.wat.melody.plugin.aws.s3.common.Transfer;
import com.wat.melody.plugin.aws.s3.common.types.LocalResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Conditions({ @Condition({ @Match(expression = "§[@provider]§", value = "aws.s3") }) })
public class Upload extends Transfer {

	/**
	 * Task's name
	 */
	public static final String UPLOAD = "upload";

	public Upload() {
		super();
	}

	@Override
	public void doTransfer(AmazonS3 s3Connection, BucketName bucketName)
			throws TransferException, InterruptedException {
		new AwsS3UploaderMultiThread(s3Connection, bucketName,
				getResourcesSpecifications(), getMaxPar(), this,
				Melody.getThreadFactory()).doTransfer();
	}

	@Override
	public ResourcesSpecification newResourcesSpecification(File basedir) {
		return new LocalResourcesSpecification(basedir);
	}

}