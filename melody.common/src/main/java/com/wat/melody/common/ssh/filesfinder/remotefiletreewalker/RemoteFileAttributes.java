package com.wat.melody.common.ssh.filesfinder.remotefiletreewalker;

import java.nio.file.Path;
import java.nio.file.Paths;

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
public class RemoteFileAttributes {

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
	public RemoteFileAttributes(SftpATTRS attrs, String target,
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
		_target = (target == null) ? null : Paths.get(target).normalize();
		_realAttrs = realAttrs;
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file
	 *         (follow link). Note that {@link #isLink()} can return
	 *         <tt>true</tt> too.
	 */
	public boolean isFile() {
		return (!_attrs.isDir() && !_attrs.isLink())
				|| (_realAttrs != null && !_realAttrs.isDir());
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular directory
	 *         (follow link). Note that {@link #isLink()} can return
	 *         <tt>true</tt> too.
	 */
	public boolean isDir() {
		return _attrs.isDir() || (_realAttrs != null && _realAttrs.isDir());
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a link (no follow
	 *         link). Note that {@link #isFile()} or {@link #isDir()} can return
	 *         <tt>true</tt> too. Also note that if this returns <tt>true</tt>,
	 *         and if {@link #isFile()} and {@link #isDir()} both return
	 *         <tt>false</tt>, then it means that this object's path points to a
	 *         link, and this link's target is a non existent file or directory.
	 */
	public boolean isLink() {
		return _attrs.isLink();
	}

	public Path getLinkTarget() {
		return _target;
	}

	public int getATime() {
		return _attrs.getATime();
	}

	public String getAtimeString() {
		return _attrs.getAtimeString();
	}

	public int getMTime() {
		return _attrs.getMTime();
	}

	public String getMtimeString() {
		return _attrs.getMtimeString();
	}

	public int getPermissions() {
		return _attrs.getPermissions();
	}

	public String getPermissionsString() {
		return _attrs.getPermissionsString();
	}

	public long getSize() {
		return _attrs.getSize();
	}

	public int getUId() {
		return _attrs.getUId();
	}

	public int getGId() {
		return _attrs.getGId();
	}

}