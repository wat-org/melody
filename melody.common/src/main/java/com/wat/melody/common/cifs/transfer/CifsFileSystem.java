package com.wat.melody.common.cifs.transfer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jcifs.dcerpc.DcerpcException;
import jcifs.smb.NtStatus;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.util.transport.TransportException;

import com.wat.melody.common.cifs.transfer.exception.WrapperNoSuchShareException;
import com.wat.melody.common.cifs.transfer.exception.WrapperSmbException;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.MelodyException;
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
import com.wat.melody.common.transfer.resources.attributes.AttributeDosArchive;
import com.wat.melody.common.transfer.resources.attributes.AttributeDosHidden;
import com.wat.melody.common.transfer.resources.attributes.AttributeDosReadOnly;
import com.wat.melody.common.transfer.resources.attributes.AttributeDosSystem;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class CifsFileSystem implements FileSystem {

	protected static boolean containsInterruptedException(Throwable Ex) {
		/*
		 * if interrupted: may throw an InterruptedException wrapped in a
		 * TransportException wrapped in a SmbException.
		 * 
		 * if interrupted: may also throw an InterruptedIOException wrapped in a
		 * SmbException.
		 */
		while (Ex != null) {
			if (Ex instanceof InterruptedException) {
				return true;
			}
			if (Ex instanceof InterruptedIOException) {
				return true;
			}
			/*
			 * CIFS exception are a BIG SHIT. They have a custom rootCause which
			 * is not standard ...
			 */
			if (Ex instanceof SmbException) {
				Ex = ((SmbException) Ex).getRootCause();
			} else if (Ex instanceof TransportException) {
				Ex = ((TransportException) Ex).getRootCause();
			} else if (Ex instanceof DcerpcException) {
				Ex = ((DcerpcException) Ex).getRootCause();
			} else {
				Ex = Ex.getCause();
			}
		}
		return false;
	}

	public static String convertToUnixPath(String path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		return path.replaceAll("\\\\", "/");
	}

	public static String convertToUnixPath(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		return convertToUnixPath(path.toString());
	}

	/**
	 * @param smbLocation
	 * @param path
	 * @param smbCredential
	 * @param options
	 *            is not used (because Samba link support can only be configured
	 *            on the samba server side, client options have no
	 *            signification).
	 * 
	 * @return an {@link SmbFile}, build from this object Samba Share + the
	 *         given {@link path}.
	 */
	protected static SmbFile createSmbFile(String smbLocation, String path,
			NtlmPasswordAuthentication smbCredential, LinkOption... options) {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		String smbPath = smbLocation;
		if (path.charAt(0) == '/') {
			smbPath += path.substring(1);
		} else {
			smbPath += path;
		}
		try {
			return new SmbFile(smbPath, smbCredential);
		} catch (MalformedURLException Ex) {
			throw new RuntimeException(smbPath + ": Malformed URL.", Ex);
		}
	}

	private NtlmPasswordAuthentication _smbCredential;
	private String _smbLocation;

	public CifsFileSystem(String location, String domain, String user,
			String password) {
		if (location == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		// if domain, user or pass is null, default values will be used
		_smbCredential = new NtlmPasswordAuthentication(domain, user, password);
		_smbLocation = "smb://" + location + "/";
	}

	protected SmbFile createSmbFile(String path, LinkOption... options) {
		return createSmbFile(getLocation(), path, getCredential(), options);
	}

	protected String getLocation() {
		return _smbLocation;
	}

	protected NtlmPasswordAuthentication getCredential() {
		return _smbCredential;
	}

	@Override
	public void release() {
		// nothing to do
	}

	@Override
	public boolean exists(Path path, LinkOption... options) {
		return exists(convertToUnixPath(path), options);
	}

	public boolean exists(String path, LinkOption... options) {
		try {
			return createSmbFile(path, options).exists();
		} catch (SmbException Ex) {
			return false;
		}
	}

	@Override
	public boolean isDirectory(Path path, LinkOption... options) {
		return isDirectory(convertToUnixPath(path), options);
	}

	public boolean isDirectory(String path, LinkOption... options) {
		try {
			return createSmbFile(path, options).isDirectory();
		} catch (SmbException Ex) {
			return false;
		}
	}

	@Override
	public boolean isRegularFile(Path path, LinkOption... options) {
		return isRegularFile(convertToUnixPath(path));
	}

	public boolean isRegularFile(String path, LinkOption... options) {
		try {
			return createSmbFile(path, options).isFile();
		} catch (SmbException Ex) {
			return false;
		}
	}

	@Override
	public boolean isSymbolicLink(Path path) {
		// Samba doesn't support links
		return false;
	}

	public boolean isSymbolicLink(String path) {
		// Samba doesn't support links
		return false;
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		createDirectory(convertToUnixPath(dir), attrs);
	}

	public void createDirectory(String dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		if (dir == null || dir.trim().length() == 0) {
			throw new IllegalArgumentException(dir + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		SmbFile smbfile = createSmbFile(dir);
		try {
			smbfile.mkdir();
		} catch (SmbException Ex) {
			WrapperSmbException wex = new WrapperSmbException(Ex);
			if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				throw new WrapperNoSuchFileException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				throw new WrapperNoSuchShareException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_COLLISION) {
				try {
					if (readAttributes(dir).isDirectory()) {
						// the dir already exists => no error
					} else {
						// a link or a regular file exists => error
						throw new WrapperFileAlreadyExistsException(dir, wex);
					}
				} catch (NoSuchFileException Exx) {
					// concurrency pb : should recreate ?
					throw new IOException(Msg.bind(Messages.CifsEx_MKDIR,
							smbfile), wex);
				}
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_MKDIR, smbfile),
						wex);
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
		String unixDir = convertToUnixPath(dir);
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
		// Samba can't do that
		throw new SymbolicLinkNotSupported(link, target,
				"CIFS doesn't support Symbolic links.");
	}

	public void createSymbolicLink(String link, String target,
			FileAttribute<?>... attrs) throws IOException,
			SymbolicLinkNotSupported, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		// Samba can't do that
		throw new SymbolicLinkNotSupported(link, target,
				"CIFS doesn't support Symbolic links.");
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		// Samba can't do that
		return null;
	}

	public Path readSymbolicLink(String link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		// Samba can't do that
		return null;
	}

	@Override
	public void delete(Path path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		delete(convertToUnixPath(path));
	}

	public void delete(String path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		SmbFile smbfile = createSmbFile(path);
		try {
			if (CifsFileAttributes.createCifsFileAttributesFromSmbFile(smbfile)
					.isDirectory()) {
				/*
				 * if not done, will receive "OxC0000001 must end with '/'"
				 */
				if (path.charAt(path.length() - 1) != '/') {
					path += '/';
					smbfile = createSmbFile(path);
				}
				String[] content = smbfile.list();
				if (content != null && content.length != 0) {
					throw new WrapperDirectoryNotEmptyException(path);
				}
			}
			smbfile.delete();
		} catch (SmbException Ex) {
			WrapperSmbException wex = new WrapperSmbException(Ex);
			if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				throw new WrapperNoSuchShareException(path, wex);
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_RM, smbfile),
						wex);
			}
		}
	}

	@Override
	public boolean deleteIfExists(Path path) throws IOException,
			DirectoryNotEmptyException, AccessDeniedException {
		return deleteIfExists(convertToUnixPath(path));
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
		deleteDirectory(convertToUnixPath(dir));
	}

	public void deleteDirectory(String dir) throws IOException,
			NotDirectoryException, AccessDeniedException {
		SmbFile smbfile = createSmbFile(dir);
		try {
			if (!CifsFileAttributes
					.createCifsFileAttributesFromSmbFile(smbfile).isDirectory()) {
				throw new WrapperNotDirectoryException(dir);
			}
			/*
			 * if not done, will receive "OxC0000001 must end with '/'"
			 */
			if (dir.charAt(dir.length() - 1) != '/') {
				dir += '/';
				smbfile = createSmbFile(dir);
			}
			smbfile.delete();
		} catch (SmbException Ex) {
			WrapperSmbException wex = new WrapperSmbException(Ex);
			if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				throw new WrapperAccessDeniedException(dir, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
				// don't do anything
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				// don't do anything
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				// don't do anything
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_RMDIR, smbfile),
						wex);
			}
		}
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path path)
			throws IOException, InterruptedIOException, NoSuchFileException,
			NotDirectoryException, AccessDeniedException {
		return newDirectoryStream(convertToUnixPath(path));
	}

	public DirectoryStream<Path> newDirectoryStream(String path)
			throws IOException, InterruptedIOException, NoSuchFileException,
			NotDirectoryException, AccessDeniedException {
		SmbFile smbfile = createSmbFile(path);
		final List<Path> content = new ArrayList<Path>();

		try {
			if (!CifsFileAttributes
					.createCifsFileAttributesFromSmbFile(smbfile).isDirectory()) {
				throw new WrapperNotDirectoryException(path);
			}
			/*
			 * if not done, will receive "OxC0000001 must end with '/'"
			 */
			if (path.charAt(path.length() - 1) != '/') {
				path += '/';
				smbfile = createSmbFile(path);
			}
			for (String file : smbfile.list()) {
				content.add(Paths.get(path, file));
			}
		} catch (SmbException Ex) {
			WrapperSmbException wex = new WrapperSmbException(Ex);
			if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				throw new WrapperNoSuchShareException(path, wex);
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_LS, smbfile),
						wex);
			}
		}

		return new DirectoryStream<Path>() {

			@Override
			public void close() throws IOException {
				// nothing to do
			}

			@Override
			public Iterator<Path> iterator() {
				return content.iterator();
			}

		};
	}

	@Override
	public EnhancedFileAttributes readAttributes(Path path) throws IOException,
			NoSuchFileException, AccessDeniedException {
		return readAttributes(convertToUnixPath(path));
	}

	public EnhancedFileAttributes readAttributes(String path)
			throws IOException, NoSuchFileException, AccessDeniedException {
		SmbFile smbfile = createSmbFile(path);
		try {
			return CifsFileAttributes
					.createCifsFileAttributesFromSmbFile(smbfile);
		} catch (SmbException Ex) {
			WrapperSmbException wex = new WrapperSmbException(Ex);
			if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				/*
				 * NtStatus.NT_STATUS_ACCESS_DENIED is raised when, for example,
				 * a resource smb://x.x.x.x/share1/ exists and is accessed by a
				 * user which is not allowed to.
				 */
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				/*
				 * NtStatus.NT_STATUS_LOGON_FAILURE is raised when, for example,
				 * a resource smb://x.x.x.x/share1/ exists and is accessed by an
				 * invalid username/password.
				 */
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				/*
				 * NtStatus.NT_STATUS_INVALID_LOGON_HOURS is raised when, for
				 * example, a resource smb://x.x.x.x/share1/ exists and is
				 * accessed by a valid user outside his valid login hours.
				 */
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				/*
				 * NtStatus.NT_STATUS_PASSWORD_EXPIRED is raised when, for
				 * example, a resource smb://x.x.x.x/share1/ exists and is
				 * accessed by a valid user whose password is expired.
				 */
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				/*
				 * NtStatus.NT_STATUS_ACCOUNT_DISABLED is raised when, for
				 * example, a resource smb://x.x.x.x/share1/ exists and is
				 * accessed by a valid user whose account is disabled.
				 */
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
				/*
				 * NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND is raised when, for
				 * example, a resource smb://x.x.x.x/share1/ exists and the
				 * requested resource is smb://x.x.x.x/share1/not-exists
				 */
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				/*
				 * NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND is raised when, for
				 * example, a resource smb://x.x.x.x/share1/ exists and the
				 * requested resource is
				 * smb://x.x.x.x/share1/dir-not-exists/dir-
				 * not-exists-too/not-exists
				 */
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				/*
				 * NtStatus.NT_STATUS_BAD_NETWORK_NAME is raised when, for
				 * example, when the requested resource is
				 * smb://x.x.x.x/a-path-which-is-not-shared
				 */
				throw new WrapperNoSuchShareException(path, wex);
			}
			throw new IOException(Msg.bind(Messages.CifsEx_STAT, smbfile), wex);
		}
	}

	@Override
	public void setAttributes(Path path, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			IllegalFileAttributeException, AccessDeniedException {
		setAttributes(convertToUnixPath(path), attrs);
	}

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
		ConsolidatedException full = new ConsolidatedException(Msg.bind(
				Messages.CifsFSEx_FAILED_TO_SET_ATTRIBUTES, path));
		for (FileAttribute<?> attr : attrs) {
			if (attr == null) {
				continue;
			}
			try {
				/*
				 * will throw NullPointerException if attr.name() is null.
				 * 
				 * will throw UnsupportedOperationException if attr.name()
				 * denotes an unrecognized view.
				 * 
				 * will throw IllegalArgumentException if attr.name() denotes a
				 * know view but an unrecognized attribute.
				 * 
				 * will throw NullPointerException if attr.valiue() is null.
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
				if (attr.name().equals(AttributeDosArchive.NAME)) {
					setAttributeArchive(path,
							((AttributeDosArchive) attr).value());
				} else if (attr.name().equals(AttributeDosHidden.NAME)) {
					setAttributeHidden(path,
							((AttributeDosHidden) attr).value());
				} else if (attr.name().equals(AttributeDosReadOnly.NAME)) {
					setAttributeReadOnly(path,
							((AttributeDosReadOnly) attr).value());
				} else if (attr.name().equals(AttributeDosSystem.NAME)) {
					setAttributeSystem(path,
							((AttributeDosSystem) attr).value());
				} else if (attr.name().indexOf("dos:") == 0
						|| attr.name().indexOf("owner:") == 0) {
					// view is known but attribute name is not recognized
					throw new IllegalArgumentException("'" + attr.name() + "'"
							+ " not recognized.");
				} else {
					// view is not recognized
					throw new UnsupportedOperationException("View '"
							+ attr.name() + "'" + " is not availbale.");
				}
			} catch (WrapperNoSuchFileException | WrapperAccessDeniedException Ex) {
				// only need the reason
				full.addCause(new MelodyException(Msg.bind(
						Messages.CifsFSEx_FAILED_TO_SET_ATTRIBUTE, attr,
						Ex.getReason())));
			} catch (FileSystemException Ex) {
				// don't want neither the stack trace nor the file name
				String msg = Ex.getReason();
				if (msg == null || msg.length() == 0) {
					msg = Ex.getClass().getName();
				} else {
					msg = Ex.getClass().getName() + " - " + msg;
				}
				full.addCause(new MelodyException(Msg.bind(
						Messages.CifsFSEx_FAILED_TO_SET_ATTRIBUTE, attr, msg)));
			} catch (IOException Ex) {
				full.addCause(new MelodyException(Msg.bind(
						Messages.CifsFSEx_FAILED_TO_SET_ATTRIBUTE_X, attr), Ex));
			} catch (UnsupportedOperationException | IllegalArgumentException
					| ClassCastException Ex) {
				// don't want the stack trace
				full.addCause(new MelodyException(Msg.bind(
						Messages.CifsFSEx_FAILED_TO_SET_ATTRIBUTE, attr, Ex)));
			} catch (Throwable Ex) {
				// want the stack trace
				full.addCause(new MelodyException(Msg.bind(
						Messages.CifsFSEx_FAILED_TO_SET_ATTRIBUTE_X, attr), Ex));
			}
		}
		if (full.countCauses() != 0) {
			throw new IllegalFileAttributeException(full);
		}
	}

	public void setAttributeArchive(Path path, boolean isArchive)
			throws IOException, NoSuchFileException, AccessDeniedException {
		setAttributeArchive(convertToUnixPath(path), isArchive);
	}

	public void setAttributeArchive(String path, boolean isArchive)
			throws IOException, NoSuchFileException, AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		SmbFile smbfile = createSmbFile(path);
		try {
			int attrs = smbfile.getAttributes();
			if (isArchive == true && (attrs | SmbFile.ATTR_ARCHIVE) == 0) {
				smbfile.setAttributes(attrs & SmbFile.ATTR_ARCHIVE);
			} else if (isArchive == false
					&& (attrs & SmbFile.ATTR_ARCHIVE) != 0) {
				smbfile.setAttributes(attrs & ~SmbFile.ATTR_ARCHIVE);
			}
		} catch (SmbException Ex) {
			WrapperSmbException wex = new WrapperSmbException(Ex);
			if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				throw new WrapperNoSuchShareException(path, wex);
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_CHA, isArchive,
						smbfile), wex);
			}
		}
	}

	public void setAttributeHidden(Path path, boolean isHidden)
			throws IOException, NoSuchFileException, AccessDeniedException {
		setAttributeHidden(convertToUnixPath(path), isHidden);
	}

	public void setAttributeHidden(String path, boolean isHidden)
			throws IOException, NoSuchFileException, AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		SmbFile smbfile = createSmbFile(path);
		try {
			int attrs = smbfile.getAttributes();
			if (isHidden == true && (attrs & SmbFile.ATTR_HIDDEN) == 0) {
				smbfile.setAttributes(attrs | SmbFile.ATTR_HIDDEN);
			} else if (isHidden == false && (attrs & SmbFile.ATTR_HIDDEN) != 0) {
				smbfile.setAttributes(attrs & ~SmbFile.ATTR_HIDDEN);
			}
		} catch (SmbException Ex) {
			WrapperSmbException wex = new WrapperSmbException(Ex);
			if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				throw new WrapperNoSuchShareException(path, wex);
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_CHH, isHidden,
						smbfile), wex);
			}
		}
	}

	public void setAttributeReadOnly(Path path, boolean isReadOnly)
			throws IOException, NoSuchFileException, AccessDeniedException {
		setAttributeReadOnly(convertToUnixPath(path), isReadOnly);
	}

	public void setAttributeReadOnly(String path, boolean isReadOnly)
			throws IOException, NoSuchFileException, AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		SmbFile smbfile = createSmbFile(path);
		try {
			int attrs = smbfile.getAttributes();
			if (isReadOnly == true && (attrs & SmbFile.ATTR_READONLY) == 0) {
				smbfile.setAttributes(attrs | SmbFile.ATTR_READONLY);
			} else if (isReadOnly == false
					&& (attrs & SmbFile.ATTR_READONLY) != 0) {
				smbfile.setAttributes(attrs & ~SmbFile.ATTR_READONLY);
			}
		} catch (SmbException Ex) {
			WrapperSmbException wex = new WrapperSmbException(Ex);
			if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				throw new WrapperNoSuchShareException(path, wex);
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_CHR, isReadOnly,
						smbfile), wex);
			}
		}
	}

	public void setAttributeSystem(Path path, boolean isSystem)
			throws IOException, NoSuchFileException, AccessDeniedException {
		setAttributeSystem(convertToUnixPath(path), isSystem);
	}

	public void setAttributeSystem(String path, boolean isSystem)
			throws IOException, NoSuchFileException, AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		SmbFile smbfile = createSmbFile(path);
		try {
			int attrs = smbfile.getAttributes();
			if (isSystem == true && (attrs & SmbFile.ATTR_SYSTEM) == 0) {
				smbfile.setAttributes(attrs | SmbFile.ATTR_SYSTEM);
			} else if (isSystem == false && (attrs & SmbFile.ATTR_SYSTEM) != 0) {
				smbfile.setAttributes(attrs & ~SmbFile.ATTR_SYSTEM);
			}
		} catch (SmbException Ex) {
			WrapperSmbException wex = new WrapperSmbException(Ex);
			if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				throw new WrapperAccessDeniedException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				throw new WrapperNoSuchFileException(path, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				throw new WrapperNoSuchShareException(path, wex);
			} else {
			}
			throw new IOException(Msg.bind(Messages.CifsEx_CHS, isSystem,
					smbfile), wex);
		}
	}

}