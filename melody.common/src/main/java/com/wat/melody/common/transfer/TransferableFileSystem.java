package com.wat.melody.common.transfer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.transfer.exception.TemplatingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface TransferableFileSystem extends FileSystem {

	public void transferRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException;

	public void transformRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws TemplatingException, IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException;

}