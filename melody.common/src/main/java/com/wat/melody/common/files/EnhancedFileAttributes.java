package com.wat.melody.common.files;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * <p>
 * This class extends {@link BasicFileAttributes}, simplifying the way to
 * retrieve information if the file described is a link.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface EnhancedFileAttributes extends BasicFileAttributes {

	public Path getLinkTarget();

}