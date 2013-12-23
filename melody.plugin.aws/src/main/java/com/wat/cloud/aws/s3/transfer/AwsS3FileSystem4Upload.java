package com.wat.cloud.aws.s3.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.cloud.aws.s3.Messages;
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
public class AwsS3FileSystem4Upload extends AwsS3FileSystem implements
		TransferableFileSystem {

	private TemplatingHandler _templatingHandler;

	public AwsS3FileSystem4Upload(AmazonS3 s3Connection, BucketName bucketName,
			TemplatingHandler th) {
		super(s3Connection, bucketName);
		setTemplatingHandler(th);
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
		// Fail if source is a directory
		if (Files.isDirectory(src)) {
			throw new WrapperDirectoryNotEmptyException(src);
		}
		upload(src, dest);
		setAttributes(dest, attrs);
	}

	@Override
	public void transformRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws TemplatingException, IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		// expand src into a tmpfile and upload the tmpfile into dest
		// doTemplate will fail if source is not a regular file
		upload(getTemplatingHandler().doTemplate(src, null), dest);
		setAttributes(dest, attrs);
	}

	private void upload(Path source, Path destination) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		upload(source.toString(), convertToS3Path(destination));
	}

	private void upload(String source, String destination) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		// source have already been validated
		// Fail if destination is a directory
		if (isDirectory(destination)) {
			throw new WrapperDirectoryNotEmptyException(destination);
		}

		failIfParentDirectoryInvalid(destination);

		File src = new File(source);
		ProgressMonitor pm = new ProgressMonitor(null, getBN(), source,
				destination, src.length());
		FileInputStream fis = null;
		try {
			/*
			 * When interrupted, the object is still uploading by underlying
			 * putObject method because it doesn't correctly deal with
			 * Interruption. The underlying org.apache.http.impl will detect
			 * interruption and retry...
			 * 
			 * Even this behavior is not good, this code try to throw
			 * InterruptedIOException.
			 * 
			 * If it fails to detect interruption, the caller should deal with
			 * this situation.
			 * 
			 * Maybe I should use the TransferManager.
			 */
			fis = new FileInputStream(src);
			ObjectMetadata metadatas = new ObjectMetadata();
			metadatas.setContentLength(src.length());
			getS3().upload(getBN(), fis, destination, metadatas, pm);
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() != null
					&& Ex.getMessage().indexOf("Forbidden") != -1) {
				throw new WrapperAccessDeniedException(destination, Ex);
			} else {
				throw new IOException(Msg.bind(Messages.S3fsEx_PUT, source,
						destination), Ex);
			}
		} catch (AmazonClientException Ex) {
			if (containsInterruptedException(Ex)) {
				throw new InterruptedIOException(
						Messages.S3fsEx_PUT_INTERRUPTED);
			}
			throw new IOException(Msg.bind(Messages.S3fsEx_PUT, source,
					destination), Ex);
		} catch (FileNotFoundException Ex) {
			String msg = Ex.getMessage();
			if (msg != null && msg.indexOf(" (Permission denied)") != -1) {
				throw new WrapperAccessDeniedException(source);
			} else if (msg != null
					&& msg.indexOf(" (No such file or directory)") != -1) {
				throw new WrapperNoSuchFileException(source);
			} else if (msg != null && msg.indexOf(" (Is a directory)") != -1) {
				throw new WrapperDirectoryNotEmptyException(source);
			} else {
				throw new WrapperNoSuchFileException(source, Ex);
			}
		} catch (Throwable Ex) {
			System.out.println(Ex);
		} finally {
			if (fis != null)
				fis.close();
		}
	}

}