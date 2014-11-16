package com.wat.cloud.aws.s3.transfer;

import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.threads.MelodyThreadFactory;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsS3DownloaderMultiThread extends AwsS3BaseTransferMultiThread {

	public AwsS3DownloaderMultiThread(AmazonS3 s3Connection,
			BucketName bucketName, List<ResourcesSpecification> rss,
			int maxPar, TemplatingHandler th, MelodyThreadFactory tf) {
		super(s3Connection, bucketName, rss, maxPar, th, tf);
	}

	@Override
	public String getThreadName() {
		return "downloader";
	}

	@Override
	public String getSourceSystemDescription() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("host:");
		str.append("aws-s3");
		str.append(", bucket-name:");
		str.append(getBucketName());
		str.append(" }");
		return str.toString();
	}

	@Override
	public String getDestinationSystemDescription() {
		return "local file system";
	}

	@Override
	public FileSystem newSourceFileSystem() {
		return new AwsS3FileSystem(getS3(), getBucketName());
	}

	@Override
	public TransferableFileSystem newDestinationFileSystem()
			throws InterruptedException {
		return new AwsS3FileSystem4Download(getS3(), getBucketName(),
				getTemplatingHandler());
	}

}