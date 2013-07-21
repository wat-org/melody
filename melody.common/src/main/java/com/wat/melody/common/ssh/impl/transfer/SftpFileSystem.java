package com.wat.melody.common.ssh.impl.transfer;

import java.io.IOException;
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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SftpFileSystem implements FileSystem {

	/**
	 * Returns {@code false} if NOFOLLOW_LINKS is present.
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

	protected static String convertToUnixPath(String path) {
		return path.replaceAll("\\\\", "/");
	}

	protected static String convertToUnixPath(Path path) {
		return convertToUnixPath(path.toString());
	}

	private ChannelSftp _channel;

	public SftpFileSystem(ChannelSftp channel) {
		_channel = channel;
	}

	protected ChannelSftp getChannel() {
		return _channel;
	}

	@Override
	public void release() {
		_channel.disconnect();
	}

	@Override
	public boolean exists(Path path, LinkOption... options) {
		return exists(convertToUnixPath(path), options);
	}

	private boolean exists(String path, LinkOption... options) {
		try {
			return readAttributes0(path, options) != null;
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public boolean isDirectory(Path path, LinkOption... options) {
		return isDirectory(convertToUnixPath(path), options);
	}

	private boolean isDirectory(String path, LinkOption... options) {
		try {
			return readAttributes0(path, options).isDir();
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public boolean isRegularFile(Path path, LinkOption... options) {
		return isRegularFile(convertToUnixPath(path), options);
	}

	private boolean isRegularFile(String path, LinkOption... options) {
		try {
			SftpATTRS attrs = readAttributes0(path, options);
			return !attrs.isDir() && !attrs.isLink();
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public boolean isSymbolicLink(Path path) {
		return isSymbolicLink(convertToUnixPath(path));
	}

	private boolean isSymbolicLink(String path) {
		try {
			return readAttributes0(path, LinkOption.NOFOLLOW_LINKS).isLink();
		} catch (IOException Ex) {
			return false;
		}
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
		try {
			createDirectory(convertToUnixPath(dir), attrs);
		} catch (NoSuchFileException Ex) {
			throw new NoSuchFileException(Ex.getMessage()
					+ ": NoSuchFileException.");
		} catch (FileAlreadyExistsException Ex) {
			throw new FileAlreadyExistsException(Ex.getMessage()
					+ ": FileAlreadyExistsException.");
		} catch (AccessDeniedException Ex) {
			throw new AccessDeniedException(Ex.getMessage()
					+ ": AccessDeniedException.");
		}
	}

	private void createDirectory(String dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
		try {
			_channel.mkdir(dir);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw newAccessDeniedException(dir, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				// mean the parent dir doesn't exists
				throw newNoSuchFileException(dir, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE) {
				try {
					if (readAttributes0(dir, LinkOption.NOFOLLOW_LINKS).isDir()) {
						// the dir already exists => no error
						return;
					} else {
						// a link or file exists => error
						throw new FileAlreadyExistsException(dir);
					}
				} catch (NoSuchFileException Exx) {
					// concurrency pb : should recreate ?
					throw new IOException(Msg.bind(Messages.SftpEx_MKDIR, dir),
							Ex);
				}
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_MKDIR, dir), Ex);
			}
		}
	}

	@Override
	public void createDirectories(Path dir, FileAttribute<?>... attrs)
			throws IOException, FileAlreadyExistsException,
			AccessDeniedException {
		try {
			_createDirectories(dir, attrs);
		} catch (FileAlreadyExistsException Ex) {
			throw new FileAlreadyExistsException(Ex.getMessage()
					+ ": FileAlreadyExistsException.");
		} catch (AccessDeniedException Ex) {
			throw new AccessDeniedException(Ex.getMessage()
					+ ": AccessDeniedException.");
		}
	}

	private void _createDirectories(Path dir, FileAttribute<?>... attrs)
			throws IOException, FileAlreadyExistsException,
			AccessDeniedException {
		if (dir.toString().length() == 0 || dir.getNameCount() < 1) {
			return;
		}
		String unixDir = convertToUnixPath(dir);
		try {
			createDirectory(unixDir);
			return;
		} catch (NoSuchFileException Ex) {
			// if the top first dir cannot be created => raise an error
			if (dir.getNameCount() <= 1) {
				throw Ex;
			}
		} catch (FileAlreadyExistsException Ex) {
			// if the file is a link on a dir or a dir => no error
			try {
				if (readAttributes0(unixDir).isDir()) {
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
		_createDirectories(parent);
		try {
			createDirectory(unixDir);
		} catch (FileAlreadyExistsException Ex) {
			// if the file is a link on a dir or a dir => no error
			try {
				if (readAttributes0(unixDir).isDir()) {
					return;
				}
			} catch (NoSuchFileException Exx) {
				// concurrency pb : should recreate ?
			}
			throw Ex;
		}
	}

	@Override
	public void createSymbolicLink(Path link, Path target,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
		try {
			createSymbolicLink(convertToUnixPath(link),
					convertToUnixPath(target), attrs);
		} catch (NoSuchFileException Ex) {
			throw new NoSuchFileException(Ex.getMessage()
					+ ": NoSuchFileException.");
		} catch (FileAlreadyExistsException Ex) {
			throw new FileAlreadyExistsException(Ex.getMessage()
					+ ": FileAlreadyExistsException.");
		} catch (AccessDeniedException Ex) {
			throw new AccessDeniedException(Ex.getMessage()
					+ ": AccessDeniedException.");
		}
	}

	private void createSymbolicLink(String link, String target,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
		try {
			_channel.symlink(target, link);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw newAccessDeniedException(link, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw newNoSuchFileException(link, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE
					&& exists(link, LinkOption.NOFOLLOW_LINKS)) {
				throw newFileAlreadyExistsException(link, Ex);
			} else {
				throw new IOException(
						Msg.bind(Messages.SftpEx_LN, target, link), Ex);
			}
		}
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		try {
			return readSymbolicLink(convertToUnixPath(link));
		} catch (NoSuchFileException Ex) {
			throw new NoSuchFileException(Ex.getMessage()
					+ ": NoSuchFileException.");
		} catch (NotLinkException Ex) {
			throw new NotLinkException(Ex.getMessage() + ": NotLinkException.");
		} catch (AccessDeniedException Ex) {
			throw new AccessDeniedException(Ex.getMessage()
					+ ": AccessDeniedException.");
		}
	}

	private Path readSymbolicLink(String link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		try {
			return Paths.get(_channel.readlink(link));
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw newAccessDeniedException(link, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw newNoSuchFileException(link, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_BAD_MESSAGE) {
				throw newNotLinkException(link, Ex);
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_READLINK, link),
						Ex);
			}
		}
	}

	@Override
	public void delete(Path path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		try {
			delete(convertToUnixPath(path));
		} catch (NoSuchFileException Ex) {
			throw new NoSuchFileException(Ex.getMessage()
					+ ": NoSuchFileException.");
		} catch (DirectoryNotEmptyException Ex) {
			throw new DirectoryNotEmptyException(Ex.getMessage()
					+ ": DirectoryNotEmptyException.");
		} catch (AccessDeniedException Ex) {
			throw new AccessDeniedException(Ex.getMessage()
					+ ": AccessDeniedException.");
		}
	}

	private void delete(String path) throws IOException,
			DirectoryNotEmptyException, NoSuchFileException,
			AccessDeniedException {
		try {
			_channel.rm(path);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw newAccessDeniedException(path, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw newNoSuchFileException(path, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE) {
				deleteEmptyDir(path);
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_RM, path), Ex);
			}
		}
	}

	private void deleteEmptyDir(String path) throws IOException,
			NoSuchFileException, DirectoryNotEmptyException,
			NotDirectoryException, AccessDeniedException {
		try {
			_channel.rmdir(path);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw newAccessDeniedException(path, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				// will raise SSH_FX_NO_SUCH_FILE if it is not a dir
				if (exists(path, LinkOption.NOFOLLOW_LINKS)) {
					throw newNotDirectoryException(path, Ex);
				} else {
					throw newNoSuchFileException(path, Ex);
				}
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE) {
				throw newDirectoryNotEmptyException(path, Ex);
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_RMDIR, path), Ex);
			}
		}
	}

	private boolean deleteEmptyDirIfExists(String path) throws IOException,
			DirectoryNotEmptyException, NotDirectoryException,
			AccessDeniedException {
		try {
			deleteEmptyDir(path);
			return true;
		} catch (NoSuchFileException Ex) {
			return false;
		}
	}

	@Override
	public boolean deleteIfExists(Path path) throws IOException,
			DirectoryNotEmptyException, AccessDeniedException {
		try {
			return deleteIfExists(convertToUnixPath(path));
		} catch (DirectoryNotEmptyException Ex) {
			throw new DirectoryNotEmptyException(Ex.getMessage()
					+ ": NotLinkException.");
		} catch (AccessDeniedException Ex) {
			throw new AccessDeniedException(Ex.getMessage()
					+ ": AccessDeniedException.");
		}
	}

	private boolean deleteIfExists(String path) throws IOException,
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
		try {
			deleteDirectory(convertToUnixPath(dir));
		} catch (NotDirectoryException Ex) {
			throw new NotDirectoryException(Ex.getMessage()
					+ ": NotDirectoryException.");
		} catch (AccessDeniedException Ex) {
			throw new AccessDeniedException(Ex.getMessage()
					+ ": AccessDeniedException.");
		}
	}

	private void deleteDirectory(String dir) throws IOException,
			NotDirectoryException, AccessDeniedException {
		try {
			for (LsEntry entry : listDirectory(dir)) {
				if (entry.getAttrs().isDir()) {
					deleteDirectory(dir + "/" + entry.getFilename());
				} else {
					deleteIfExists(dir + "/" + entry.getFilename());
				}
			}
		} catch (NoSuchFileException ignored) {
		}
		deleteEmptyDirIfExists(dir);
	}

	public Vector<LsEntry> listDirectory(String path) throws IOException,
			NoSuchFileException, NotDirectoryException, AccessDeniedException {
		final Vector<LsEntry> content = new Vector<LsEntry>();
		listDirectory(path, new LsEntrySelector() {
			public int select(LsEntry entry) {
				// don't add . and ..
				if (!entry.getFilename().equals(".")
						&& !entry.getFilename().equals("..")) {
					content.addElement(entry);
				}
				return CONTINUE;
			}
		});
		return content;
	}

	private void listDirectory(String path, LsEntrySelector selector)
			throws IOException, NoSuchFileException, NotDirectoryException,
			AccessDeniedException {
		if (!readAttributes0(path).isDir()) {
			throw new NotDirectoryException(path);
		}
		try {
			_channel.ls(path, selector);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw newAccessDeniedException(path, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw newNoSuchFileException(path, Ex);
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_LS, path), Ex);
			}
		}
	}

	@Override
	public EnhancedFileAttributes readAttributes(Path path) throws IOException,
			NoSuchFileException, AccessDeniedException {
		try {
			return readAttributes(convertToUnixPath(path));
		} catch (NoSuchFileException Ex) {
			throw new NoSuchFileException(Ex.getMessage()
					+ ": NoSuchFileException.");
		} catch (AccessDeniedException Ex) {
			throw new AccessDeniedException(Ex.getMessage()
					+ ": AccessDeniedException.");
		}
	}

	private EnhancedFileAttributes readAttributes(String path)
			throws IOException, NoSuchFileException, AccessDeniedException {
		SftpATTRS pathAttrs = readAttributes0(path, LinkOption.NOFOLLOW_LINKS);
		Path target = null;
		SftpATTRS realAttrs = null;
		if (pathAttrs.isLink()) {
			target = readSymbolicLink(path);
			try {
				realAttrs = readAttributes0(path);
			} catch (NoSuchFileException ignored) {
			}
		}
		return new SftpFileAttributes(pathAttrs, target, realAttrs);
	}

	private SftpATTRS readAttributes0(String path, LinkOption... options)
			throws IOException, NoSuchFileException, AccessDeniedException {
		try {
			if (followLinks(options)) {
				return _channel.stat(path);
			} else {
				return _channel.lstat(path);
			}
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw newAccessDeniedException(path, Ex);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw newNoSuchFileException(path, Ex);
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_LSTAT, path), Ex);
			}
		}
	}

	public DirectoryStream<Path> newDirectoryStream(Path path)
			throws IOException, NotDirectoryException, NoSuchFileException {
		try {
			return newDirectoryStream(convertToUnixPath(path));
		} catch (NoSuchFileException Ex) {
			throw new NoSuchFileException(Ex.getMessage()
					+ ": NoSuchFileException.");
		} catch (NotDirectoryException Ex) {
			throw new NotDirectoryException(Ex.getMessage()
					+ ": NotDirectoryException.");
		} catch (AccessDeniedException Ex) {
			throw new AccessDeniedException(Ex.getMessage()
					+ ": AccessDeniedException.");
		}
	}

	private DirectoryStream<Path> newDirectoryStream(final String path)
			throws IOException, NoSuchFileException, NotDirectoryException,
			AccessDeniedException {
		final List<Path> content = new ArrayList<Path>();
		listDirectory(path, new LsEntrySelector() {
			public int select(LsEntry entry) {
				// don't add . and ..
				if (!entry.getFilename().equals(".")
						&& !entry.getFilename().equals("..")) {
					content.add(Paths.get(path, entry.getFilename()));
				}
				return CONTINUE;
			}
		});
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

	public void upload(Path source, Path destination) throws IOException {
		upload(source.toString(), convertToUnixPath(destination));
	}

	public void upload(String source, String destination) throws IOException {
		try {
			_channel.put(source, destination);
		} catch (SftpException Ex) {
			// TODO : throw dedicated exception
			throw new IOException(Msg.bind(Messages.SfptEx_PUT, source,
					destination), Ex);
		}
	}

	public void download(Path source, Path destination) throws IOException {
		download(convertToUnixPath(source), destination.toString());
	}

	public void download(String source, String destination) throws IOException {
		try {
			_channel.get(source, destination);
		} catch (SftpException Ex) {
			// TODO : throw dedicated exception
			throw new IOException(Msg.bind(Messages.SfptEx_GET, source,
					destination), Ex);
		}
	}

	private NoSuchFileException newNoSuchFileException(String path,
			Exception cause) {
		NoSuchFileException e = new NoSuchFileException(path);
		e.initCause(cause);
		return e;
	}

	private FileAlreadyExistsException newFileAlreadyExistsException(
			String path, Exception cause) {
		FileAlreadyExistsException e = new FileAlreadyExistsException(path);
		e.initCause(cause);
		return e;
	}

	private NotLinkException newNotLinkException(String path, Exception cause) {
		NotLinkException e = new NotLinkException(path);
		e.initCause(cause);
		return e;
	}

	private DirectoryNotEmptyException newDirectoryNotEmptyException(
			String path, Exception cause) {
		DirectoryNotEmptyException e = new DirectoryNotEmptyException(path);
		e.initCause(cause);
		return e;
	}

	private NotDirectoryException newNotDirectoryException(String path,
			Exception cause) {
		NotDirectoryException e = new NotDirectoryException(path);
		e.initCause(cause);
		return e;
	}

	private AccessDeniedException newAccessDeniedException(String path,
			Exception cause) {
		AccessDeniedException e = new AccessDeniedException(path);
		e.initCause(cause);
		return e;
	}

}