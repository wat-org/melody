package com.wat.melody.common.ssh.filesfinder;

import java.nio.file.Path;

import com.jcraft.jsch.SftpATTRS;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.LinkOption;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.TransferBehavior;

/**
 * <p>
 * A {@link RemoteResource} describe a single file or directory which was found
 * with {@link RemoteResourcesFinder#findResources(ResourcesSpecification)}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class RemoteResource implements Resource {

	private LocalResource _localResource;
	private SftpATTRS _remoteAttrs;

	public RemoteResource(Path path, SftpATTRS remoteAttrs,
			ResourcesSpecification rs) {
		setLocalResource(new LocalResource(path, rs));
		setRemoteAttrs(remoteAttrs);
	}

	@Override
	public Path getPath() {
		return getLocalResource().getPath();
	}

	@Override
	public ResourceSpecification setResourceSpecification(
			ResourceSpecification spec) {
		return getLocalResource().setResourceSpecification(spec);
	}

	public String getSrcBaseDir() {
		return getLocalResource().getSrcBaseDir();
	}

	public String getDestBaseDir() {
		return getLocalResource().getDestBaseDir();
	}

	public String getMatch() {
		return getLocalResource().getMatch();
	}

	public Modifiers getFileModifiers() {
		return getLocalResource().getFileModifiers();
	}

	public Modifiers getDirModifiers() {
		return getLocalResource().getDirModifiers();
	}

	public LinkOption getLinkOption() {
		return getLocalResource().getLinkOption();
	}

	public TransferBehavior getTransferBehavior() {
		return getLocalResource().getTransferBehavior();
	}

	public boolean getTemplate() {
		return getLocalResource().getTemplate();
	}

	public GroupID getGroup() {
		return getLocalResource().getGroup();
	}

	public String getDestPath() {
		return getLocalResource().getDestPath();
	}

	/**
	 * @return <tt>true</tt> if this object's path is a directory. Note that if
	 *         neither {@link #isLink()} nor {@link #isDir()} returns
	 *         <tt>true</tt>, then it means that this object's path is a file.
	 */
	public boolean isDir() {
		return getRemoteAttrs().isDir();
	}

	/**
	 * @return <tt>true</tt> if this object's path is a link. Note that if
	 *         neither {@link #isLink()} nor {@link #isDir()} returns
	 *         <tt>true</tt>, then it means that this object's path is a file.
	 */
	public boolean isLink() {
		return getRemoteAttrs().isLink();
	}

	/**
	 * <pre>
	 * Sample
	 * 
	 * src-basedir = /src/basedir
	 * path        = /src/basedir/dir3/dir4/file.txt
	 * 
	 * will return   dir3/dir4/file.txt
	 * </pre>
	 * 
	 * @return a {@link Path} which, when resolved from the src-basedir, is
	 *         equal to this object's path.
	 */
	protected Path getRelativePath() {
		return getLocalResource().getRelativePath();
	}

	/**
	 * <pre>
	 * Sample
	 * 
	 * src-basedir  = /src/basedir
	 * path         = /src/basedir/dir3/dir4/file.txt
	 * dest-basedir = /dest/basedir
	 * 
	 * will return    /dest/basedir/dir3/dir4/file.txt
	 * </pre>
	 * 
	 * @return the destination {@link Path} of this object (a relative or
	 *         absolute path, depending the if the dest-basedir is relative or
	 *         absolute).
	 */
	public Path getDestination() {
		return getLocalResource().getDestination();
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
		str.append(getPath());
		str.append(", destination:");
		str.append(getDestination());
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
					&& getSrcBaseDir().equals(sr.getSrcBaseDir());
		}
		return false;
	}

	private LocalResource getLocalResource() {
		return _localResource;
	}

	private LocalResource setLocalResource(LocalResource localResource) {
		if (localResource == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ LocalResource.class.getCanonicalName() + ".");
		}
		LocalResource previous = getLocalResource();
		_localResource = localResource;
		return previous;
	}

	private SftpATTRS getRemoteAttrs() {
		return _remoteAttrs;
	}

	private SftpATTRS setRemoteAttrs(SftpATTRS attrs) {
		if (attrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + SftpATTRS.class.getCanonicalName()
					+ ".");
		}
		SftpATTRS previous = getRemoteAttrs();
		_remoteAttrs = attrs;
		return previous;
	}

}