package com.wat.melody.common.ssh.types;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jcraft.jsch.SftpATTRS;

public class RemoteResource {

	private LocalResource _simpleResource;
	private SftpATTRS _remoteAttrs;

	public RemoteResource(Path path, SftpATTRS remoteAttrs,
			ResourceMatcher matcher) {
		setSimpleResource(new LocalResource(path, matcher));
		setRemoteAttrs(remoteAttrs);
	}

	public Path getPath() {
		return getSimpleResource().getPath();
	}

	public File getLocalBaseDir() {
		return getSimpleResource().getLocalBaseDir();
	}

	public String getRemoteBaseDir() {
		return getSimpleResource().getRemoteBaseDir();
	}

	public Modifiers getFileModifiers() {
		return getSimpleResource().getFileModifiers();
	}

	public Modifiers getDirModifiers() {
		return getSimpleResource().getDirModifiers();
	}

	public LinkOption getLinkOption() {
		return getSimpleResource().getLinkOption();
	}

	public TransferBehavior getTransferBehavior() {
		return getSimpleResource().getTransferBehavior();
	}

	public boolean getTemplate() {
		return getSimpleResource().getTemplate();
	}

	public GroupID getGroup() {
		return getSimpleResource().getGroup();
	}

	public boolean isDir() {
		return getRemoteAttrs().isDir();
	}

	public boolean isLink() {
		return getRemoteAttrs().isLink();
	}

	/**
	 * <pre>
	 * Sample
	 * 
	 * remote-basedir = /tmp
	 * path           = /tmp/dir3/dir4/file.txt
	 * 
	 * will return      dir3/dir4/file.txt
	 * </pre>
	 * 
	 * @return a {@link Path} which, when resolved from the remote-basedir, is
	 *         equal to the absolute path of this object.
	 */
	public Path getRelativePath() {
		return Paths.get(getRemoteBaseDir()).relativize(getPath());
	}

	/**
	 * <pre>
	 * Sample
	 * 
	 * local-basedir  = /home/user/dir1/dir2
	 * path           = /tmp/dir3/dir4/file.txt
	 * remote-basedir = /tmp
	 * 
	 * will return      /home/user/dir1/dir2/dir3/dir4/file.txt
	 * </pre>
	 * 
	 * <p>
	 * <i> * The resulting path will be an absolute path, because the
	 * local-basedir is absolute ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @return the destination {@link Path} of this object.
	 */
	public Path getDestination() {
		return Paths.get(getLocalBaseDir().getPath())
				.resolve(getRelativePath());
	}

	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		if (isLink()) {
			str.append("link:");
		} else if (isDir()) {
			str.append("directory:");
		} else {
			str.append("file:");
		}
		str.append(getRelativePath());
		str.append(", remote-basedir:");
		str.append(getRemoteBaseDir());
		str.append(", local-basedir:");
		str.append(getLocalBaseDir());
		str.append(", file-modifiers:");
		str.append(getFileModifiers());
		str.append(", dir-modifiers:");
		str.append(getDirModifiers());
		str.append(", group:");
		str.append(getGroup());
		str.append(", link-option:");
		str.append(getLinkOption());
		str.append(", transfer-behavior:");
		str.append(getTransferBehavior());
		str.append(", is-template:");
		str.append(getTemplate());
		str.append(" }");
		return str.toString();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof RemoteResource) {
			RemoteResource sr = (RemoteResource) anObject;
			return getPath().equals(sr.getPath())
					&& getLocalBaseDir().equals(sr.getLocalBaseDir());
		}
		return false;
	}

	public ResourceMatcher setResourceMatcher(ResourceMatcher resourceMatcher) {
		return getSimpleResource().setResourceMatcher(resourceMatcher);
	}

	private LocalResource getSimpleResource() {
		return _simpleResource;
	}

	private LocalResource setSimpleResource(LocalResource path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ LocalResource.class.getCanonicalName() + ".");
		}
		LocalResource previous = getSimpleResource();
		_simpleResource = path;
		return previous;
	}

	private SftpATTRS getRemoteAttrs() {
		return _remoteAttrs;
	}

	private SftpATTRS setRemoteAttrs(SftpATTRS rm) {
		if (rm == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + SftpATTRS.class.getCanonicalName()
					+ ".");
		}
		SftpATTRS previous = getRemoteAttrs();
		_remoteAttrs = rm;
		return previous;
	}

}