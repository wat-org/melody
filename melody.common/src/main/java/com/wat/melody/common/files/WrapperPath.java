package com.wat.melody.common.files;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

/**
 * <p>
 * A {@link Path}, which provides a public 1-string-argument constructor and
 * implements {@link IFileBased}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperPath implements Path, IFileBased {

	private Path _path = null;

	public WrapperPath(String path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		_path = Paths.get(path);
	}

	@Override
	public String toString() {
		return _path.toString();
	}

	@Override
	public FileSystem getFileSystem() {
		return _path.getFileSystem();
	}

	@Override
	public boolean isAbsolute() {
		return _path.isAbsolute();
	}

	@Override
	public Path getRoot() {
		return _path.getRoot();
	}

	@Override
	public Path getFileName() {
		return _path.getFileName();
	}

	@Override
	public Path getParent() {
		return _path.getParent();
	}

	@Override
	public int getNameCount() {
		return _path.getNameCount();
	}

	@Override
	public Path getName(int index) {
		return _path.getName(index);
	}

	@Override
	public Path subpath(int beginIndex, int endIndex) {
		return _path.subpath(beginIndex, endIndex);
	}

	@Override
	public boolean startsWith(Path other) {
		return _path.startsWith(other);
	}

	@Override
	public boolean startsWith(String other) {
		return _path.startsWith(other);
	}

	@Override
	public boolean endsWith(Path other) {
		return _path.endsWith(other);
	}

	@Override
	public boolean endsWith(String other) {
		return _path.endsWith(other);
	}

	@Override
	public Path normalize() {
		return _path.normalize();
	}

	@Override
	public Path resolve(Path other) {
		return _path.resolve(other);
	}

	@Override
	public Path resolve(String other) {
		return _path.resolve(other);
	}

	@Override
	public Path resolveSibling(Path other) {
		return _path.resolveSibling(other);
	}

	@Override
	public Path resolveSibling(String other) {
		return _path.resolveSibling(other);
	}

	@Override
	public Path relativize(Path other) {
		return _path.relativize(other);
	}

	@Override
	public URI toUri() {
		return _path.toUri();
	}

	@Override
	public Path toAbsolutePath() {
		return _path.toAbsolutePath();
	}

	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		return _path.toRealPath(options);
	}

	@Override
	public File toFile() {
		return _path.toFile();
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events,
			Modifier... modifiers) throws IOException {
		return _path.register(watcher, events, modifiers);
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events)
			throws IOException {
		return _path.register(watcher, events);
	}

	@Override
	public Iterator<Path> iterator() {
		return _path.iterator();
	}

	@Override
	public int compareTo(Path other) {
		return _path.compareTo(other);
	}

}