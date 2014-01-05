package com.wat.melody.common.cifs.transfer;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import com.wat.melody.common.files.EnhancedFileAttributes;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class CifsFileAttributes implements EnhancedFileAttributes {

	public static CifsFileAttributes createCifsFileAttributesFromSmbFile(
			SmbFile smbfile) throws SmbException {
		if (smbfile == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + SmbFile.class.getCanonicalName()
					+ ".");
		}
		return new CifsFileAttributes(smbfile.getType(),
				smbfile.lastModified(), smbfile.createTime(), smbfile.length(),
				smbfile.getAttributes());
	}

	private int _type;
	private long _lastModifiedTime;
	private long _creationTime;
	private long _size;
	private int _attributes;

	public CifsFileAttributes(int type, long lastModifiedTime,
			long creationTime, long size, int attributes) {
		_type = type;
		_lastModifiedTime = lastModifiedTime;
		_creationTime = creationTime;
		_size = size;
		_attributes = attributes;
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

	@Override
	public boolean isRegularFile() {
		/*
		 * holy shit ! there's a special rule in SmbFile.isDirectory, which
		 * return false if getUncPath0().length() == 1. We have to do equal
		 * stuff here.
		 */
		if ((_type & (SmbFile.TYPE_WORKGROUP | SmbFile.TYPE_SERVER | SmbFile.TYPE_SHARE)) != 0) {
			return false;
		}
		return (_attributes & SmbFile.ATTR_DIRECTORY) == 0;
	}

	@Override
	public boolean isDirectory() {
		/*
		 * holy shit ! there's a special rule in SmbFile.isDirectory, which
		 * return true if getUncPath0().length() == 1. We have to do equal stuff
		 * here.
		 */
		if ((_type & (SmbFile.TYPE_WORKGROUP | SmbFile.TYPE_SERVER | SmbFile.TYPE_SHARE)) != 0) {
			return true;
		}
		return (_attributes & SmbFile.ATTR_DIRECTORY) == SmbFile.ATTR_DIRECTORY;
	}

	@Override
	public boolean isSymbolicLink() {
		// Samba doesn't handle links
		return false;
	}

	@Override
	public boolean isOther() {
		return false;
	}

	@Override
	public Path getLinkTarget() {
		// Samba doesn't handle links
		return null;
	}

	@Override
	public FileTime lastModifiedTime() {
		return FileTime.fromMillis(_lastModifiedTime);
	}

	public FileTime lastModifiedTime(boolean followLink) {
		return FileTime.fromMillis(_lastModifiedTime);
	}

	@Override
	public FileTime lastAccessTime() {
		// Samba doesn't handle links
		return null;
	}

	public FileTime lastAccessTime(boolean followLink) {
		// Samba doesn't handle links
		return null;
	}

	@Override
	public FileTime creationTime() {
		return FileTime.fromMillis(_creationTime);
	}

	public FileTime creationTime(boolean followLink) {
		return FileTime.fromMillis(_creationTime);
	}

	@Override
	public long size() {
		return _size;
	}

	public long size(boolean followLink) {
		return _size;
	}

	@Override
	public Object fileKey() {
		// No way to deal with fileKey with CIFS
		return null;
	}

	public Object fileKey(boolean followLink) {
		// No way to deal with fileKey with CIFS
		return null;
	}

}