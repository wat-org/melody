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
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.types.exception.IllegalModifiersException;
import com.wat.melody.common.ssh.types.exception.ResourceException;
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

	// Mandatory with no default value
	private File _localBaseDir;
	private String _match;
	// Mandatory with a default value
	private String _remoteBaseDir;
	private Modifiers _fileModifiers;
	private Modifiers _dirModifiers;
	private LinkOption _linkOption;
	private boolean _template;
	// Optional
	private GroupID _group;

	public ResourceMatcher() {
		super();
		initLocalBaseDir();
		initMatch();
		setRemoteBaseDir(".");
		setFileModifiers(DEFAULT_FILE_MODIFIERS);
		setDirModifiers(DEFAULT_DIR_MODIFIERS);
		setLinkOption(LinkOption.KEEP_LINKS);
		setTemplate(false);
		initGroup();
	}

	public ResourceMatcher(ResourceMatcher r) throws ResourceException {
		this();
		if (r.getLocalBaseDir() != null) {
			setLocalBaseDir(r.getLocalBaseDir());
		}
		if (r.getMatch() != null) {
			try {
				setMatch(r.getMatch());
			} catch (ResourceException Ex) {
				throw new RuntimeException("Unexpected error occurred while "
						+ "calling setMatch(\"" + r.getMatch() + "\"). "
						+ "Since this string have already been validated by "
						+ "the owner ResourceElement, such error cannot "
						+ "happened. "
						+ "Source code have certainly been modified and a bug "
						+ "have been introduced.", Ex);
			}
		}
		setRemoteBaseDir(r.getRemoteBaseDir());
		setFileModifiers(r.getFileModifiers());
		setDirModifiers(r.getDirModifiers());
		setLinkOption(r.getLinkOption());
		setTemplate(r.getTemplate());
		if (r.getGroup() != null) {
			setGroup(r.getGroup());
		}
	}

	private void initLocalBaseDir() {
		_localBaseDir = null;
	}

	private void initMatch() {
		_match = null;
	}

	private void initGroup() {
		_group = null;
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
		str.append(" }");
		return str.toString();
	}

	public File getLocalBaseDir() {
		return _localBaseDir;
	}

	@Attribute(name = LOCAL_BASEDIR_ATTR)
	public File setLocalBaseDir(File basedir) throws ResourceException {
		if (basedir == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Mus be a valid " + File.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		try {
			FS.validateDirExists(basedir.getAbsolutePath());
		} catch (IllegalDirectoryException Ex) {
			throw new ResourceException(Ex);
		}
		File previous = getLocalBaseDir();
		_localBaseDir = basedir;
		return previous;
	}

	public String getMatch() {
		return _match;
	}

	@Attribute(name = MATCH_ATTR)
	public String setMatch(String sMatch) throws ResourceException {
		if (sMatch == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " " + "(a PathMatcher Glob Pattern).");
		}
		if (sMatch.indexOf('/') == 0) {
			throw new ResourceException(Msg.bind(
					Messages.ResourceEx_INVALID_MATCH_ATTR, sMatch));
		}
		String previous = getMatch();
		_match = sMatch;
		return previous;
	}

	public String getRemoteBaseDir() {
		return _remoteBaseDir;
	}

	@Attribute(name = REMOTE_BASEDIR_ATTR)
	public String setRemoteBaseDir(String sDestination) {
		if (sDestination == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		String previous = getRemoteBaseDir();
		_remoteBaseDir = sDestination;
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

	public GroupID getGroup() {
		return _group;
	}

	@Attribute(name = GROUP_ATTR)
	public GroupID setGroup(GroupID sGroup) {
		if (sGroup == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + GroupID.class.getCanonicalName()
					+ ".");
		}
		GroupID previous = getGroup();
		_group = sGroup;
		return previous;
	}

	public boolean getTemplate() {
		return _template;
	}

	@Attribute(name = TEMPLATE_ATTR)
	public boolean setTemplate(boolean sGroup) {
		boolean previous = getTemplate();
		_template = sGroup;
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
			throw new IllegalArgumentException("invalid ResourceMatcher : "
					+ "localBaseDir is null.");
		}
		if (resourceMatcher.getMatch() == null) {
			throw new IllegalArgumentException("invalid ResourceMatcher : "
					+ "match is null.");
		}
		String path = resourceMatcher.getLocalBaseDir().getAbsolutePath()
				+ SysTool.FILE_SEPARATOR + resourceMatcher.getMatch();
		String pattern = "glob:" + Paths.get(path).normalize().toString();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		pattern = pattern.replaceAll("\\\\", "\\\\\\\\");
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