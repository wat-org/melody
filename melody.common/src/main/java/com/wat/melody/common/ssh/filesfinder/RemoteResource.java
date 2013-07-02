package com.wat.melody.common.ssh.filesfinder;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ssh.filesfinder.remotefiletreewalker.RemoteFileAttributes;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.LinkOption;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.TransferBehavior;

/**
 * <p>
 * A {@link RemoteResource} describe a single file or directory which was found
 * with
 * {@link RemoteResourcesFinder#findResources(ChannelSftp, ResourcesSpecification)}
 * .
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class RemoteResource implements Resource {

	private LocalResource _localResource;
	private RemoteFileAttributes _remoteAttrs;

	public RemoteResource(Path path, RemoteFileAttributes remoteAttrs,
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
	 * @return <tt>true</tt> if this object's path points to a regular file,
	 *         directory, or link (follow link). Note that if this object's path
	 *         is a symbolic link which points to nothing, this will return
	 *         <tt>false</tt>.
	 */
	public boolean exists() {
		return isFile() || isDirectory();
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file,
	 *         directory, or link (no follow link). Note that if this object's
	 *         path is a symbolic link which points to nothing, this will return
	 *         <tt>true</tt>.
	 */
	public boolean lexists() {
		return true;
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	public boolean isFile() {
		return getRemoteAttrs().isFile();
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular directory
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	public boolean isDirectory() {
		return getRemoteAttrs().isDir();
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a link (no follow
	 *         link). Note that {@link #isFile()} or {@link #isDirectory()} can
	 *         return <tt>true</tt> too. Also note that if this returns
	 *         <tt>true</tt>, and if {@link #isFile()} and
	 *         {@link #isDirectory()} both return <tt>false</tt>, then it means
	 *         that this object's path points to a link, and this link's target
	 *         is a non existent file or directory.
	 */
	public boolean isSymbolicLink() {
		return getRemoteAttrs().isLink();
	}

	public Path getSymbolicLinkTarget() {
		return getRemoteAttrs().getLinkTarget();
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

	/**
	 * @return {@code true} if this link's target doesn't points outside of the
	 *         src-basedir and is not an absolute path.
	 */
	public boolean isSafeLink() {
		Path symTarget = getSymbolicLinkTarget();
		if (symTarget.isAbsolute()) {
			return false;
		}
		int refLength = Paths.get(getSrcBaseDir()).normalize().getNameCount();
		Path computed = getPath().getParent();
		for (Path p : symTarget) {
			computed = computed.resolve(p).normalize();
			if (computed.getNameCount() < refLength) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		if (isSymbolicLink()) {
			str.append("link:");
		} else if (isDirectory()) {
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
					&& getDestBaseDir().equals(sr.getDestBaseDir());
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

	private RemoteFileAttributes getRemoteAttrs() {
		return _remoteAttrs;
	}

	private RemoteFileAttributes setRemoteAttrs(RemoteFileAttributes attrs) {
		if (attrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ RemoteFileAttributes.class.getCanonicalName() + ".");
		}
		RemoteFileAttributes previous = getRemoteAttrs();
		_remoteAttrs = attrs;
		return previous;
	}

}