package com.wat.melody.common.ssh.types;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.ssh.types.exception.IllegalModifiersException;
import com.wat.melody.common.systool.SysTool;

/**
 * <p>
 * A {@link ResourceMatcher} is especially design to find files and directories
 * based on a Glob.
 * </p>
 * 
 * <p>
 * <i>The content of this object must be validated before calling
 * {@link #findResources()}. </i>
 * </p>
 * 
 * <p>
 * <i> Its attributes are mandatory for the {@link #findResources()} method to
 * work. But they are not declared as 'mandatory' Task's Attributes. The reason
 * is that they can be herited from its {@link Resources} {@link ITask} . </i>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourceMatcher {

	private static Modifiers createModifiers(String modifiers) {
		try {
			return Modifiers.parseString(modifiers);
		} catch (IllegalModifiersException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a FileModifiers with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static Modifiers DEFAULT_FILE_MODIFIERS = createModifiers("660");
	private static Modifiers DEFAULT_DIR_MODIFIERS = createModifiers("774");

	/**
	 * Attribute, which specifies the local directory.
	 */
	public static final String LOCAL_BASEDIR_ATTR = "local-basedir";

	/**
	 * Attribute, which specifies what to search.
	 */
	public static final String MATCH_ATTR = "match";

	/**
	 * Attribute, which specifies the remote directory.
	 */
	public static final String REMOTE_BASEDIR_ATTR = "remote-basedir";

	/**
	 * Attribute, which specifies the file's modifier.
	 */
	public static final String FILE_MODIFIERS_ATTR = "file-modifiers";

	/**
	 * Attribute, which specifies the directory's modifier.
	 */
	public static final String DIR_MODIFIERS_ATTR = "dir-modifiers";

	/**
	 * Attribute, which indicates if the matching resource should be templated
	 * or not.
	 */
	public static final String TEMPLATE_ATTR = "template";

	/**
	 * Attribute, which specifies the group to apply.
	 */
	public static final String GROUP_ATTR = "group";

	/**
	 * Attribute, which specifies the behavior when a link found.
	 */
	public static final String LINK_OPTION_ATTR = "link-option";

	/**
	 * Attribute, which specifies the behavior when the to file transfer already
	 * exists.
	 */
	public static final String TRANSFER_BEHAVIOR_ATTR = "transfer-behavior";

	// Mandatory with no default value
	private File _localBaseDir = null;
	// Mandatory (verified by Task Factory)
	private String _match = null;
	// Mandatory with a default value
	private String _remoteBaseDir = ".";
	private Modifiers _fileModifiers = DEFAULT_FILE_MODIFIERS;
	private Modifiers _dirModifiers = DEFAULT_DIR_MODIFIERS;
	private LinkOption _linkOption = LinkOption.KEEP_LINKS;
	private TransferBehavior _transferBehavior = TransferBehavior.OVERWRITE_IF_LOCAL_NEWER;
	private boolean _template = false;
	// Optional
	private GroupID _group = null;

	public ResourceMatcher() {
		// This 0-arg constructor is used by the Task Factory
	}

	public ResourceMatcher(ResourceMatcher r) {
		if (r.getMatch() == null) {
			throw new IllegalArgumentException("invalid arg : the given "
					+ ResourceMatcher.MATCH_ATTR + " is null.");
		}
		if (r.getLocalBaseDir() != null) {
			setLocalBaseDir(r.getLocalBaseDir());
		}
		setRemoteBaseDir(r.getRemoteBaseDir());
		setFileModifiers(r.getFileModifiers());
		setDirModifiers(r.getDirModifiers());
		setLinkOption(r.getLinkOption());
		setTransferBehavior(r.getTransferBehavior());
		setTemplate(r.getTemplate());
		if (r.getGroup() != null) {
			setGroup(r.getGroup());
		}
	}

	public List<SimpleResource> findResources() throws IOException {
		if (getLocalBaseDir() == null || getMatch() == null) {
			return new ArrayList<SimpleResource>();
		}
		return new Finder(this).findFiles();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("local-basedir:");
		str.append(getLocalBaseDir());
		str.append(", match:");
		str.append(getMatch());
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

	public File getLocalBaseDir() {
		return _localBaseDir;
	}

	@Attribute(name = LOCAL_BASEDIR_ATTR)
	public File setLocalBaseDir(File basedir) {
		if (basedir == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Mus be a valid " + File.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		File previous = getLocalBaseDir();
		_localBaseDir = basedir;
		return previous;
	}

	public String getMatch() {
		return _match;
	}

	@Attribute(name = MATCH_ATTR, mandatory = true)
	public String setMatch(String match) {
		if (match == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " " + "(a Path Matcher Glob Pattern).");
		}
		String previous = getMatch();
		_match = match;
		return previous;
	}

	public String getRemoteBaseDir() {
		return _remoteBaseDir;
	}

	@Attribute(name = REMOTE_BASEDIR_ATTR)
	public String setRemoteBaseDir(String destination) {
		if (destination == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		String previous = getRemoteBaseDir();
		_remoteBaseDir = destination;
		return previous;
	}

	public Modifiers getFileModifiers() {
		return _fileModifiers;
	}

	@Attribute(name = FILE_MODIFIERS_ATTR)
	public Modifiers setFileModifiers(Modifiers modifiers) {
		if (modifiers == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Modifiers.class.getCanonicalName()
					+ ".");
		}
		Modifiers previous = getFileModifiers();
		_fileModifiers = modifiers;
		return previous;
	}

	public Modifiers getDirModifiers() {
		return _dirModifiers;
	}

	@Attribute(name = DIR_MODIFIERS_ATTR)
	public Modifiers setDirModifiers(Modifiers modifiers) {
		if (modifiers == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Modifiers.class.getCanonicalName()
					+ ".");
		}
		Modifiers previous = getDirModifiers();
		_dirModifiers = modifiers;
		return previous;
	}

	public LinkOption getLinkOption() {
		return _linkOption;
	}

	@Attribute(name = LINK_OPTION_ATTR)
	public LinkOption setLinkOption(LinkOption linkOption) {
		if (linkOption == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + LinkOption.class.getCanonicalName()
					+ ".");
		}
		LinkOption previous = getLinkOption();
		_linkOption = linkOption;
		return previous;
	}

	public TransferBehavior getTransferBehavior() {
		return _transferBehavior;
	}

	@Attribute(name = TRANSFER_BEHAVIOR_ATTR)
	public TransferBehavior setTransferBehavior(
			TransferBehavior transferBehavior) {
		if (transferBehavior == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TransferBehavior.class.getCanonicalName() + ".");
		}
		TransferBehavior previous = getTransferBehavior();
		_transferBehavior = transferBehavior;
		return previous;
	}

	public boolean getTemplate() {
		return _template;
	}

	@Attribute(name = TEMPLATE_ATTR)
	public boolean setTemplate(boolean yesno) {
		boolean previous = getTemplate();
		_template = yesno;
		return previous;
	}

	public GroupID getGroup() {
		return _group;
	}

	@Attribute(name = GROUP_ATTR)
	public GroupID setGroup(GroupID group) {
		if (group == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + GroupID.class.getCanonicalName()
					+ ".");
		}
		GroupID previous = getGroup();
		_group = group;
		return previous;
	}

}

class Finder extends SimpleFileVisitor<Path> {

	private ResourceMatcher _resourceMatcher;
	private final PathMatcher _matcher;
	private final List<SimpleResource> _resources;

	public Finder(ResourceMatcher resourceMatcher) {
		super();
		if (resourceMatcher == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ ResourceMatcher.class.getCanonicalName() + ".");
		}
		if (resourceMatcher.getLocalBaseDir() == null) {
			throw new IllegalArgumentException("invalid arg : the given "
					+ ResourceMatcher.LOCAL_BASEDIR_ATTR + " is null.");
		}
		if (resourceMatcher.getMatch() == null) {
			throw new IllegalArgumentException("invalid arg : the given "
					+ ResourceMatcher.MATCH_ATTR + " is null.");
		}
		String path = Paths.get(
				resourceMatcher.getLocalBaseDir().getAbsolutePath())
				.normalize()
				+ SysTool.FILE_SEPARATOR + resourceMatcher.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		_resourceMatcher = resourceMatcher;
		_matcher = FileSystems.getDefault().getPathMatcher(pattern);
		_resources = new ArrayList<SimpleResource>();
	}

	public List<SimpleResource> findFiles() throws IOException {
		Set<FileVisitOption> set = new HashSet<FileVisitOption>();
		set.add(FileVisitOption.FOLLOW_LINKS);
		Files.walkFileTree(
				Paths.get(_resourceMatcher.getLocalBaseDir().getAbsolutePath()),
				set, Integer.MAX_VALUE, this);
		return _resources;
	}

	private void matches(Path path, BasicFileAttributes attrs) {
		if (path != null && _matcher.matches(path)) {
			_resources.add(new SimpleResource(path, _resourceMatcher));
		}
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
		matches(file, attrs);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		SimpleResource sr = new SimpleResource(dir, _resourceMatcher);
		if (sr.isSymbolicLink()) {
			switch (sr.getLinkOption()) {
			case COPY_LINKS:
				return FileVisitResult.CONTINUE;
			case KEEP_LINKS:
				return FileVisitResult.SKIP_SUBTREE;
			case COPY_UNSAFE_LINKS:
				if (sr.isSafeLink()) {
					return FileVisitResult.SKIP_SUBTREE;
				} else {
					return FileVisitResult.CONTINUE;
				}
			}
		}
		matches(dir, attrs);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		return FileVisitResult.CONTINUE;
	}

}