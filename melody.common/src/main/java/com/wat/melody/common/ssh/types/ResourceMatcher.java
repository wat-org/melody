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

	/**
	 * The 'basedir' XML Attribute
	 */
	public static final String LOCAL_BASEDIR_ATTR = "localBaseDir";

	/**
	 * The 'match' XML Attribute
	 */
	public static final String MATCH_ATTR = "match";

	/**
	 * The 'remoteBaseDir' XML Attribute
	 */
	public static final String REMOTE_BASEDIR_ATTR = "remoteBaseDir";

	/**
	 * The 'fileModifiers' XML Attribute
	 */
	public static final String FILE_MODIFIERS_ATTR = "fileModifiers";

	/**
	 * The 'dirModifiers' XML Attribute
	 */
	public static final String DIR_MODIFIERS_ATTR = "dirModifiers";

	/**
	 * The 'template' XML Attribute
	 */
	public static final String TEMPLATE_ATTR = "template";

	/**
	 * The 'group' XML Attribute
	 */
	public static final String GROUP_ATTR = "group";

	/**
	 * The 'linkOption' XML Attribute
	 */
	public static final String LINK_OPTION_ATTR = "linkOption";

	// Mandatory with no default value
	private File moLocalBaseDir;
	private String msMatch;
	// Mandatory with a default value
	private String msRemoteBaseDir;
	private Modifiers moFileModifiers;
	private Modifiers moDirModifiers;
	private LinkOption moLinkOption;
	private boolean mbTemplate;
	// Optional
	private GroupID msGroup;

	public ResourceMatcher() {
		super();
		initLocalBaseDir();
		initMatch();
		setRemoteBaseDir(".");
		try {
			setFileModifiers(Modifiers.parseString("660"));
		} catch (IllegalModifiersException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the FileModifiers with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		try {
			setDirModifiers(Modifiers.parseString("774"));
		} catch (IllegalModifiersException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the DirModifiers with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
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
		moLocalBaseDir = null;
	}

	private void initMatch() {
		msMatch = null;
	}

	private void initGroup() {
		msGroup = null;
	}

	public List<SimpleResource> findResources() throws IOException {
		if (getLocalBaseDir() == null || getMatch() == null) {
			return new ArrayList<SimpleResource>();
		}
		return new Finder(this).findFiles();
	}

	@Override
	public String toString() {
		return "{ " + "localBaseDir:" + getLocalBaseDir() + ", match:"
				+ getMatch() + ", remoteBaseDir:" + getRemoteBaseDir()
				+ ", fileModifiers:" + getFileModifiers() + ", dirModifiers:"
				+ getDirModifiers() + ", linkOption:" + getLinkOption()
				+ ", group:" + getGroup() + " }";
	}

	public File getLocalBaseDir() {
		return moLocalBaseDir;
	}

	@Attribute(name = LOCAL_BASEDIR_ATTR)
	public File setLocalBaseDir(File basedir) throws ResourceException {
		if (basedir == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Mus be a valid String (a Directory Path).");
		}
		try {
			FS.validateDirExists(basedir.getAbsolutePath());
		} catch (IllegalDirectoryException Ex) {
			throw new ResourceException(Ex);
		}
		File previous = getLocalBaseDir();
		moLocalBaseDir = basedir;
		return previous;
	}

	public String getMatch() {
		return msMatch;
	}

	@Attribute(name = MATCH_ATTR)
	public String setMatch(String sMatch) throws ResourceException {
		if (sMatch == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String "
					+ "(a PathMatcher Glob Pattern).");
		}
		if (sMatch.indexOf('/') == 0) {
			throw new ResourceException(Messages.bind(
					Messages.ResourceEx_INVALID_MATCH_ATTR, sMatch));
		}
		String previous = getMatch();
		msMatch = sMatch;
		return previous;
	}

	public String getRemoteBaseDir() {
		return msRemoteBaseDir;
	}

	@Attribute(name = REMOTE_BASEDIR_ATTR)
	public String setRemoteBaseDir(String sDestination) {
		if (sDestination == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a Directory Path).");
		}
		String previous = getRemoteBaseDir();
		msRemoteBaseDir = sDestination;
		return previous;
	}

	public Modifiers getFileModifiers() {
		return moFileModifiers;
	}

	@Attribute(name = FILE_MODIFIERS_ATTR)
	public Modifiers setFileModifiers(Modifiers modifiers) {
		if (modifiers == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Modifiers.");
		}
		Modifiers previous = getFileModifiers();
		moFileModifiers = modifiers;
		return previous;
	}

	public Modifiers getDirModifiers() {
		return moDirModifiers;
	}

	@Attribute(name = DIR_MODIFIERS_ATTR)
	public Modifiers setDirModifiers(Modifiers modifiers) {
		if (modifiers == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Modifiers.");
		}
		Modifiers previous = getDirModifiers();
		moDirModifiers = modifiers;
		return previous;
	}

	public LinkOption getLinkOption() {
		return moLinkOption;
	}

	@Attribute(name = LINK_OPTION_ATTR)
	public LinkOption setLinkOption(LinkOption linkOption) {
		if (linkOption == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid LinkOption.");
		}
		LinkOption previous = getLinkOption();
		moLinkOption = linkOption;
		return previous;
	}

	public GroupID getGroup() {
		return msGroup;
	}

	@Attribute(name = GROUP_ATTR)
	public GroupID setGroup(GroupID sGroup) {
		if (sGroup == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid GroupID.");
		}
		GroupID previous = getGroup();
		msGroup = sGroup;
		return previous;
	}

	public boolean getTemplate() {
		return mbTemplate;
	}

	@Attribute(name = TEMPLATE_ATTR)
	public boolean setTemplate(boolean sGroup) {
		boolean previous = getTemplate();
		mbTemplate = sGroup;
		return previous;
	}

}

class Finder extends SimpleFileVisitor<Path> {

	private ResourceMatcher moBaseResource;
	private final PathMatcher matcher;
	private final List<SimpleResource> maResources;

	public Finder(ResourceMatcher baseResource) {
		super();
		if (baseResource == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid ResourceElement.");
		}
		if (baseResource.getLocalBaseDir() == null) {
			throw new IllegalArgumentException("invalid ResourceElement : "
					+ "localBaseDir is null.");
		}
		if (baseResource.getMatch() == null) {
			throw new IllegalArgumentException("invalid ResourceElement : "
					+ "match is null.");
		}
		String path = baseResource.getLocalBaseDir().getAbsolutePath()
				+ SysTool.FILE_SEPARATOR + baseResource.getMatch();
		String sPattern = "glob:" + Paths.get(path).normalize().toString();
		moBaseResource = baseResource;
		matcher = FileSystems.getDefault().getPathMatcher(sPattern);
		maResources = new ArrayList<SimpleResource>();
	}

	public List<SimpleResource> findFiles() throws IOException {
		Set<FileVisitOption> set = new HashSet<FileVisitOption>();
		set.add(FileVisitOption.FOLLOW_LINKS);
		Files.walkFileTree(
				Paths.get(moBaseResource.getLocalBaseDir().getAbsolutePath()),
				set, Integer.MAX_VALUE, this);
		return maResources;
	}

	private void matches(Path path, BasicFileAttributes attrs) {
		if (path != null && matcher.matches(path)) {
			maResources.add(new SimpleResource(path, moBaseResource));
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
		matches(dir, attrs);
		SimpleResource sr = new SimpleResource(dir, moBaseResource);
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