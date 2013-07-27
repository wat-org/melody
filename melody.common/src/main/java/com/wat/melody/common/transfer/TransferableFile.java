package com.wat.melody.common.transfer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.transfer.resources.ResourceAttribute;
import com.wat.melody.common.transfer.resources.ResourceSpecification;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * <p>
 * A {@link TransferableFile} describe a single path (a regular file, directory
 * or link), some basic attributes (size, last access time, ...) and the
 * transfer directive.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferableFile implements Transferable {

	private Path _sourcePath;
	private ResourcesSpecification _resourcesSpecification;
	private ResourceSpecification _resourceSpecification;
	private EnhancedFileAttributes _attrs;

	/**
	 * @param sourcePath
	 *            is the path of a regular file, directory or link.
	 * @param rs
	 *            is the {@link ResourcesSpecification} which was used to find
	 *            this regular file, directory or link.
	 */
	public TransferableFile(Path sourcePath, EnhancedFileAttributes attrs,
			ResourcesSpecification rs) {
		setSourcePath(sourcePath);
		setResourcesSpecification(rs);
		setResourceSpecification(rs);
		setAttributes(attrs);
	}

	private String getSrcBaseDir() {
		return getResourcesSpecification().getSrcBaseDir();
	}

	private String getDestBaseDir() {
		return getResourcesSpecification().getDestBaseDir();
	}

	@Override
	public LinkOption getLinkOption() {
		return getResourceSpecification().getLinkOption();
	}

	@Override
	public TransferBehavior getTransferBehavior() {
		return getResourceSpecification().getTransferBehavior();
	}

	@Override
	public boolean getTemplate() {
		return getResourceSpecification().getTemplate();
	}

	@Override
	public FileAttribute<?>[] getExpectedAttributes() {
		if (isSymbolicLink()) {
			return null;
		}
		if (isDirectory()) {
			return getResourceSpecification().getDirExpectedAttributes();
		}
		return getResourceSpecification().getFileExpectedAttributes();
	}

	private Collection<ResourceAttribute> getExpectedAttributesAsList() {
		if (isSymbolicLink()) {
			return new ArrayList<ResourceAttribute>();
		}
		Map<String, ResourceAttribute> map;
		if (isDirectory()) {
			map = getResourceSpecification().getDirExpectedAttributesMap();
		} else {
			map = getResourceSpecification().getFileExpectedAttributesMap();
		}
		if (map == null) {
			return new ArrayList<ResourceAttribute>();
		}
		return map.values();
	}

	private String getDestPath() {
		return getResourceSpecification().getDestName();
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
	private Path getRelativePath() {
		return Paths.get(getSrcBaseDir()).relativize(getSourcePath());
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
	@Override
	public Path getDestinationPath() {
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
	@Override
	public boolean exists() {
		return isRegularFile() || isDirectory();
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular file
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	@Override
	public boolean isRegularFile() {
		return getAttributes().isRegularFile();
	}

	/**
	 * @return <tt>true</tt> if this object's path points to a regular directory
	 *         (follow link). Note that {@link #isSymbolicLink()} can return
	 *         <tt>true</tt> too.
	 */
	@Override
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
	@Override
	public boolean isSymbolicLink() {
		return getAttributes().isSymbolicLink();
	}

	@Override
	public Path getSymbolicLinkTarget() {
		return getAttributes().getLinkTarget();
	}

	/**
	 * @return {@code true} if this link's target doesn't points outside of the
	 *         src-basedir and is not an absolute path.
	 */
	@Override
	public boolean isSafeLink() {
		Path symTarget = getSymbolicLinkTarget();
		if (symTarget.isAbsolute()) {
			return false;
		}
		int refLength = Paths.get(getSrcBaseDir()).normalize().getNameCount();
		Path computed = getSourcePath().getParent();
		for (Path p : symTarget) {
			computed = computed.resolve(p).normalize();
			if (computed.getNameCount() < refLength) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return <tt>true</tt> if this object is not a link or if it is a link,
	 *         which, regarding the link option, should be keep as link or
	 *         converted to the revelant file during transfer.
	 */
	@Override
	public boolean linkShouldBeConvertedToFile() {
		if (!isSymbolicLink()) {
			return true;
		}
		switch (getLinkOption()) {
		case COPY_LINKS:
			return true;
		case KEEP_LINKS:
			return false;
		case COPY_UNSAFE_LINKS:
			if (isSafeLink()) {
				return false;
			} else {
				return true;
			}
		default:
			throw new RuntimeException("shouldn't go here");
		}
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		if (isSymbolicLink()) {
			if (isDirectory()) {
				str.append("dirlink:");
			} else if (isRegularFile()) {
				str.append("filelink:");
			} else {
				str.append("invalidlink:");
			}
		} else if (isDirectory()) {
			str.append("dir:");
		} else {
			str.append("file:");
		}
		str.append(getSourcePath());
		str.append(", destination:");
		str.append(getDestinationPath());
		str.append(", attributes:");
		str.append(getExpectedAttributesAsList());
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
		if (anObject instanceof TransferableFile) {
			TransferableFile sr = (TransferableFile) anObject;
			return getDestinationPath().equals(sr.getDestinationPath());
		}
		return false;
	}

	@Override
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

	@Override
	public Path getSourcePath() {
		return _sourcePath;
	}

	private Path setSourcePath(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName()
					+ " (a file or directory path, relative or absolute).");
		}
		Path previous = getSourcePath();
		_sourcePath = path;
		return previous;
	}

	private ResourcesSpecification getResourcesSpecification() {
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

	@Override
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