package com.wat.cloud.aws.s3.transfer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.wat.cloud.aws.s3.AwsS3Cloud;
import com.wat.cloud.aws.s3.exception.DeleteKeyException;
import com.wat.melody.common.files.FileSystem;

/**
 * <p>
 * Our file system aim is to made all {@link FileSystem} implementations
 * uniforms. But S3 handle specifically the root element : root element (e.g.
 * '/') doesn't need to be specified in S3. For this reason, we have to provide
 * S3 methods which will correctly handle the root element. This class is
 * especially design to do that.
 * </p>
 * 
 * <p>
 * Ideally, this class should implements {@link AmazonS3} but it is too much
 * work for now.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsS3Wrapper {

	private static String adaptKey(String key) {
		if (key == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an AWS Bucket Key).");
		}
		if (key.charAt(0) == '/') {
			// key must be absolute with no root element
			key = key.substring(1);
		}
		if (key.length() == 0) {
			// root element doesn't need to be specified in S3
			return null;
		}
		return key;
	}

	protected static final ObjectMetadata ROOT_METADATAS;

	static {
		ROOT_METADATAS = new ObjectMetadata();
		ROOT_METADATAS.setContentLength(0);
		Map<String, String> userMetadatas = new HashMap<String, String>();
		userMetadatas.put(AwsS3FileAttributes.DIRECTORY_FLAG, "");
		ROOT_METADATAS.setUserMetadata(userMetadatas);
	}

	private AmazonS3 _s3Connection = null;

	public AwsS3Wrapper(AmazonS3 s3) {
		setS3(s3);
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

	public PutObjectResult putObject(String bn, String key, InputStream is,
			ObjectMetadata metadatas) throws AmazonClientException,
			AmazonServiceException, AmazonS3Exception {
		if (bn == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an AWS Bucket Name).");
		}
		key = adaptKey(key);
		if (key == null) {
			return null;
		}
		return getS3().putObject(bn, key, is, metadatas);
	}

	public void deleteObject(String bn, String key)
			throws AmazonClientException, AmazonServiceException,
			AmazonS3Exception {
		if (bn == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an AWS Bucket Name).");
		}
		key = adaptKey(key);
		if (key == null) {
			return;
		}
		getS3().deleteObject(bn, key);
	}

	public ObjectListing listDirectory(String bn, String key)
			throws AmazonClientException, AmazonServiceException,
			AmazonS3Exception {
		if (bn == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an AWS Bucket Name).");
		}
		key = adaptKey(key);
		ListObjectsRequest loreq = null;
		loreq = new ListObjectsRequest(bn, key, key, "/", 100);
		return getS3().listObjects(loreq);
	}

	public ObjectMetadata getObjectMetadata(String bn, String key)
			throws AmazonClientException, AmazonServiceException,
			AmazonS3Exception {
		if (bn == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an AWS Bucket Name).");
		}
		key = adaptKey(key);
		if (key == null) {
			// workaround: root element doesn't need to be specified in S3
			// when targeted, we return a specifically build structure
			return ROOT_METADATAS;
		}
		return getS3().getObjectMetadata(bn, key);
	}

	public void setObjectMetadata(String bn, String key,
			ObjectMetadata metadatas) throws AmazonClientException,
			AmazonServiceException, AmazonS3Exception {
		if (bn == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an AWS Bucket Name).");
		}
		key = adaptKey(key);
		if (key == null) {
			return;
		}
		CopyObjectRequest coreq = null;
		coreq = new CopyObjectRequest(bn, key, bn, key);
		coreq.setNewObjectMetadata(metadatas);
		coreq.setAccessControlList(getS3().getObjectAcl(bn, key));
		getS3().copyObject(coreq);
	}

	protected void removeAllKeysInVersionningDisabledBucket(String bn,
			String key) throws InterruptedException, DeleteKeyException,
			AmazonClientException, AmazonServiceException, AmazonS3Exception {
		key = adaptKey(key);
		AwsS3Cloud.removeAllKeysInVersionningDisabledBucket(getS3(), bn, key);
	}

	/**
	 * @param bn
	 * @param source
	 * @param destination
	 * @param metadatas
	 *            Should provide the length of the file to upload.an be
	 *            <tt>null</tt>. If <tt>null</tt>, no content length will be
	 *            specified for stream data, and stream contents will be
	 *            buffered in memory and could result in out of memory errors.
	 * @param pl
	 *            Can be <tt>null</tt>. If <tt>null</tt>, no upload progress
	 *            notification will be send.
	 */
	public void upload(String bn, InputStream source, String destination,
			ObjectMetadata metadatas, ProgressListener pl) {
		if (bn == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an AWS Bucket Name).");
		}
		destination = adaptKey(destination);
		if (destination == null) {
			return;
		}
		PutObjectRequest poreq = null;
		poreq = new PutObjectRequest(bn, destination, source, metadatas);
		poreq.setGeneralProgressListener(pl);
		getS3().putObject(poreq);
	}

	/**
	 * @param bn
	 * @param source
	 * @param pl
	 *            Can be <tt>null</tt>. If <tt>null</tt>, no upload progress
	 *            notification will be send.
	 */
	public S3ObjectInputStream download(String bn, String source,
			ProgressListener pl) {
		if (bn == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an AWS Bucket Name).");
		}
		source = adaptKey(source);
		if (source == null) {
			return null;
		}
		GetObjectRequest poreq = new GetObjectRequest(bn, source);
		poreq.setGeneralProgressListener(pl);
		return getS3().getObject(poreq).getObjectContent();
	}

}