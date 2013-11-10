package com.wat.melody.common.files.exception;

import java.nio.file.FileSystemException;
import java.nio.file.Path;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SymbolicLinkNotSupported extends FileSystemException {

	private static final long serialVersionUID = 65423367678679261L;

	public SymbolicLinkNotSupported(String link, String target, String message) {
		super("'" + link + "'", "'" + target + "'", message);
	}

	public SymbolicLinkNotSupported(String link, String target) {
		super(link, target, "Symbolic links are not supported.");
	}

	public SymbolicLinkNotSupported(Path link, Path target) {
		this(link.toString(), target.toString());
	}

	public SymbolicLinkNotSupported(Path link, Path target, String message) {
		this(link.toString(), target.toString(), message);
	}

	public SymbolicLinkNotSupported(String link, String target, Throwable cause) {
		this(link, target);
		initCause(cause);
	}

	public SymbolicLinkNotSupported(Path link, Path target, Throwable cause) {
		this(link.toString(), target.toString(), cause);
	}

}