package com.wat.melody.common.ssh.filesfinder;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import com.jcraft.jsch.SftpATTRS;

/**
 * <p>
 * Contains a remote link, file or directory attributes (size, access time,
 * modification time, user id, group id and permissions)
 * </p>
 * 
 * <p>
 * if it describes a link, also give informations about this link's target and
 * the real file or directory attributes.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class RemoteFileAttributes implements EnhancedFileAttributes {

	// TODO : move this in ssh.impls, because it is using JSch
	private SftpATTRS _attrs;
	private Path _target;
	private SftpATTRS _realAttrs;

	/**
	 * @param attrs
	 *            are the attributes of a link, a file or a directory.
	 * @param target
	 *            if the given attributes describes a link, it is the direct
	 *            link target. Can be <tt>null</tt>, if the given attributes
	 *            doesn't describe a link.
	 * @param realAttrs
	 *            if the given attributes describes a link, it is the final file
	 *            or directory target attributes. Can be <tt>null</tt>, if the
	 *            given link points - at any degree - to a non existing file or
	 *            directory.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given attributes are <tt>null</tt> ;</li>
	 *             <li>if the given attributes describe a link and the given
	 *             direct target is <tt>null</tt> ;</li>
	 *             </ul>
	 */
	public RemoteFileAttributes(SftpATTRS attrs, Path target,
			SftpATTRS realAttrs) {
		if (attrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + SftpATTRS.class.getCanonicalName()
					+ ".");
		}
		if (attrs.isLink() && target == null) {
			throw new IllegalArgumentException("The link's target must be "
					+ "provided.");
		}
		_attrs = attrs;
		_target = target;
		_realAttrs = realAttrs;
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	@Override
	public boolean isRegularFile() {
		return (!_attrs.isDir() && !_attrs.isLink())
				|| (_realAttrs != null && !_realAttrs.isDir());
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular directory
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	@Override
	public boolean isDirectory() {
		return _attrs.isDir() || (_realAttrs != null && _realAttrs.isDir());
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a link (no follow
	 *         link). Note that {@link #isRegularFile()} or
	 *         {@link #isDirectory()} can return <tt>true</tt> too. Also note
	 *         that if this returns <tt>true</tt>, and if
	 *         {@link #isRegularFile()} and {@link #isDirectory()} both return
	 *         <tt>false</tt>, then it means that this object's path points to a
	 *         link, and this link's target is a non existent file or directory.
	 */
	@Override
	public boolean isSymbolicLink() {
		return _attrs.isLink();
	}

	@Override
	public boolean isOther() {
		// TODO
		return false;
	}

	@Override
	public Path getLinkTarget() {
		return _target;
	}

	@Override
	public FileTime lastAccessTime() {
		return FileTime.fromMillis(_attrs.getATime()*1000L);
	}

	@Override
	public FileTime lastModifiedTime() {
		return FileTime.fromMillis(_attrs.getMTime()*1000L);
	}

	@Override
	public FileTime creationTime() {
		// TODO
		return null;
	}

	@Override
	public long size() {
		return _attrs.getSize();
	}

	@Override
	public Object fileKey() {
		return null;
	}

}