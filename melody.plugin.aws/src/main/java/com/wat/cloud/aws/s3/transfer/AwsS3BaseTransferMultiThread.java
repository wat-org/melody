package com.wat.cloud.aws.s3.transfer;

import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.melody.common.threads.MelodyThreadFactory;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferMultiThread;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AwsS3BaseTransferMultiThread extends TransferMultiThread {

	private AmazonS3 _s3Connection = null;
	private BucketName _bucketName = null;

	public AwsS3BaseTransferMultiThread(AmazonS3 s3Connection,
			BucketName bucketName, List<ResourcesSpecification> rss,
			int maxPar, TemplatingHandler th, MelodyThreadFactory tf) {
		super(rss, maxPar, th, tf);
		setS3(s3Connection);
		setBucketName(bucketName);
	}

	@Override
	public String getTransferProtocolDescription() {
		return "s3fs";
	}

	protected AmazonS3 getS3() {
		return _s3Connection;
	}

	private AmazonS3 setS3(AmazonS3 s3) {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonS3.class.getCanonicalName()
					+ ".");
		}
		AmazonS3 previous = getS3();
		_s3Connection = s3;
		return previous;
	}

	protected BucketName getBucketName() {
		return _bucketName;
	}

	private BucketName setBucketName(BucketName bucketName) {
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