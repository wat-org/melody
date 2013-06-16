package com.wat.melody.common.ssh.types;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p>
 * A {@link SimpleResource} describe a single file or directory which was found
 * with {@link ResourceMatcher#findResources()}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class SimpleResource {

	private Path _path;
	private ResourceMatcher _matcher;

	/**
	 * @param path
	 *            is the absolute path of a file or directory.
	 * @param matcher
	 *            is the {@link ResourceMatcher} which was used to find this
	 *            file or directory.
	 */
	public SimpleResource(Path path, ResourceMatcher matcher) {
		super();
		setPath(path);
		setMatcher(matcher);
	}

	public File getLocalBaseDir() {
		return getMatcher().getLocalBaseDir();
	}

	public String getRemoteBaseDir() {
		return getMatcher().getRemoteBaseDir();
	}

	public Modifiers getFileModifiers() {
		return getMatcher().getFileModifiers();
	}

	public Modifiers getDirModifiers() {
		return getMatcher().getDirModifiers();
	}

	public LinkOption getLinkOption() {
		return getMatcher().getLinkOption();
	}

	public boolean getTemplate() {
		return getMatcher().getTemplate();
	}

	public GroupID getGroup() {
		return getMatcher().getGroup();
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
	 * localBaseDir = /home/user/dir1/dir2
	 * path         = /home/user/dir1/dir2/dir3/dir4/file.txt
	 * 
	 * will return                         dir3/dir4/file.txt
	 * </pre>
	 * 
	 * @return a {@link Path} which, when resolved from the localBaseDir, is
	 *         equal to the absolute path of this object.
	 */
	protected Path getRelativePath() {
		return Paths.get(getLocalBaseDir().getPath()).relativize(getPath());
	}

	/**
	 * <pre>
	 * Sample
	 * 
	 * localBaseDir  = /home/user/dir1/dir2
	 * path          = /home/user/dir1/dir2/dir3/dir4/file.txt
	 * remoteBaseDir = /tmp
	 * 
	 * will return     /tmp/                dir3/dir4/file.txt
	 * </pre>
	 * 
	 * <p>
	 * <i> * The resulting path can be a relative or absolute path, depending
	 * the if the remoteBaseDir is relative or absolute ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @return the destination {@link Path} of this object.
	 */
	public Path getDestination() {
		return Paths.get(getRemoteBaseDir()).resolve(getRelativePath());
	}

	/**
	 * 
	 * @return {@code true} if this link's target doesn't points outside of the
	 *         localBaseDir and is not an absolute path.
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
		return "{ " + "localBaseDir:" + getLocalBaseDir() + ", "
				+ (isFile() ? "file" : "directory") + ":" + getRelativePath()
				+ ", remoteBaseDir:" + getRemoteBaseDir() + ", fileModifiers:"
				+ getFileModifiers() + ", dirModifiers:" + getDirModifiers()
				+ ", linkOption:" + getLinkOption() + ", group:" + getGroup()
				+ " }";
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof SimpleResource) {
			SimpleResource sr = (SimpleResource) anObject;
			return getPath().equals(sr.getPath())
					&& getMatcher().getRemoteBaseDir().equals(
							sr.getMatcher().getRemoteBaseDir());
		}
		return false;
	}

	public Path getPath() {
		return _path;
	}

	private Path setPath(Path path) {
		if (path == null || !path.isAbsolute()) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Path (an absolute file or directory "
					+ "path).");
		}
		Path previous = getPath();
		_path = path;
		return previous;
	}

	public ResourceMatcher getMatcher() {
		return _matcher;
	}

	private ResourceMatcher setMatcher(ResourceMatcher rm) {
		if (rm == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ResourceMatcher.");
		}
		ResourceMatcher previous = getMatcher();
		_matcher = rm;
		return previous;
	}

}