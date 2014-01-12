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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.cloud.aws.s3.Messages;
import com.wat.cloud.aws.s3.exception.DeleteKeyException;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.HiddenException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ex.WrapperInterruptedIOException;
import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.SymbolicLinkNotSupported;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperDirectoryNotEmptyException;
import com.wat.melody.common.files.exception.WrapperFileAlreadyExistsException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.files.exception.WrapperNotDirectoryException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsS3FileSystem implements FileSystem {

	protected static boolean containsInterruptedException(Throwable Ex) {
		/*
		 * if interrupted: may throw an InterruptedException wrapped in a
		 * TransportException wrapped in a AmazonClientException.
		 * 
		 * if interrupted: may also throw an InterruptedIOException wrapped in a
		 * AmazonClientException.
		 */
		while (Ex != null) {
			if (Ex instanceof InterruptedException) {
				return true;
			}
			if (Ex instanceof InterruptedIOException) {
				return true;
			}
			Ex = Ex.getCause();
		}
		return false;
	}

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

	private AwsS3Wrapper _s3Connection = null;
	private BucketName _bucketName = null;

	public AwsS3FileSystem(AmazonS3 s3Connection, BucketName bucketName) {
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

	@Override
	public boolean exists(String path, LinkOption... options) {
		try {
			return readAttributes(path) != null;
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public boolean isDirectory(Path path, LinkOption... options) {
		return isDirectory(convertToS3Path(path), options);
	}

	@Override
	public boolean isDirectory(String path, LinkOption... options) {
		try {
			return readAttributes(path).isDirectory();
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public boolean isRegularFile(Path path, LinkOption... options) {
		return isRegularFile(convertToS3Path(path), options);
	}

	@Override
	public boolean isRegularFile(String path, LinkOption... options) {
		try {
			EnhancedFileAttributes attrs = readAttributes(path);
			return !attrs.isDirectory() && !attrs.isSymbolicLink();
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public boolean isSymbolicLink(Path path) {
		return isSymbolicLink(convertToS3Path(path));
	}

	@Override
	public boolean isSymbolicLink(String path) {
		try {
			return readAttributes(path).isSymbolicLink();
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
	 */
	@Override
	public void createDirectory(String dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		if (dir == null || dir.trim().length() == 0) {
			throw new IllegalArgumentException(dir + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}

		/*
		 * a trailing '/' will make the call readAttributes faster if the
		 * directory exist.
		 */
		String path = dir;
		if (dir.charAt(dir.length() - 1) != '/') {
			path += '/';
		}
		try {
			if (!readAttributes(path).isDirectory()) {
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

		createDirectory0(path, attrs);
	}

	/**
	 * unlike {@link #createDirectory(String, FileAttribute...)}, this method
	 * don't perform any verification on the parent.
	 * 
	 * @param dir
	 * @param attrs
	 * 
	 * @throws IOException
	 * @throws IllegalFileAttributeException
	 * @throws AccessDeniedException
	 */
	private void createDirectory0(String dir, FileAttribute<?>... attrs)
			throws IOException, IllegalFileAttributeException,
			AccessDeniedException {
		// create the directory
		ObjectMetadata metadatas = new ObjectMetadata();
		Map<String, String> userMetadatas = new HashMap<String, String>();
		userMetadatas.put(AwsS3FileAttributes.DIRECTORY_FLAG, "");
		metadatas.setUserMetadata(userMetadatas);

		InputStream is = new ByteArrayInputStream(new byte[0]);
		try {
			getS3().putObject(getBN(), dir, is, metadatas);
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() != null
					&& Ex.getMessage().indexOf("Forbidden") != -1) {
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
		String unixDir = convertToS3Path(dir) + "/";
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
			// optim: createDirectory0 don't perform any verification.
			createDirectory0(unixDir, attrs);
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
		// link not supported
		throw new SymbolicLinkNotSupported(link, target,
				"AWS S3 doesn't support Symbolic links.");
	}

	@Override
	public void createSymbolicLink(String link, String target,
			FileAttribute<?>... attrs) throws IOException,
			SymbolicLinkNotSupported, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		// link not supported
		throw new SymbolicLinkNotSupported(link, target,
				"AWS S3 doesn't support Symbolic links.");
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		// link not supported
		return null;
	}

	@Override
	public Path readSymbolicLink(String link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		// link not supported
		return null;
	}

	@Override
	public void delete(Path path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		delete(convertToS3Path(path));
	}

	@Override
	public void delete(String path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		EnhancedFileAttributes attrs = readAttributes(path);
		String sPath = path;
		if (attrs.isDirectory()) {
			// if directory, a trailing '/' must be added
			if (path.charAt(path.length() - 1) != '/') {
				sPath = path + '/';
			}
			ObjectListing content = null;
			try {
				content = getS3().listDirectory(getBN(), sPath);
			} catch (AmazonS3Exception Ex) {
				if (Ex.getMessage() != null
						&& Ex.getMessage().indexOf("Forbidden") != -1) {
					throw new WrapperAccessDeniedException(path, Ex);
				} else {
					throw new IOException(Msg.bind(Messages.S3fsEx_LS, path),
							Ex);
				}
			} catch (AmazonClientException Ex) {
				throw new IOException(Msg.bind(Messages.S3fsEx_LS, path), Ex);
			}
			if (content.getObjectSummaries().size()
					+ content.getCommonPrefixes().size() > 0) {
				throw new WrapperDirectoryNotEmptyException(path);
			}
		} else if (attrs.isRegularFile()) {
			// if not directory, remove the trailing '/'
			if (path.charAt(path.length() - 1) == '/') {
				sPath = path.substring(0, path.length() - 1);
			}
		}
		try {
			getS3().deleteObject(getBN(), sPath);
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() != null
					&& Ex.getMessage().indexOf("Forbidden") != -1) {
				throw new WrapperAccessDeniedException(path, Ex);
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

	@Override
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

	@Override
	public void deleteDirectory(String dir) throws IOException,
			NotDirectoryException, AccessDeniedException {
		String path = dir;
		// if directory, a trailing '/' make readAttributes faster
		if (dir.charAt(dir.length() - 1) != '/') {
			path = dir + '/';
		}
		try {
			if (!readAttributes(path).isDirectory()) {
				throw new WrapperNotDirectoryException(dir);
			}
		} catch (NoSuchFileException ignored) {
			// specifications says 'don't raise error'
			return;
		}
		try {
			getS3().removeAllKeysInVersionningDisabledBucket(getBN(), path);
		} catch (DeleteKeyException Ex) {
			throw new IOException(null, Ex);
		} catch (InterruptedException Ex) {
			throw new WrapperInterruptedIOException(Msg.bind(
					Messages.S3fsEx_RMDIR_INTERRUPTED, dir), Ex);
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() != null
					&& Ex.getMessage().indexOf("Forbidden") != -1) {
				throw new WrapperAccessDeniedException(dir, Ex);
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

	@Override
	public DirectoryStream<Path> newDirectoryStream(final String path)
			throws IOException, InterruptedIOException, NoSuchFileException,
			NotDirectoryException, AccessDeniedException {
		String dir = path;
		// if directory, a trailing '/' make readAttributes faster
		if (path.charAt(path.length() - 1) != '/') {
			dir = path + '/';
		}
		// will throw NoSuchFile
		if (!readAttributes(dir).isDirectory()) {
			throw new WrapperNotDirectoryException(path);
		}

		final ObjectListing listing;
		try {
			listing = getS3().listDirectory(getBN(), dir);
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() != null
					&& Ex.getMessage().indexOf("Forbidden") != -1) {
				throw new WrapperAccessDeniedException(dir, Ex);
			} else {
				throw new IOException(Msg.bind(Messages.S3fsEx_LS, path), Ex);
			}
		} catch (AmazonClientException Ex) {
			throw new IOException(Msg.bind(Messages.S3fsEx_LS, path), Ex);
		}

		final Iterator<Path> content = new Iterator<Path>() {

			int pos = 0;
			ObjectListing internal = listing;

			@Override
			public boolean hasNext() {
				return internal.isTruncated()
						|| pos < (internal.getCommonPrefixes().size() + internal
								.getObjectSummaries().size());
			}

			@Override
			public Path next() {
				if (pos < internal.getCommonPrefixes().size()) {
					String prefix = path.charAt(0) == '/' ? "/" : "";
					return Paths.get(prefix
							+ internal.getCommonPrefixes().get(pos++));
				} else if (pos < (internal.getCommonPrefixes().size() + internal
						.getObjectSummaries().size())) {
					String prefix = path.charAt(0) == '/' ? "/" : "";
					return Paths.get(prefix
							+ internal
									.getObjectSummaries()
									.get(pos++
											- internal.getCommonPrefixes()
													.size()).getKey());
				} else if (internal.isTruncated()) {
					pos = 0;
					// no need to wrap amazon exception
					internal = getS3().getS3().listNextBatchOfObjects(internal);
					return next();
				} else
					throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new RuntimeException("remove: not supported.");
			}
		};

		return new DirectoryStream<Path>() {

			@Override
			public void close() throws IOException {
				// nothing to do
			}

			@Override
			public Iterator<Path> iterator() {
				return content;
			}

		};
	}

	@Override
	public EnhancedFileAttributes readAttributes(Path path) throws IOException,
			NoSuchFileException, AccessDeniedException {
		return readAttributes(convertToS3Path(path));
	}

	@Override
	public AwsS3FileAttributes readAttributes(String path) throws IOException,
			NoSuchFileException, AccessDeniedException {
		return readAttributes0(true, path);
	}

	private AwsS3FileAttributes readAttributes0(boolean readAlternateName,
			String path) throws IOException, NoSuchFileException,
			AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			ObjectMetadata metadatas = null;
			metadatas = getS3().getObjectMetadata(getBN(), path);
			return new AwsS3FileAttributes(metadatas, null);
		} catch (AmazonS3Exception Ex) {
			if (Ex.getMessage() == null) {
				throw new IOException(Msg.bind(Messages.S3fsEx_STAT, path), Ex);
			} else if (Ex.getMessage().indexOf("Forbidden") != -1) {
				// Means that the given key cannot be accessed with the given
				// credentials
				// Means that the given key cannot be found
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
					throw new WrapperAccessDeniedException(path, Ex);
				}
				String alternate = null;
				if (path.charAt(path.length() - 1) == '/') {
					alternate = path.substring(0, path.length() - 1);
				} else {
					alternate = path + '/';
				}
				try {
					return readAttributes0(false, alternate);
				} catch (AccessDeniedException sub) {
					throw new WrapperAccessDeniedException(path, Ex);
				} catch (NoSuchFileException sub) {
					throw new WrapperAccessDeniedException(path, Ex);
				}
			} else if (Ex.getMessage().indexOf("Not Found") != -1) {
				// Means that the given key cannot be found
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
				String alternate = null;
				if (path.charAt(path.length() - 1) == '/') {
					alternate = path.substring(0, path.length() - 1);
				} else {
					alternate = path + '/';
				}
				try {
					return readAttributes0(false, alternate);
				} catch (AccessDeniedException sub) {
					throw new WrapperNoSuchFileException(path, Ex);
				} catch (NoSuchFileException sub) {
					throw new WrapperNoSuchFileException(path, Ex);
				}
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

	protected static List<String> SupportedMetadatas = new ArrayList<String>();
	static {
		SupportedMetadatas.add(Headers.CACHE_CONTROL.toLowerCase());
		SupportedMetadatas.add(Headers.CONTENT_DISPOSITION.toLowerCase());
		SupportedMetadatas.add(Headers.CONTENT_ENCODING.toLowerCase());
		SupportedMetadatas.add(Headers.CONTENT_TYPE.toLowerCase());
		SupportedMetadatas.add(Headers.STORAGE_CLASS.toLowerCase());
		SupportedMetadatas.add(Headers.SERVER_SIDE_ENCRYPTION.toLowerCase());
	}

	/**
	 * An attribute with an <tt>null</tt> value will be removed.
	 */
	@Override
	public void setAttributes(String path, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			IllegalFileAttributeException, AccessDeniedException {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		if (attrs == null || attrs.length == 0) {
			return;
		}

		AwsS3FileAttributes attribute = readAttributes(path);
		ObjectMetadata origMD = attribute.getMetadatas(false);
		Map<String, String> userMD = origMD.getUserMetadata();
		/*
		 * BUG in ObjectMetadata : most values can't be set to null. And
		 * getRawMetadata() returns an unmodifiableMap... There's no standard
		 * way to remove a meta-data.
		 * 
		 * Our solution to remove a meta-data from the raw meta data is to build
		 * our own meta-data from scratch. That's why we copy the raw meta data
		 * into a modifiable map. Later, we will modify (add/remove keys) this
		 * modifiable map. Finally, we will create our own meta data using the
		 * modifiable map and the user meta data,
		 */
		Map<String, Object> rawMD = new HashMap<String, Object>(
				origMD.getRawMetadata());

		boolean needUpdate = false;

		ConsolidatedException full = new ConsolidatedException(Msg.bind(
				Messages.S3fsEx_FAILED_TO_SET_ATTRIBUTES, path));
		for (FileAttribute<?> attr : attrs) {
			if (attr == null) {
				continue;
			}
			try {
				/*
				 * SPECS : (not really applicable to AWS S3)
				 * 
				 * will throw NullPointerException if attr.name() is null.
				 * 
				 * will throw UnsupportedOperationException if attr.name()
				 * denotes an unrecognized view.
				 * 
				 * will throw IllegalArgumentException if attr.name() denotes a
				 * know view but an unrecognized attribute.
				 * 
				 * will throw NullPointerException if attr.value() is null.
				 * 
				 * will throw ClassCastException if attr.name() denotes a known
				 * view and a known but attr.value() is not of the correct type.
				 * 
				 * will throw IllegalArgumentException if attr.name() denotes a
				 * known view and a known and attr.value() is of the correct
				 * type but as an invalid value.
				 * 
				 * will throw IOException the operation failed (access denied,
				 * no such file, permissions on symbolic link...).
				 */
				/*
				 * TODO : support ACL ?
				 */
				if (attr.name().indexOf(":") != -1) {
					// view is not recognized
					throw new UnsupportedOperationException("View '"
							+ attr.name() + "'" + " is not availbale.");
				} else if (!attr.name().matches("^[a-zA-Z0-9-_;\\^#&]+$")) {
					// view is known but attribute name is not recognized
					throw new IllegalArgumentException("'" + attr.name() + "'"
							+ " not recognized.");
				} else if (SupportedMetadatas.contains(attr.name()
						.toLowerCase())) {
					if (attr.value() == null) {
						if (rawMD.get(attr.name()) != null) {
							// the attribute will be removed
							rawMD.remove(attr.name());
							needUpdate = true;
						}
					} else if (!attr.value().equals(rawMD.get(attr.name()))) {
						// the attribute will be added
						rawMD.put(attr.name(), (String) attr.value());
						needUpdate = true;
					}
				} else if (attr.value() == null) {
					if (userMD.get(attr.name()) != null) {
						// the attribute will be removed
						userMD.remove(attr.name());
						needUpdate = true;
					}
				} else if (!attr.value().equals(userMD.get(attr.name()))) {
					// the attribute will be added
					userMD.put(attr.name(), (String) attr.value());
					needUpdate = true;
				}

			} catch (UnsupportedOperationException | IllegalArgumentException
					| ClassCastException Ex) {
				// don't want the stack trace
				full.addCause(new MelodyException(Msg.bind(
						Messages.S3fsEx_FAILED_TO_SET_ATTRIBUTE, attr, Ex),
						new HiddenException(Ex)));
			} catch (Throwable Ex) {
				// want the stack trace
				full.addCause(new MelodyException(Msg.bind(
						Messages.S3fsEx_FAILED_TO_SET_ATTRIBUTE_X, attr), Ex));
			}
		}

		if (needUpdate == true) {
			// BUG : build our own meta-data from scratch
			ObjectMetadata finalMD = new ObjectMetadata();
			for (String header : rawMD.keySet()) {
				finalMD.setHeader(header, rawMD.get(header));
			}
			finalMD.setUserMetadata(userMD);

			String sPath = path;
			if (attribute.isDirectory()
					&& path.charAt(path.length() - 1) != '/') {
				// if directory, add a trailing '/'
				sPath += '/';
			} else if (attribute.isRegularFile()
					&& path.charAt(path.length() - 1) == '/') {
				// if regular file, remove the trailing '/'
				sPath = path.substring(0, path.length() - 1);
			}

			try {
				getS3().setObjectMetadata(getBN(), sPath, finalMD);
			} catch (AmazonS3Exception Ex) {
				if (Ex.getMessage() != null
						&& Ex.getMessage().indexOf("Forbidden") != -1) {
					throw new WrapperAccessDeniedException(path, Ex);
				} else {
					throw new IOException(Msg.bind(Messages.S3fsEx_SETATTRS,
							path, rawMD, userMD), Ex);
				}
			} catch (AmazonClientException Ex) {
				throw new IOException(Msg.bind(Messages.S3fsEx_SETATTRS, path,
						rawMD, userMD), Ex);
			}
		}

		if (full.countCauses() != 0) {
			throw new IllegalFileAttributeException(full);
		}
	}

	/**
	 * 
	 * @param path
	 * @throws IOException
	 * @throws NoSuchFileException
	 *             if the given parent element doesn't exists or exists but is
	 *             not a directory.
	 * @throws AccessDeniedException
	 */
	protected void failIfParentDirectoryInvalid(String path)
			throws IOException, NoSuchFileException, AccessDeniedException {
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