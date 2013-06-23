package com.wat.melody.common.ssh.types;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p>
 * A {@link LocalResource} describe a single file or directory which was found
 * with {@link ResourceMatcher#findResources()}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocalResource {

	private Path _path;
	private ResourceMatcher _resourceMatcher;

	/**
	 * @param path
	 *            is the path of a file or directory.
	 * @param matcher
	 *            is the {@link ResourceMatcher} which was used to find this
	 *            file or directory.
	 */
	public LocalResource(Path path, ResourceMatcher matcher) {
		super();
		setPath(path);
		setResourceMatcher(matcher);
	}

	public File getLocalBaseDir() {
		return getResourceMatcher().getLocalBaseDir();
	}

	public String getRemoteBaseDir() {
		return getResourceMatcher().getRemoteBaseDir();
	}

	public Modifiers getFileModifiers() {
		return getResourceMatcher().getFileModifiers();
	}

	public Modifiers getDirModifiers() {
		return getResourceMatcher().getDirModifiers();
	}

	public LinkOption getLinkOption() {
		return getResourceMatcher().getLinkOption();
	}

	public TransferBehavior getTransferBehavior() {
		return getResourceMatcher().getTransferBehavior();
	}

	public boolean getTemplate() {
		return getResourceMatcher().getTemplate();
	}

	public GroupID getGroup() {
		return getResourceMatcher().getGroup();
	}

	public boolean exists() {
		return Files.exists(getPath());
	}

	public boolean isFile() {
		return Files.isRegularFile(getPath(),
				java.nio.file.LinkOption.NOFOLLOW_LINKS)
				|| (isSymbolicLink() && !isDirectory());
	}

	public boolean isDirectory() {
		return Files.isDirectory(getPath());
	}

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

	public ResourceMatcher getResourceMatcher() {
		return _resourceMatcher;
	}

	public ResourceMatcher setResourceMatcher(ResourceMatcher resourceMatcher) {
		if (resourceMatcher == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ResourceMatcher.class.getCanonicalName() + ".");
		}
		ResourceMatcher previous = getResourceMatcher();
		_resourceMatcher = resourceMatcher;
		return previous;
	}

}