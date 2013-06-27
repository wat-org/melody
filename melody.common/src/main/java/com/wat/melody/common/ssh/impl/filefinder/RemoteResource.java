package com.wat.melody.common.ssh.impl.filefinder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jcraft.jsch.SftpATTRS;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.LinkOption;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.TransferBehavior;
import com.wat.melody.common.ssh.types.filesfinder.Resource;
import com.wat.melody.common.ssh.types.filesfinder.ResourceSpecification;
import com.wat.melody.common.ssh.types.filesfinder.ResourcesSelector;

/**
 * <p>
 * A {@link RemoteResource} describe a single file or directory which was found
 * with {@link RemoteResourcesFinder#findResources(ResourcesSelector)}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class RemoteResource implements Resource {

	private LocalResource _localResource;
	private SftpATTRS _remoteAttrs;

	public RemoteResource(Path path, SftpATTRS remoteAttrs, ResourcesSelector rs) {
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

	public File getLocalBaseDir() {
		return getLocalResource().getLocalBaseDir();
	}

	public String getRemoteBaseDir() {
		return getLocalResource().getRemoteBaseDir();
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