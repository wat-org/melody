package com.wat.melody.common.files;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.wat.melody.common.ex.ConsolidatedException;

/**
 * <p>
 * Simple remote file tree walker that works in a similar manner to nftw(3C).
 * </p>
 * 
 * @see Files#walkFileTree
 * 
 * @author Guillaume Cornet
 * 
 */
public class EnhancedFileTreeWalker {

	private final FileSystem _fs;
	private final boolean _followLinks;
	private final EnhancedFileVisitor<? super Path> _visitor;
	private final int _maxDepth;

	public EnhancedFileTreeWalker(FileSystem fs, Set<FileVisitOption> options,
			EnhancedFileVisitor<? super Path> visitor, int maxDepth) {
		if (fs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FileSystem.class.getCanonicalName()
					+ ".");
		}
		if (visitor == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ EnhancedFileVisitor.class.getCanonicalName()
					+ "<? super " + Path.class.getCanonicalName() + ">.");
		}
		if (maxDepth < 0) {
			throw new IllegalArgumentException(maxDepth + ": Not accepted. "
					+ "Must be a positive integer or 0.");
		}

		boolean fl = false;
		if (options != null) {
			for (FileVisitOption option : options) {
				if (option == null) {
					continue;
				} else if (option == FileVisitOption.FOLLOW_LINKS) {
					fl = true;
				} else {
					throw new RuntimeException("Should not get here");
				}
			}
		}
		_fs = fs;
		_followLinks = fl;
		_visitor = visitor;
		_maxDepth = maxDepth;
	}

	/**
	 * Walk file tree starting at the given file
	 */
	public void walk(Path start) throws IOException, InterruptedIOException {
		FileVisitResult result = walk(start, 0,
				new ArrayList<AncestorDirectory>());
		Objects.requireNonNull(result, "FileVisitor returned null");
	}

	/**
	 * @param path
	 *            the directory to visit
	 * @param depth
	 *            depth remaining
	 * @param ancestors
	 *            use when cycle detection is enabled
	 */
	private FileVisitResult walk(Path path, int depth,
			List<AncestorDirectory> ancestors) throws IOException,
			InterruptedIOException {
		if (Thread.interrupted()) {
			throw new InterruptedIOException("listing interrupted");
		}

		EnhancedFileAttributes attrs = null;

		// attempt to get attributes of file. If fails and we are following
		// links then a link target might not exist so get attributes of link
		try {
			attrs = _fs.readAttributes(path);
		} catch (IOException Ex) {
			return _visitor.visitFileFailed(path, Ex);
		}

		// max depth or file is not a directory or (dir link+nofollowlink)
		if (depth >= _maxDepth
				|| !attrs.isDirectory()
				|| (!_followLinks && attrs.isDirectory() && attrs
						.isSymbolicLink())) {
			return _visitor.visitFile(path, attrs);
		}

		// check for cycles when following links
		if (_followLinks) {
			// if this directory and ancestor has a file key then we compare
			// them; otherwise we use less efficient isSameFile test.
			for (AncestorDirectory ancestor : ancestors) {
				if (ancestor.file().equals(path)) {
					// cycle detected
					return _visitor.visitFileFailed(path, new IOException(path
							+ ": Cycle detected"));
				}
			}

			ancestors.add(new AncestorDirectory(path));
		}

		// visit directory
		try {
			DirectoryStream<Path> stream;
			FileVisitResult result;

			// open the directory
			try {
				stream = _fs.newDirectoryStream(path);
			} catch (InterruptedIOException Ex) {
				throw Ex;
			} catch (IOException Ex) {
				return _visitor.visitFileFailed(path, Ex);
			}

			// the exception notified to the postVisitDirectory method
			ConsolidatedException causes = new ConsolidatedException(path
					+ ": errors occured while visiting directory.");

			// invoke preVisitDirectory and then visit each entry
			result = _visitor.preVisitDirectory(path, attrs);
			if (result != FileVisitResult.CONTINUE) {
				return result;
			}

			try {
				// DirectoryStream's iterator can throw runtime exception
				for (Path entry : stream) {
					try {
						result = walk(entry, depth + 1, ancestors);

						// returning null will cause NPE to be thrown
						if (result == null
								|| result == FileVisitResult.TERMINATE) {
							return result;
						}
						// skip remaining siblings in this directory
						if (result == FileVisitResult.SKIP_SIBLINGS) {
							break;
						}
					} catch (InterruptedIOException Ex) {
						throw Ex;
					} catch (IOException Ex) {
						causes.addCause(Ex);
					}
				}
			} catch (Throwable Ex) {
				causes.addCause(Ex);
			}

			// invoke postVisitDirectory last
			return _visitor.postVisitDirectory(path,
					causes.countCauses() == 0 ? null : new IOException(causes));

		} finally {
			// remove key from trail if doing cycle detection
			if (_followLinks) {
				ancestors.remove(ancestors.size() - 1);
			}
		}
	}

	private static class AncestorDirectory {
		private final Path _dir;

		AncestorDirectory(Path dir) {
			_dir = dir;
		}

		Path file() {
			return _dir;
		}
	}

}