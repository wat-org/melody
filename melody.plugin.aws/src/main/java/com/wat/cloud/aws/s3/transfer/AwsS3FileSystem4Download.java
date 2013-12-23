package com.wat.cloud.aws.s3.transfer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.cloud.aws.s3.Messages;
import com.wat.melody.common.ex.WrapperInterruptedIOException;
import com.wat.melody.common.files.LocalFileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperDirectoryNotEmptyException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.exception.TemplatingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsS3FileSystem4Download extends LocalFileSystem implements
		TransferableFileSystem {

	private AwsS3Wrapper _s3Connection = null;
	private BucketName _bucketName = null;
	private TemplatingHandler _templatingHandler;

	public AwsS3FileSystem4Download(AmazonS3 s3Connection,
			BucketName bucketName, TemplatingHandler th) {
		super();
		setS3(new AwsS3Wrapper(s3Connection));
		setBucketName(bucketName);
		setTemplatingHandler(th);
	}

	protected AwsS3Wrapper getS3() {
		return _s3Connection;
	}

	private AwsS3Wrapper setS3(AwsS3Wrapper s3) {
		if (s3 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AwsS3Wrapper.class.getCanonicalName() + ".");
		}
		AwsS3Wrapper previous = getS3();
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

	protected String getBN() {
		return _bucketName.getValue();
	}

	protected TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	protected TemplatingHandler setTemplatingHandler(TemplatingHandler th) {
		if (th == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TemplatingHandler.class.getCanonicalName() + ".");
		}
		TemplatingHandler previous = getTemplatingHandler();
		_templatingHandler = th;
		return previous;
	}

	@Override
	public void transferRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		download(src, dest);
		setAttributes(dest, attrs);
	}

	@Override
	public void transformRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws TemplatingException, IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		// download the file
		download(src, dest);
		// expand the into itself
		getTemplatingHandler().doTemplate(dest, dest);
		setAttributes(dest, attrs);
	}

	private void download(Path source, Path destination) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		download(AwsS3FileSystem.convertToS3Path(source),
				destination.toString());
	}

	private void download(String source, String destination)
			throws IOException, InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		// must validate source
		if (source == null || source.trim().length() == 0) {
			throw new IllegalArgumentException(source + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		/*
		 * /!\ do not release this FS ! cause it shares with this object the
		 * same underlying connection to the remote system.
		 */
		AwsS3FileSystem remotefs = new AwsS3FileSystem(getS3().getS3(),
				getBucketName());
		// fail if source doesn't exists
		AwsS3FileAttributes sourceAttrs = remotefs.readAttributes(source);
		// fail if source is a directory
		if (sourceAttrs.isDirectory()) {
			throw new WrapperDirectoryNotEmptyException(source);
		}
		// Fail if destination is a directory
		if (isDirectory(destination)) {
			throw new WrapperDirectoryNotEmptyException(destination);
		}
		ProgressMonitor pm = new ProgressMonitor(getBN(), null, source,
				destination, sourceAttrs.size());
		InputStream fis = null;
		FileOutputStream fos = null;
		byte[] datas = null;
		try {
			/*
			 * When interrupted, the object is still downloading by underlying
			 * getObject method because it doesn't correctly deal with
			 * Interruption. The underlying org.apache.http.impl will detect
			 * interruption and retry...
			 * 
			 * Even this behavior is not good, this code do the job and will
			 * throw InterruptedIOException.
			 * 
			 * If it fails to detect interruption, the caller should deal with
			 * this situation.
			 * 
			 * Maybe I should use the TransferManager.
			 */
			fos = new FileOutputStream(destination);
			fis = getS3().download(getBN(), source, pm);

			int read = -1;
			datas = new byte[1024];
			while ((read = fis.read(datas)) > 0) {
				fos.write(datas, 0, read);
				if (Thread.interrupted()) {
					throw new InterruptedIOException();
				}
			}
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() != null
					&& Ex.getMessage().indexOf("Forbidden") != -1) {
				throw new WrapperAccessDeniedException(destination, Ex);
			} else {
				throw new IOException(Msg.bind(Messages.S3fsEx_GET, source,
						destination), Ex);
			}
		} catch (AmazonClientException Ex) {
			if (AwsS3FileSystem.containsInterruptedException(Ex)) {
				throw new InterruptedIOException(
						Messages.S3fsEx_PUT_INTERRUPTED);
			}
			throw new IOException(Msg.bind(Messages.S3fsEx_GET, source,
					destination), Ex);
		} catch (FileNotFoundException Ex) {
			String msg = Ex.getMessage();
			if (msg != null && msg.indexOf(" (Permission denied)") != -1) {
				throw new WrapperAccessDeniedException(destination);
			} else if (msg != null
					&& msg.indexOf(" (No such file or directory)") != -1) {
				throw new WrapperNoSuchFileException(destination);
			} else if (msg != null && msg.indexOf(" (Is a directory)") != -1) {
				throw new WrapperDirectoryNotEmptyException(destination);
			} else {
				throw new WrapperNoSuchFileException(source, Ex.getCause());
			}
		} catch (InterruptedIOException Ex) {
			throw new WrapperInterruptedIOException(
					Messages.S3fsEx_GET_INTERRUPTED, Ex);
		} catch (IOException Ex) {
			if (Thread.interrupted()) {
				throw new InterruptedIOException(
						Messages.S3fsEx_GET_INTERRUPTED);
			} else {
				throw new IOException(Msg.bind(Messages.S3fsEx_GET, source,
						destination), Ex);
			}
		} finally {
			datas = null;
			if (fos != null)
				fos.close();
			if (fis != null)
				fis.close();
		}
	}

}