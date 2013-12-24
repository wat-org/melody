package com.wat.cloud.aws.s3.transfer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.wat.melody.common.files.EnhancedFileAttributes;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsS3FileAttributes implements EnhancedFileAttributes {

	protected static boolean isFile(ObjectMetadata metadatas) {
		return !isDir(metadatas) && !isLink(metadatas);
	}

	protected static boolean isDir(ObjectMetadata metadatas) {
		return metadatas.getUserMetadata().get(DIRECTORY_FLAG) != null;
	}

	protected static boolean isLink(ObjectMetadata metadatas) {
		return metadatas.getUserMetadata().get(SYMBOLIC_LINK_FLAG) != null;
	}

	protected static String readLink(ObjectMetadata metadatas) {
		return metadatas.getUserMetadata().get(SYMBOLIC_LINK_FLAG);
	}

	/*
	 * Amazon S3 Console, which associate a meta data
	 * 'x-amz-s3-console-folder=true' when creating a directory. But, when
	 * reading such directory, the meta data 'x-amz-s3-console-folder' is
	 * invisible... So we can't be compatible with Amazon S3 Console.
	 */
	public static String DIRECTORY_FLAG = "dir";
	public static String SYMBOLIC_LINK_FLAG = "x-amz-website-redirect-location";

	private ObjectMetadata _metadatas;
	private ObjectMetadata _targetMetadatas;

	public AwsS3FileAttributes(ObjectMetadata metadatas,
			ObjectMetadata targetMetadatas) {
		if (metadatas == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ObjectMetadata.class.getCanonicalName() + ".");
		}
		_metadatas = metadatas;
		_targetMetadatas = targetMetadatas;
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
		return (!isDir(_metadatas) && !isSymbolicLink())
				|| (_targetMetadatas != null && !isDir(_targetMetadatas));
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular directory
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	@Override
	public boolean isDirectory() {
		return isDir(_metadatas)
				|| (_targetMetadatas != null && isDir(_targetMetadatas));
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
		return isLink(_metadatas);
	}

	@Override
	public boolean isOther() {
		return false;
	}

	@Override
	public Path getLinkTarget() {
		String target = _metadatas.getUserMetadata().get(SYMBOLIC_LINK_FLAG);
		return target == null ? null : Paths.get(target);
	}

	@Override
	public FileTime lastAccessTime() {
		// not supported
		return null;
	}

	@Override
	public FileTime lastModifiedTime() {
		Date date = _metadatas.getLastModified();
		return date == null ? null : FileTime.fromMillis(date.getTime());
	}

	@Override
	public FileTime creationTime() {
		// not supported
		return null;
	}

	@Override
	public long size() {
		return _metadatas.getContentLength();
	}

	@Override
	public Object fileKey() {
		// not supported
		return null;
	}

	/**
	 * @param followLink
	 *            if <tt>true</tt> and if this object's path points to a
	 *            symbolic link, metadata of the symbolic link's target will be
	 *            returned.
	 * 
	 * @return the metadatas of this object, or the metadatas of this object's
	 *         symbolic link's target (see parameter followLink).
	 */
	public ObjectMetadata getMetadatas(boolean followLink) {
		return followLink && isSymbolicLink() ? _targetMetadatas : _metadatas;
	}

	public ObjectMetadata getMetadatas() {
		return getMetadatas(false);
	}

}