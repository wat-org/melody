package com.wat.cloud.aws.s3.transfer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.cloud.aws.s3.Messages;
import com.wat.cloud.aws.s3.exception.DeleteKeyException;
import com.wat.melody.common.ex.WrapperInterruptedIOException;
import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.SymbolicLinkNotSupported;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperFileAlreadyExistsException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.files.exception.WrapperNotDirectoryException;
import com.wat.melody.common.messages.Msg;

/**
 * <p>
 * /!\ Work In progress
 * 
 * TODO : need to resolve path before each operation
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsS3FileSystemWithSymbolicLinkSupport implements FileSystem {

	public static String convertToS3Path(String path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		return path.replaceAll("\\\\", "/");
	}

	public static String convertToS3Path(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		return convertToS3Path(path.toString());
	}

	/**
	 * Returns {@code false} if NOFOLLOW_LINKS is present in the given list.
	 */
	private static boolean followLinks(LinkOption... options) {
		boolean followLinks = true;
		for (LinkOption opt : options) {
			if (opt == LinkOption.NOFOLLOW_LINKS) {
				followLinks = false;
				continue;
			}
			if (opt == null) {
				throw new NullPointerException();
			}
			throw new AssertionError("Should not get here");
		}
		return followLinks;
	}

	private AwsS3Wrapper _s3Connection = null;
	private BucketName _bucketName = null;

	public AwsS3FileSystemWithSymbolicLinkSupport(AmazonS3 s3Connection,
			BucketName bucketName) {
		setS3(new AwsS3Wrapper(s3Connection));
		setBucketName(bucketName);
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

	@Override
	public void release() {
		// nothing to do
	}

	@Override
	public boolean exists(Path path, LinkOption... options) {
		return exists(convertToS3Path(path), options);
	}

	public boolean exists(String path, LinkOption... options) {
		try {
			return readAttributes0(path, options) != null;
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public boolean isDirectory(Path path, LinkOption... options) {
		return isDirectory(convertToS3Path(path), options);
	}

	public boolean isDirectory(String path, LinkOption... options) {
		try {
			return AwsS3FileAttributes.isDir(readAttributes0(path, options));
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public boolean isRegularFile(Path path, LinkOption... options) {
		return isRegularFile(convertToS3Path(path), options);
	}

	public boolean isRegularFile(String path, LinkOption... options) {
		try {
			ObjectMetadata metadatas = readAttributes0(path, options);
			return !AwsS3FileAttributes.isDir(metadatas)
					&& !AwsS3FileAttributes.isLink(metadatas);
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public boolean isSymbolicLink(Path path) {
		return isSymbolicLink(convertToS3Path(path));
	}

	public boolean isSymbolicLink(String path) {
		try {
			return AwsS3FileAttributes.isLink(readAttributes0(path,
					LinkOption.NOFOLLOW_LINKS));
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		createDirectory(convertToS3Path(dir), attrs);
	}

	/**
	 * A key with a trailing '/' denotes a directory in S3. This method will add
	 * a trailing '/' to the given string.
	 * 
	 * @param dir
	 * @param attrs
	 * @throws IOException
	 * @throws NoSuchFileException
	 * @throws FileAlreadyExistsException
	 * @throws IllegalFileAttributeException
	 * @throws AccessDeniedException
	 */
	public void createDirectory(String dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		if (dir == null || dir.trim().length() == 0) {
			throw new IllegalArgumentException(dir + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}

		try {
			/*
			 * a trailing '/' will make the call readAttributes0 faster if the
			 * directory exist.
			 */
			if (dir.charAt(dir.length() - 1) != '/') {
				dir += '/';
			}
			if (!AwsS3FileAttributes.isDir(readAttributes0(dir,
					LinkOption.NOFOLLOW_LINKS))) {
				// a file exists, but it's not a directory => raise error
				throw new WrapperFileAlreadyExistsException(dir);
			}
			// directory already exists => set attributes and exit
			setAttributes(dir, attrs);
			return;
		} catch (NoSuchFileException ignored) {
			// no file exists => create it
		}
		// verify if parent directory is valid. If not, raise error
		failIfParentDirectoryInvalid(dir);

		// create the directory
		ObjectMetadata metadatas = new ObjectMetadata();
		Map<String, String> userMetadatas = new HashMap<String, String>();
		userMetadatas.put(AwsS3FileAttributes.DIRECTORY_FLAG, "");
		metadatas.setUserMetadata(userMetadatas);

		InputStream is = new ByteArrayInputStream(new byte[0]);
		try {
			getS3().putObject(getBN(), dir, is, metadatas);
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() == null) {
				throw new IOException(Msg.bind(Messages.S3fsEx_MKDIR, dir), Ex);
			} else if (Ex.getMessage().indexOf("Forbidden") != -1) {
				throw new WrapperAccessDeniedException(dir, Ex);
			} else {
				throw new IOException(Msg.bind(Messages.S3fsEx_MKDIR, dir), Ex);
			}
		} catch (AmazonClientException Ex) {
			throw new IOException(Msg.bind(Messages.S3fsEx_MKDIR, dir), Ex);
		} finally {
			if (is != null) {
				is.close();
				is = null;
			}
		}

		setAttributes(dir, attrs);
	}

	@Override
	public void createDirectories(Path dir, FileAttribute<?>... attrs)
			throws IOException, IllegalFileAttributeException,
			FileAlreadyExistsException, AccessDeniedException {
		if (dir == null || dir.toString().length() == 0
				|| dir.getNameCount() < 1) {
			return;
		}
		String unixDir = convertToS3Path(dir);
		try {
			createDirectory(unixDir, attrs);
			return;
		} catch (NoSuchFileException Ex) {
			// if the top first dir cannot be created => raise an error
			if (dir.getNameCount() <= 1) {
				throw Ex;
			}
		} catch (FileAlreadyExistsException Ex) {
			// if the file is a link on a dir => no error
			try {
				if (readAttributes(unixDir).isDirectory()) {
					return;
				}
			} catch (NoSuchFileException Exx) {
				// concurrency pb : should recreate ?
			}
			throw Ex;
		} catch (IOException Ex) {
			throw Ex;
		}
		// if dir cannot be created => create its parent
		Path parent = null;
		parent = dir.resolve("..").normalize();
		createDirectories(parent, attrs);
		try {
			createDirectory(unixDir, attrs);
		} catch (FileAlreadyExistsException Ex) {
			// if the file is a link on a dir => no error
			try {
				if (readAttributes(unixDir).isDirectory()) {
					return;
				}
			} catch (NoSuchFileException Exx) {
				// concurrency pb : should recreate ?
			}
			throw Ex;
		}
	}

	@Override
	public void createDirectories(String dir, FileAttribute<?>... attrs)
			throws IOException, IllegalFileAttributeException,
			FileAlreadyExistsException, AccessDeniedException {
		createDirectories(Paths.get(dir));
	}

	@Override
	public void createSymbolicLink(Path link, Path target,
			FileAttribute<?>... attrs) throws IOException,
			SymbolicLinkNotSupported, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		createSymbolicLink(convertToS3Path(link), convertToS3Path(target),
				attrs);
	}

	public void createSymbolicLink(String link, String target,
			FileAttribute<?>... attrs) throws IOException,
			SymbolicLinkNotSupported, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		if (link == null || link.trim().length() == 0) {
			throw new IllegalArgumentException(link + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (target == null || target.trim().length() == 0) {
			throw new IllegalArgumentException(target + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}

		try {
			if (!AwsS3FileAttributes.isLink(readAttributes0(link,
					LinkOption.NOFOLLOW_LINKS))) {
				// a file exists, but it's not a link => raise error
				throw new WrapperFileAlreadyExistsException(link);
			}
		} catch (NoSuchFileException ignored) {
		}
		failIfParentDirectoryInvalid(link);

		ObjectMetadata metadatas = new ObjectMetadata();
		Map<String, String> userMetadatas = new HashMap<String, String>();
		userMetadatas.put(AwsS3FileAttributes.SYMBOLIC_LINK_FLAG, target);
		metadatas.setUserMetadata(userMetadatas);

		InputStream is = new ByteArrayInputStream(new byte[0]);
		try {
			getS3().putObject(getBN(), link, is, metadatas);
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() == null) {
				throw new IOException(
						Msg.bind(Messages.S3fsEx_LN, target, link), Ex);
			} else if (Ex.getMessage().indexOf("Forbidden") != -1) {
				throw new WrapperAccessDeniedException(link, Ex);
			} else {
				throw new IOException(
						Msg.bind(Messages.S3fsEx_LN, target, link), Ex);
			}
		} catch (AmazonClientException Ex) {
			throw new IOException(Msg.bind(Messages.S3fsEx_LN, target, link),
					Ex);
		} finally {
			if (is != null) {
				is.close();
				is = null;
			}
		}

		setAttributes(link, attrs);
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		return readSymbolicLink(convertToS3Path(link));
	}

	public Path readSymbolicLink(String link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		ObjectMetadata metadatas = readAttributes0(link,
				LinkOption.NOFOLLOW_LINKS);
		return readSymbolicLink(link, metadatas);
	}

	/**
	 * For internal usage only. Save the readAttributes0 call.
	 * 
	 * @param metadatas
	 *            the metadatas of the given link.
	 */
	private Path readSymbolicLink(String link, ObjectMetadata metadatas)
			throws IOException, NoSuchFileException, NotLinkException,
			AccessDeniedException {
		String target = readSymbolicLink0(link, metadatas);
		while (true) {
			try {
				metadatas = readAttributes0(target, LinkOption.NOFOLLOW_LINKS);
				target = readSymbolicLink0(target, metadatas);
			} catch (NoSuchFileException | NotLinkException EndLoop) {
				break;
			}
		}
		return Paths.get(target);
	}

	private String readSymbolicLink0(String link, ObjectMetadata metadatas)
			throws NotLinkException {
		if (link == null || link.trim().length() == 0) {
			throw new IllegalArgumentException(link + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (metadatas == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ObjectMetadata.class.getCanonicalName() + ".");
		}
		if (!AwsS3FileAttributes.isLink(metadatas)) {
			throw new NotLinkException(link);
		}
		return AwsS3FileAttributes.readLink(metadatas);
	}

	@Override
	public void delete(Path path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		delete(convertToS3Path(path));
	}

	public void delete(String path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		ObjectMetadata metadatas = readAttributes0(path,
				LinkOption.NOFOLLOW_LINKS);
		if (AwsS3FileAttributes.isDir(metadatas)
				&& getS3().listDirectory(getBN(), path).getObjectSummaries()
						.size() == 0) {
			throw new DirectoryNotEmptyException(path);
		}
		try {
			getS3().deleteObject(getBN(), path);
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() == null) {
				throw new IOException(Msg.bind(Messages.S3fsEx_RM, path), Ex);
			} else if (Ex.getMessage().indexOf("Forbidden") != -1) {
				throw new WrapperAccessDeniedException(path, Ex);
			} else if (Ex.getMessage().indexOf("Not Found") != -1) {
				throw new WrapperNoSuchFileException(path, Ex);
			} else {
				throw new IOException(Msg.bind(Messages.S3fsEx_RM, path), Ex);
			}
		} catch (AmazonClientException Ex) {
			throw new IOException(Msg.bind(Messages.S3fsEx_RM, path), Ex);
		}
	}

	@Override
	public boolean deleteIfExists(Path path) throws IOException,
			DirectoryNotEmptyException, AccessDeniedException {
		return deleteIfExists(convertToS3Path(path));
	}

	public boolean deleteIfExists(String path) throws IOException,
			DirectoryNotEmptyException, AccessDeniedException {
		try {
			delete(path);
		} catch (NoSuchFileException Ex) {
			return false;
		}
		return true;
	}

	@Override
	public void deleteDirectory(Path dir) throws IOException,
			NotDirectoryException, AccessDeniedException {
		deleteDirectory(convertToS3Path(dir));
	}

	/**
	 * /!\ Work In progress
	 */
	public void deleteDirectory(String dir) throws IOException,
			NotDirectoryException, AccessDeniedException,
			InterruptedIOException {
		if (!AwsS3FileAttributes.isDir(readAttributes0(dir,
				LinkOption.NOFOLLOW_LINKS))) {
			throw new WrapperNotDirectoryException(dir);
		}
		try {
			getS3().removeAllKeysInVersionningDisabledBucket(getBN(), dir);
		} catch (DeleteKeyException Ex) {
			throw new IOException(null, Ex);
		} catch (InterruptedException Ex) {
			throw new WrapperInterruptedIOException(Msg.bind(
					Messages.S3fsEx_RMDIR_INTERRUPTED, dir), Ex);
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() == null) {
				throw new IOException(Msg.bind(Messages.S3fsEx_RMDIR, dir), Ex);
			} else if (Ex.getMessage().indexOf("Forbidden") != -1) {
				throw new WrapperAccessDeniedException(dir, Ex);
			} else if (Ex.getMessage().indexOf("Not Found") != -1) {
				throw new WrapperNoSuchFileException(dir, Ex);
			} else {
				throw new IOException(Msg.bind(Messages.S3fsEx_RMDIR, dir), Ex);
			}
		} catch (AmazonClientException Ex) {
			throw new IOException(Msg.bind(Messages.S3fsEx_RMDIR, dir), Ex);
		}
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path path)
			throws IOException, InterruptedIOException, NoSuchFileException,
			NotDirectoryException, AccessDeniedException {
		return newDirectoryStream(convertToS3Path(path));
	}

	/**
	 * /!\ Work In progress
	 */
	public DirectoryStream<Path> newDirectoryStream(String path)
			throws IOException, InterruptedIOException, NoSuchFileException,
			NotDirectoryException, AccessDeniedException {
		return null;
	}

	@Override
	public EnhancedFileAttributes readAttributes(Path path) throws IOException,
			NoSuchFileException, AccessDeniedException {
		return readAttributes(convertToS3Path(path));
	}

	public EnhancedFileAttributes readAttributes(String path)
			throws IOException, NoSuchFileException, AccessDeniedException {
		ObjectMetadata metadatas = readAttributes0(path,
				LinkOption.NOFOLLOW_LINKS);
		ObjectMetadata targetMetadatas = null;
		if (AwsS3FileAttributes.isLink(metadatas)) {
			Path target = readSymbolicLink(path, metadatas);
			try {
				targetMetadatas = readAttributes0(convertToS3Path(target),
						LinkOption.NOFOLLOW_LINKS);
			} catch (NoSuchFileException ignored) {
			}
		}
		return new AwsS3FileAttributes(metadatas, targetMetadatas);
	}

	private ObjectMetadata readAttributes0(String path, LinkOption... options)
			throws IOException, NoSuchFileException, AccessDeniedException {
		return readAttributes0(true, path, options);
	}

	private ObjectMetadata readAttributes0(boolean readAlternateName,
			String path, LinkOption... options) throws IOException,
			NoSuchFileException, AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		boolean followlink = followLinks(options);
		try {
			ObjectMetadata metadatas = getS3().getObjectMetadata(getBN(), path);
			if (followlink && AwsS3FileAttributes.isLink(metadatas)) {
				Path target = readSymbolicLink(path, metadatas);
				metadatas = readAttributes0(convertToS3Path(target),
						LinkOption.NOFOLLOW_LINKS);
			}
			return metadatas;
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() == null) {
				throw new IOException(Msg.bind(Messages.S3fsEx_STAT, path), Ex);
			} else if (Ex.getMessage().indexOf("Forbidden") != -1) {
				// Means that the given key cannot be accessed with the given
				// credentials
				throw new WrapperAccessDeniedException(path, Ex);
			} else if (Ex.getMessage().indexOf("Not Found") != -1) {
				// Means that the given key doesn't exists in the given bucket
				/*
				 * A key with a trailing '/' denotes a directory in S3. We must
				 * test the existence of a directory.
				 * 
				 * If the given path ends with a '/', we must test the existence
				 * of a file with no '/'.
				 * 
				 * If the given path not ends with a '/', we must test the
				 * existence of a directory with a '/'.
				 */
				if (readAlternateName == false) {
					throw new WrapperNoSuchFileException(path, Ex);
				}
				if (path.charAt(path.length() - 1) == '/') {
					path = path.substring(0, path.length() - 1);
				} else {
					path += '/';
				}
				return readAttributes0(false, path, options);
			} else {
				throw new IOException(Msg.bind(Messages.S3fsEx_STAT, path), Ex);
			}
		} catch (AmazonClientException Ex) {
			throw new IOException(Msg.bind(Messages.S3fsEx_STAT, path), Ex);
		}
	}

	@Override
	public void setAttributes(Path path, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			IllegalFileAttributeException, AccessDeniedException {
		setAttributes(convertToS3Path(path), attrs);
	}

	/**
	 * /!\ Work In progress
	 */
	public void setAttributes(String path, FileAttribute<?>... attrrs)
			throws IOException, NoSuchFileException,
			IllegalFileAttributeException, AccessDeniedException {
	}

	/**
	 * 
	 * @param path
	 * @throws IOException
	 * @throws NoSuchFileException
	 *             if the given parent element doesn't exists or exists but is
	 *             not a directory or a directory link.
	 * @throws AccessDeniedException
	 */
	private void failIfParentDirectoryInvalid(String path) throws IOException,
			NoSuchFileException, AccessDeniedException {
		Path parent = Paths.get(path).getParent();
		if (parent == null || parent.getNameCount() == 0) {
			// root element reached : always valid
			return;
		}
		EnhancedFileAttributes attrs = null;
		try {
			/*
			 * a trailing '/' will make the call readAttributes faster if the
			 * directory exist.
			 */
			attrs = readAttributes(convertToS3Path(parent) + '/');
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(path, Ex);
		}
		if (!attrs.isDirectory()) {
			throw new WrapperNoSuchFileException(path);
		}
	}

}