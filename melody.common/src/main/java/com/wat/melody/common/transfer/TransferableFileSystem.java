package com.wat.melody.common.transfer;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface TransferableFileSystem extends FileSystem {

	public void transferRegularFile(Path source, Path dest,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
			AccessDeniedException, IllegalFileAttributeException;

	/*
	 * TODO : should better if it provide a method transformRegularFile(Path)
	 */
	public TemplatingHandler getTemplatingHandler();

}