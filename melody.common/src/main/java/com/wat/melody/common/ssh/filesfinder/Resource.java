package com.wat.melody.common.ssh.filesfinder;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.LinkOption;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.TransferBehavior;

/**
 * <p>
 * A {@link Resource} describe a single path (a file,directory or link), some
 * basic attributes (size, last access time, ...) and the transfer directive. .
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Resource {

	private Path _path;
	private ResourcesSpecification _resourcesSpecification;
	private ResourceSpecification _resourceSpecification;
	private EnhancedFileAttributes _attrs;

	/**
	 * @param path
	 *            is the path of a file or directory.
	 * @param rs
	 *            is the {@link ResourcesSpecification} which was used to find
	 *            this file or directory.
	 */
	public Resource(Path path, EnhancedFileAttributes attrs,
			ResourcesSpecification rs) {
		setPath(path);
		setResourcesSpecification(rs);
		setResourceSpecification(rs);
		setAttributes(attrs);
	}

	public String getSrcBaseDir() {
		return getResourcesSpecification().getSrcBaseDir();
	}

	public String getDestBaseDir() {
		return getResourcesSpecification().getDestBaseDir();
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

	public String getDestPath() {
		return getResourceSpecification().getDestPath();
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
	 *         equal to the path of this object.
	 */
	protected Path getRelativePath() {
		return Paths.get(getSrcBaseDir()).relativize(getPath());
	}

	/**
	 * <pre>
	 * Sample
	 * 
	 * src-basedir  = /src/basedir
	 * path         = /src/basedir/dir3/dir4/file.txt
	 * dest-basedir = /dest/basedir
	 * dest-path    = null
	 * 
	 * will return    /dest/basedir/dir3/dir4/file.txt
	 * </pre>
	 * 
	 * <pre>
	 * Sample
	 * 
	 * src-basedir  = /src/basedir
	 * path         = /src/basedir/dir3/dir4/file.txt
	 * dest-basedir = /dest/basedir
	 * dest-path    = /super/directory/renamed.txt
	 * 
	 * will return    /dest/basedir/super/directory/renamed.txt
	 * </pre>
	 * 
	 * @return the destination {@link Path} of this object (a relative or
	 *         absolute path, depending the if the dest-basedir is relative or
	 *         absolute).
	 */
	public Path getDestination() {
		if (getDestPath() != null) {
			return Paths.get(getDestBaseDir()).resolve(getDestPath());
		}
		return Paths.get(getDestBaseDir()).resolve(getRelativePath());
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file,
	 *         directory, or link (follow link). Note that if this object's path
	 *         is a symbolic link which points to nothing, this will return
	 *         <tt>false</tt>.
	 */
	public boolean exists() {
		return isRegularFile() || isDirectory();
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	public boolean isRegularFile() {
		return getAttributes().isRegularFile();
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular directory
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	public boolean isDirectory() {
		return getAttributes().isDirectory();
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
	public boolean isSymbolicLink() {
		return getAttributes().isSymbolicLink();
	}

	public Path getSymbolicLinkTarget() {
		return getAttributes().getLinkTarget();
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
		if (anObject instanceof Resource) {
			Resource sr = (Resource) anObject;
			return getPath().equals(sr.getPath())
					&& getDestBaseDir().equals(sr.getDestBaseDir());
		}
		return false;
	}

	public EnhancedFileAttributes getAttributes() {
		return _attrs;
	}

	private EnhancedFileAttributes setAttributes(EnhancedFileAttributes attrs) {
		if (attrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ EnhancedFileAttributes.class.getCanonicalName() + ".");
		}
		EnhancedFileAttributes previous = getAttributes();
		_attrs = attrs;
		return previous;
	}

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

	public ResourcesSpecification getResourcesSpecification() {
		return _resourcesSpecification;
	}

	private ResourcesSpecification setResourcesSpecification(
			ResourcesSpecification resourceMatcher) {
		if (resourceMatcher == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ResourcesSpecification.class.getCanonicalName() + ".");
		}
		ResourcesSpecification previous = getResourcesSpecification();
		_resourcesSpecification = resourceMatcher;
		return previous;
	}

	public ResourceSpecification getResourceSpecification() {
		return _resourceSpecification;
	}

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