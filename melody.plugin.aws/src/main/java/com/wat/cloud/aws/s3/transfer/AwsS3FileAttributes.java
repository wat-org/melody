package com.wat.cloud.aws.s3.transfer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import com.amazonaws.services.s3.Headers;
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

	protected static boolean isClientSideEncryptedWithMetadatas(
			ObjectMetadata metadatas) {
		return metadatas.getUserMetadata().get(Headers.CRYPTO_IV) != null
				&& metadatas.getUserMetadata().get(Headers.CRYPTO_KEY) != null
				&& metadatas.getUserMetadata().get(
						Headers.MATERIALS_DESCRIPTION) != null;
	}

	protected static long getUnencryptedSize(ObjectMetadata metadatas)
			throws NumberFormatException {
		return Long.parseLong(metadatas.getUserMetadata().get(
				Headers.UNENCRYPTED_CONTENT_LENGTH));
	}

	/*
	 * Amazon S3 Console associates a meta data 'x-amz-s3-console-folder=true'
	 * when creating a directory. But, when reading such directory, the meta
	 * data 'x-amz-s3-console-folder' is invisible... So we can't be compatible
	 * with Amazon S3 Console.
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

	public FileTime lastModifiedTime(boolean followLink) {
		Date date = getMetadatas(followLink).getLastModified();
		return date == null ? null : FileTime.fromMillis(date.getTime());
	}

	@Override
	public FileTime lastModifiedTime() {
		return lastModifiedTime(true);
	}

	@Override
	public FileTime creationTime() {
		// not supported
		return null;
	}

	/**
	 * @return the length of the unencrypted object. Note that if the object was
	 *         uploaded with client-side-encryption, this method and
	 *         {@link #encryptedSize(boolean)} will return different values.
	 */
	public long size(boolean followLink) {
		long size = getMetadatas(followLink).getContentLength();
		/*
		 * if uploaded with client-side encryption + meta data , try to get the
		 * original size (e.g. before the content was encrypted)
		 */
		if (isClientSideEncryptedWithMetadatas(getMetadatas(followLink))) {
			try {
				size = getUnencryptedSize(getMetadatas(followLink));
			} catch (NumberFormatException ignored) {
				// get the _metadatas.getContentLength()
			}
		}
		return size;
	}

	/**
	 * @return the length of the unencrypted object. Note that if the object was
	 *         uploaded with client-side-encryption, this method and
	 *         {@link #encryptedSize()} will return different values.
	 */
	@Override
	public long size() {
		return size(true);
	}

	@Override
	public Object fileKey() {
		// No way to deal with fileKey with S3FS
		return null;
	}

	public Object fileKey(boolean followLink) {
		// No way to deal with fileKey with S3FS
		return null;
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
	public ObjectMetadata getMetadatas(boolean followLink) {
		return followLink && isSymbolicLink() ? _targetMetadatas : _metadatas;
	}

	public ObjectMetadata getMetadatas() {
		return getMetadatas(true);
	}

	/**
	 * @return the length of the encrypted object (the content of the
	 *         content-length header). Note that if the object was uploaded with
	 *         client-side-encryption, this method and {@link #size(boolean)}
	 *         will return different values.
	 */
	public long encryptedSize(boolean followLink) {
		return getMetadatas(followLink).getContentLength();
	}

	/**
	 * @return the length of the encrypted object (the content of the
	 *         content-length header). Note that if the object was uploaded with
	 *         client-side-encryption, this method and {@link #size()} will
	 *         return different values.
	 */
	public long encryptedSize() {
		return encryptedSize(true);
	}

}