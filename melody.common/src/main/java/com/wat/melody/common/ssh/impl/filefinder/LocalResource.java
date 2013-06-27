package com.wat.melody.common.ssh.impl.filefinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.LinkOption;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.TransferBehavior;
import com.wat.melody.common.ssh.types.filesfinder.Resource;
import com.wat.melody.common.ssh.types.filesfinder.ResourceSpecification;
import com.wat.melody.common.ssh.types.filesfinder.ResourcesSelector;

/**
 * <p>
 * A {@link LocalResource} describe a single file or directory which was found
 * with {@link LocalResourcesFinder#findResources(ResourcesSelector)}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocalResource implements Resource {

	private Path _path;
	private ResourcesSelector _resourcesSelector;
	private ResourceSpecification _resourceSpecification;

	/**
	 * @param path
	 *            is the path of a file or directory.
	 * @param rs
	 *            is the {@link ResourcesSelector} which was used to find this
	 *            file or directory.
	 */
	public LocalResource(Path path, ResourcesSelector rs) {
		super();
		setPath(path);
		setResourcesSelector(rs);
		setResourceSpecification(rs);
	}

	public File getLocalBaseDir() {
		return getResourcesSelector().getLocalBaseDir();
	}

	public String getRemoteBaseDir() {
		return getResourcesSelector().getRemoteBaseDir();
	}

	public String getMatch() {
		return getResourceSpecification().getMatch();
	}

	public Modifiers getFileModifiers() {
		return getResourceSpecification().getFileModifiers();
	}

	public Modifiers getDirModifiers() {
		return getResourceSpecification().getDirModifiers();
	}

	public LinkOption getLinkOption() {
		return getResourceSpecification().getLinkOption();
	}

	public TransferBehavior getTransferBehavior() {
		return getResourceSpecification().getTransferBehavior();
	}

	public boolean getTemplate() {
		return getResourceSpecification().getTemplate();
	}

	public GroupID getGroup() {
		return getResourceSpecification().getGroup();
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file,
	 *         directory, or link (follow link). Note that if this object's path
	 *         is a symbolic link which points to nothing, this will return
	 *         <tt>false</tt>.
	 */
	public boolean exists() {
		return Files.exists(getPath());
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file,
	 *         directory, or link (no follow link). Note that if this object's
	 *         path is a symbolic link which points to nothing, this will return
	 *         <tt>true</tt>.
	 */
	public boolean lexists() {
		return Files.exists(getPath(), java.nio.file.LinkOption.NOFOLLOW_LINKS);
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	public boolean isFile() {
		return Files.isRegularFile(getPath());
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular directory
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	public boolean isDirectory() {
		return Files.isDirectory(getPath());
	}

	/**
	 * @return <tt>true</tt> if this object's path directly points to a link.
	 *         Note that {@link #isFile()} or {@link #isDirectory()} can return
	 *         <tt>true</tt> too.
	 */
	public boolean isSymbolicLink() {
		return Files.isSymbolicLink(getPath());
	}

	public Path getSymbolicLinkTarget() throws IOException {
		return Files.readSymbolicLink(getPath());
	}

	/**
	 * <pre>
	 * Sample
	 * 
	 * local-basedir = /home/user/dir1/dir2
	 * path          = /home/user/dir1/dir2/dir3/dir4/file.txt
	 * 
	 * will return     dir3/dir4/file.txt
	 * </pre>
	 * 
	 * @return a {@link Path} which, when resolved from the local-basedir, is
	 *         equal to the path of this object.
	 */
	protected Path getRelativePath() {
		return Paths.get(getLocalBaseDir().getPath()).relativize(getPath());
	}

	/**
	 * <pre>
	 * Sample
	 * 
	 * local-basedir  = /home/user/dir1/dir2
	 * path           = /home/user/dir1/dir2/dir3/dir4/file.txt
	 * remote-basedir = /tmp
	 * 
	 * will return      /tmp/dir3/dir4/file.txt
	 * </pre>
	 * 
	 * <p>
	 * <i> * The resulting path can be a relative or absolute path, depending
	 * the if the remote-basedir is relative or absolute ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @return the destination {@link Path} of this object.
	 */
	public Path getDestination() {
		return Paths.get(getRemoteBaseDir()).resolve(getRelativePath());
	}

	/**
	 * @return {@code true} if this link's target doesn't points outside of the
	 *         local-basedir and is not an absolute path.
	 * 
	 * @throws IOException
	 *             if an error occurred while retrieving the link's target.
	 */
	public boolean isSafeLink() throws IOException {
		Path symTarget = getSymbolicLinkTarget();
		if (symTarget.isAbsolute()) {
			return false;
		}
		int refLength = Paths.get(getLocalBaseDir().getPath()).normalize()
				.getNameCount();
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
		str.append(getRelativePath());
		str.append(", local-basedir:");
		str.append(getLocalBaseDir());
		str.append(", remote-basedir:");
		str.append(getRemoteBaseDir());
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
		if (anObject instanceof LocalResource) {
			LocalResource sr = (LocalResource) anObject;
			return getPath().equals(sr.getPath())
					&& getRemoteBaseDir().equals(sr.getRemoteBaseDir());
		}
		return false;
	}

	@Override
	public Path getPath() {
		return _path;
	}

	private Path setPath(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName()
					+ " (a file or directory path, relative or absolute).");
		}
		Path previous = getPath();
		_path = path;
		return previous;
	}

	public ResourcesSelector getResourcesSelector() {
		return _resourcesSelector;
	}

	private ResourcesSelector setResourcesSelector(
			ResourcesSelector resourceMatcher) {
		if (resourceMatcher == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ResourcesSelector.class.getCanonicalName() + ".");
		}
		ResourcesSelector previous = getResourcesSelector();
		_resourcesSelector = resourceMatcher;
		return previous;
	}

	public ResourceSpecification getResourceSpecification() {
		return _resourceSpecification;
	}

	@Override
	public ResourceSpecification setResourceSpecification(
			ResourceSpecification resourceSpecification) {
		if (resourceSpecification == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ResourceSpecification.class.getCanonicalName() + ".");
		}
		ResourceSpecification previous = getResourceSpecification();
		_resourceSpecification = resourceSpecification;
		return previous;
	}

}