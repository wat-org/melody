package com.wat.melody.common.transfer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.SymbolicLinkNotSupported;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.exception.TemplatingException;
import com.wat.melody.common.transfer.exception.TransferException;
import com.wat.melody.common.transfer.resources.ResourceSpecification;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;
import com.wat.melody.common.transfer.resources.attributes.AttributeBase;

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

	private static Logger log = LoggerFactory.getLogger(TransferableFile.class);

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

	@Override
	public void transfer(TransferableFileSystem fs) throws IOException,
			InterruptedIOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		log.debug(Msg.bind(Messages.TransferMsg_BEGIN, this));
		if (isDirectory()) { // dir link will return true
			if (linkShouldBeConvertedToFile()) {
				TransferHelper.createDirectory(fs, getDestinationPath(),
						getExpectedAttributes());
			} else if (getLinkOption() == LinkOption.SKIP_LINKS) {
				ln_skip(fs);
			} else {
				createSymlink(fs);
			}
		} else if (isSymbolicLink()) {
			ln(fs);
		} else {
			template(fs);
		}
		log.info(Msg.bind(Messages.TransferMsg_END, this));
	}

	protected void ln(TransferableFileSystem fs) throws IOException,
			InterruptedIOException {
		if (getLinkOption() == LinkOption.SKIP_LINKS) {
			ln_skip(fs);
		} else if (linkShouldBeConvertedToFile()) {
			ln_copy(fs);
		} else {
			createSymlink(fs);
		}
	}

	protected void createSymlink(TransferableFileSystem fs) throws IOException {
		try {
			TransferHelper.createSymbolicLink(fs, getDestinationPath(),
					getSymbolicLinkTarget(), getExpectedAttributes());
		} catch (SymbolicLinkNotSupported Ex) {
			log.warn(new TransferException(Messages.TransferMsg_SKIP_LINK, Ex)
					.toString());
		}
	}

	protected void ln_copy(TransferableFileSystem fs) throws IOException,
			InterruptedIOException {
		if (!exists()) {
			deleteDestination(fs);
			log.warn(new TransferException(Messages.TransferMsg_SKIP_LINK,
					new TransferException(Msg.bind(
							Messages.TransferMsg_LINK_COPY_UNSAFE_IMPOSSIBLE,
							getSourcePath()))).toString());
			return;
		}
		template(fs);
	}

	protected void template(TransferableFileSystem fs) throws IOException,
			InterruptedIOException {
		if (getTemplate() == true) {
			if (fs.getTemplatingHandler() == null) {
				throw new IllegalArgumentException(
						Messages.TransferEx_NO_TEMPLATING_HANDLER);
			}
			Path template;
			try {
				/*
				 * TODO : this templating logic can't work for download...
				 */
				template = fs.getTemplatingHandler()
						.doTemplate(getSourcePath());
			} catch (TemplatingException Ex) {
				throw new IOException(null, Ex);
			}
			transfer(fs, template);
		} else {
			transfer(fs, getSourcePath());
		}
	}

	protected void transfer(TransferableFileSystem fs, Path source)
			throws IOException, InterruptedIOException, AccessDeniedException {
		Path dest = getDestinationPath();
		FileAttribute<?>[] attrs = getExpectedAttributes();
		if (TransferHelper.ensureDestinationIsRegularFile(fs, getAttributes(),
				dest, getTransferBehavior())) {
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_FILE_ALREADY_EXISTS);
			try {
				fs.setAttributes(dest, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.toString());
			}
		} else {
			try {
				fs.transferRegularFile(source, dest, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.toString());
			}
		}
	}

	protected void ln_skip(TransferableFileSystem fs) {
		log.info(Messages.TransferMsg_LINK_SKIPPED);
	}

	protected void deleteDestination(TransferableFileSystem fs)
			throws IOException {
		Path path = getDestinationPath();
		try {
			EnhancedFileAttributes attrs = fs.readAttributes(path);
			if (attrs.isDirectory() && !attrs.isSymbolicLink()) {
				fs.deleteDirectory(path);
			} else {
				fs.deleteIfExists(path);
			}
		} catch (NoSuchFileException ignored) {
		}
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
		if (isSymbolicLink() && !linkShouldBeConvertedToFile()) {
			return getResourceSpecification().getLinkAttributes();
		} else if (isDirectory()) {
			return getResourceSpecification().getDirExpectedAttributes();
		} else if (isRegularFile()) {
			return getResourceSpecification().getFileAttributes();
		}
		return null;
	}

	private Collection<AttributeBase<?>> getExpectedAttributesAsList() {
		Map<String, AttributeBase<?>> m = null;
		if (isSymbolicLink() && !linkShouldBeConvertedToFile()) {
			m = getResourceSpecification().getLinkAttributesMap();
		} else if (isDirectory()) {
			m = getResourceSpecification().getDirAttributesMap();
		} else if (isRegularFile()) {
			m = getResourceSpecification().getFileAttributesMap();
		}
		return m != null ? m.values() : new ArrayList<AttributeBase<?>>();
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
		case SKIP_LINKS:
			return false;
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

	protected ResourceSpecification getResourceSpecification() {
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