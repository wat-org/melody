package com.wat.melody.common.ssh.impl.transfer;

import java.io.IOException;
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
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.PosixGroup;
import com.wat.melody.common.files.PosixPermissions;
import com.wat.melody.common.files.PosixUser;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.IllegalPosixGroupException;
import com.wat.melody.common.files.exception.IllegalPosixUserException;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperDirectoryNotEmptyException;
import com.wat.melody.common.files.exception.WrapperFileAlreadyExistsException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.files.exception.WrapperNotDirectoryException;
import com.wat.melody.common.files.exception.WrapperNotLinkException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.impl.Messages;
import com.wat.melody.common.transfer.resources.attributes.AttributePosixGroup;
import com.wat.melody.common.transfer.resources.attributes.AttributePosixPermissions;
import com.wat.melody.common.transfer.resources.attributes.AttributePosixUser;

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
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		createDirectory(convertToUnixPath(dir), attrs);
	}

	private void createDirectory(String dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		if (dir == null || dir.trim().length() == 0) {
			throw new IllegalArgumentException(dir + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			_channel.mkdir(dir);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(dir);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				// mean the parent dir doesn't exists
				throw new WrapperNoSuchFileException(dir);
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE) {
				try {
					if (readAttributes0(dir, LinkOption.NOFOLLOW_LINKS).isDir()) {
						// the dir already exists => no error
					} else {
						// a link or file exists => error
						throw new WrapperFileAlreadyExistsException(dir);
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
		setAttributes(dir, attrs);
	}

	@Override
	public void createDirectories(Path dir, FileAttribute<?>... attrs)
			throws IOException, FileAlreadyExistsException,
			IllegalFileAttributeException, AccessDeniedException {
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
		createDirectories(parent, attrs);
		try {
			createDirectory(unixDir, attrs);
		} catch (FileAlreadyExistsException Ex) {
			// if the file is a link on a dir => no error
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
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		createSymbolicLink(convertToUnixPath(link), convertToUnixPath(target),
				attrs);
	}

	private void createSymbolicLink(String link, String target,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
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
			_channel.symlink(target, link);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(link);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(link);
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE
					&& exists(link, LinkOption.NOFOLLOW_LINKS)) {
				throw new WrapperFileAlreadyExistsException(link);
			} else {
				throw new IOException(
						Msg.bind(Messages.SftpEx_LN, target, link), Ex);
			}
		}
		setAttributes(link, attrs);
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		return readSymbolicLink(convertToUnixPath(link));
	}

	private Path readSymbolicLink(String link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		if (link == null || link.trim().length() == 0) {
			throw new IllegalArgumentException(link + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			return Paths.get(_channel.readlink(link));
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(link);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(link);
			} else if (Ex.id == ChannelSftp.SSH_FX_BAD_MESSAGE) {
				throw new WrapperNotLinkException(link);
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_READLINK, link),
						Ex);
			}
		}
	}

	@Override
	public void delete(Path path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		delete(convertToUnixPath(path));
	}

	private void delete(String path) throws IOException,
			DirectoryNotEmptyException, NoSuchFileException,
			AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			_channel.rm(path);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(path);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(path);
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
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			_channel.rmdir(path);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(path);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				// will raise SSH_FX_NO_SUCH_FILE if it is not a dir
				if (exists(path, LinkOption.NOFOLLOW_LINKS)) {
					throw new WrapperNotDirectoryException(path);
				} else {
					throw new WrapperNoSuchFileException(path);
				}
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE) {
				throw new WrapperDirectoryNotEmptyException(path);
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
		return deleteIfExists(convertToUnixPath(path));
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
		deleteDirectory(convertToUnixPath(dir));
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
			throw new WrapperNotDirectoryException(path);
		}
		try {
			_channel.ls(path, selector);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(path);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(path);
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_LS, path), Ex);
			}
		}
	}

	public DirectoryStream<Path> newDirectoryStream(Path path)
			throws IOException, NotDirectoryException, NoSuchFileException,
			AccessDeniedException {
		return newDirectoryStream(convertToUnixPath(path));
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

	@Override
	public EnhancedFileAttributes readAttributes(Path path) throws IOException,
			NoSuchFileException, AccessDeniedException {
		return readAttributes(convertToUnixPath(path));
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
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		boolean followlink = followLinks(options);
		try {
			if (followlink) {
				return _channel.stat(path);
			} else {
				return _channel.lstat(path);
			}
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(path);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(path);
			} else {
				throw new IOException(Msg.bind(
						followlink ? Messages.SftpEx_STAT
								: Messages.SftpEx_LSTAT, path), Ex);
			}
		}
	}

	@Override
	public void setAttributes(Path path, FileAttribute<?>... attributes)
			throws IOException, NoSuchFileException,
			IllegalFileAttributeException, AccessDeniedException {
		setAttributes(convertToUnixPath(path), attributes);
	}

	@SuppressWarnings("unchecked")
	private void setAttributes(String path, FileAttribute<?>... attributes)
			throws IOException, NoSuchFileException,
			IllegalFileAttributeException, AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (attributes == null) {
			return;
		}
		Boolean isLink = false;
		if (isSymbolicLink(path)) {
			isLink = true;
		}
		ConsolidatedException full = new ConsolidatedException(Msg.bind(
				Messages.SftpFSEx_FAILED_TO_SET_ATTRIBUTES, path));
		for (FileAttribute<?> attr : attributes) {
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
				 * no such file, ...).
				 * 
				 * will throw FileSystemException if the subject of the
				 * operation is a link.
				 */
				if (isLink) {
					throw new FileSystemException(
							path,
							null,
							Messages.SftpFSEx_SET_ATTRIBUTES_NOT_SUPPORTED_ON_LINK);
				}
				// optim
				else if (attr instanceof AttributePosixGroup) {
					chgrp(path, ((AttributePosixGroup) attr).getPosixGroup()
							.toInt());
				} else if (attr instanceof AttributePosixUser) {
					chown("/tmp", ((AttributePosixUser) attr).getPosixUser()
							.toInt());
				} else if (attr instanceof AttributePosixPermissions) {
					chmod(path, ((AttributePosixPermissions) attr)
							.getPosixPermissions().toInt());
				}
				// end optim
				else if (attr.name().equals(AttributePosixGroup.NAME)) {
					try {
						chgrp(path,
								PosixGroup.fromGroupPrincipal(
										(GroupPrincipal) attr.value()).toInt());
					} catch (IllegalPosixGroupException Ex) {
						throw new IllegalArgumentException(Ex);
					}
				} else if (attr.name().equals(AttributePosixUser.NAME)) {
					try {
						chown(path,
								PosixUser.fromUserPrincipal(
										(UserPrincipal) attr.value()).toInt());
					} catch (IllegalPosixUserException Ex) {
						throw new MelodyException(Ex);
					}
				} else if (attr.name().equals(AttributePosixPermissions.NAME)) {
					chmod(path,
							PosixPermissions.fromPosixFilePermissionSet(
									(Set<PosixFilePermission>) attr.value())
									.toInt());
				} else if (attr.name().indexOf("posix:") == 0
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
				// unwrap needed
				full.addCause(new MelodyException(Msg.bind(
						Messages.SftpFSEx_FAILED_TO_SET_ATTRIBUTE, attr, Ex
								.getClass().getSuperclass().getName())));
			} catch (FileSystemException Ex) {
				// don't want neither the stack trace nor the file name
				String msg = Ex.getReason();
				if (msg == null || msg.length() == 0) {
					msg = Ex.getClass().getName();
				} else {
					msg = Ex.getClass().getName() + " - " + msg;
				}
				full.addCause(new MelodyException(Msg.bind(
						Messages.SftpFSEx_FAILED_TO_SET_ATTRIBUTE, attr, msg)));
			} catch (UnsupportedOperationException | IllegalArgumentException
					| ClassCastException | IOException Ex) {
				// don't want the stack trace
				full.addCause(new MelodyException(Msg.bind(
						Messages.SftpFSEx_FAILED_TO_SET_ATTRIBUTE, attr, Ex)));
			} catch (Throwable Ex) {
				// want the stack trace
				full.addCause(new MelodyException(Msg.bind(
						Messages.SftpFSEx_FAILED_TO_SET_ATTRIBUTE_X, attr), Ex));
			}
		}
		if (full.countCauses() != 0) {
			throw new IllegalFileAttributeException(full);
		}
	}

	public void chmod(Path path, int permissions) throws IOException,
			NoSuchFileException, AccessDeniedException {
		chmod(convertToUnixPath(path), permissions);
	}

	private void chmod(String path, int permissions) throws IOException,
			NoSuchFileException, AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			_channel.chmod(permissions, path);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(path);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(path);
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_CHMOD,
						permissions, path), Ex);
			}
		}
	}

	public void chgrp(Path path, int groupid) throws IOException,
			NoSuchFileException, AccessDeniedException {
		chgrp(convertToUnixPath(path), groupid);
	}

	private void chgrp(String path, int groupid) throws IOException,
			NoSuchFileException, AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			_channel.chgrp(groupid, path);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(path);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(path);
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_CHGRP, groupid,
						path), Ex);
			}
		}
	}

	public void chown(Path path, int userid) throws IOException,
			NoSuchFileException, AccessDeniedException {
		chown(convertToUnixPath(path), userid);
	}

	private void chown(String path, int userid) throws IOException,
			NoSuchFileException, AccessDeniedException {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			_channel.chown(userid, path);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(path);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(path);
			} else {
				throw new IOException(Msg.bind(Messages.SftpEx_CHOWN, userid,
						path), Ex);
			}
		}
	}

	public void upload(Path source, Path destination) throws IOException,
			NoSuchFileException, AccessDeniedException {
		upload(source.toString(), convertToUnixPath(destination));
	}

	public void upload(String source, String destination) throws IOException,
			NoSuchFileException, AccessDeniedException {
		if (source == null || source.trim().length() == 0) {
			throw new IllegalArgumentException(source + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (destination == null || destination.trim().length() == 0) {
			throw new IllegalArgumentException(destination + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			_channel.put(source, destination);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(destination);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(destination);
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE
					&& Ex.getCause() != null) {
				throw new IOException(Msg.bind(Messages.SfptEx_PUT, source,
						destination), Ex.getCause());
			} else {
				throw new IOException(Msg.bind(Messages.SfptEx_PUT, source,
						destination), Ex);
			}
		}
	}

	public void download(Path source, Path destination) throws IOException,
			NoSuchFileException, AccessDeniedException {
		download(convertToUnixPath(source), destination.toString());
	}

	public void download(String source, String destination) throws IOException,
			NoSuchFileException, AccessDeniedException {
		if (source == null || source.trim().length() == 0) {
			throw new IllegalArgumentException(source + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (destination == null || destination.trim().length() == 0) {
			throw new IllegalArgumentException(destination + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			_channel.get(source, destination);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				throw new WrapperAccessDeniedException(source);
			} else if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new WrapperNoSuchFileException(source);
			} else if (Ex.id == ChannelSftp.SSH_FX_FAILURE
					&& Ex.getCause() != null) {
				throw new IOException(Msg.bind(Messages.SfptEx_GET, source,
						destination), Ex.getCause());
			} else {
				throw new IOException(Msg.bind(Messages.SfptEx_GET, source,
						destination), Ex);
			}
		}
	}

}