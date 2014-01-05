package com.wat.melody.common.files;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocalFileAttributes implements EnhancedFileAttributes {

	private BasicFileAttributes _attrs;
	private Path _target;
	private BasicFileAttributes _targetAttrs;

	/**
	 * @param attrs
	 *            are the attributes of a link, a file or a directory.
	 * @param target
	 *            if the given attributes describes a link, it is the direct
	 *            link target. Can be <tt>null</tt>, if the given attributes
	 *            doesn't describe a link.
	 * @param targetAttrs
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
	public LocalFileAttributes(BasicFileAttributes attrs, Path target,
			BasicFileAttributes targetAttrs) {
		if (attrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ BasicFileAttributes.class.getCanonicalName() + ".");
		}
		if (attrs.isSymbolicLink() && target == null) {
			throw new IllegalArgumentException("The link's target must be "
					+ "provided.");
		}
		_attrs = attrs;
		_target = target;
		_targetAttrs = targetAttrs;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("is-regular-file:");
		str.append(isRegularFile());
		str.append(", is-directory:");
		str.append(isDirectory());
		str.append(", is-symbolic-link:");
		str.append(isSymbolicLink());
		str.append(", is-other:");
		str.append(isOther());
		str.append(", size:");
		str.append(size());
		str.append(", creation-time:");
		str.append(creationTime());
		str.append(", last-modified-time:");
		str.append(lastModifiedTime());
		str.append(", last-access-time:");
		str.append(lastAccessTime());
		str.append(" }");
		return str.toString();
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	@Override
	public boolean isRegularFile() {
		return _attrs.isRegularFile()
				|| (_targetAttrs != null && _targetAttrs.isRegularFile());
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular directory
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	@Override
	public boolean isDirectory() {
		return _attrs.isDirectory()
				|| (_targetAttrs != null && _targetAttrs.isDirectory());
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
		return _attrs.isSymbolicLink();
	}

	@Override
	public boolean isOther() {
		return _attrs.isOther();
	}

	@Override
	public Path getLinkTarget() {
		return _target;
	}

	@Override
	public FileTime lastAccessTime() {
		return lastAccessTime(true);
	}

	public FileTime lastAccessTime(boolean followLink) {
		return getMetadatas(followLink).lastAccessTime();
	}

	@Override
	public FileTime lastModifiedTime() {
		return lastModifiedTime(true);
	}

	public FileTime lastModifiedTime(boolean followLink) {
		return getMetadatas(followLink).lastModifiedTime();
	}

	@Override
	public FileTime creationTime() {
		return creationTime(true);
	}

	public FileTime creationTime(boolean followLink) {
		return getMetadatas(followLink).creationTime();
	}

	@Override
	public long size() {
		return size(true);
	}

	public long size(boolean followLink) {
		return getMetadatas(followLink).size();
	}

	@Override
	public Object fileKey() {
		return fileKey(true);
	}

	public Object fileKey(boolean followLink) {
		return getMetadatas(followLink).fileKey();
	}

	/**
	 * @param followLink
	 *            if <tt>true</tt> and if this object's path points to a
	 *            symbolic link, natives attributes of the symbolic link's
	 *            target will be returned.
	 * 
	 * @return the natives attributes of this object, or the natives attributes
	 *         of this object's symbolic link's target (see parameter
	 *         followLink).
	 */
	public BasicFileAttributes getMetadatas(boolean followLink) {
		return followLink && isSymbolicLink() ? _targetAttrs : _attrs;
	}

	public BasicFileAttributes getMetadatas() {
		return getMetadatas(true);
	}

}