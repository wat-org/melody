package com.wat.melody.common.transfer;

import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.transfer.resources.ResourceSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface Transferable {

	public Path getSourcePath();

	public Path getDestinationPath();

	public boolean getTemplate();

	public LinkOption getLinkOption();

	public TransferBehavior getTransferBehavior();

	public ResourceSpecification getResourceSpecification();

	public ResourceSpecification setResourceSpecification(
			ResourceSpecification resourceSpecification);

	public FileAttribute<?>[] getExpectedAttributes();

	public boolean exists();

	public boolean isRegularFile();

	public boolean isDirectory();

	public boolean isSymbolicLink();

	public boolean isSafeLink();

	public Path getSymbolicLinkTarget();

	public boolean linkShouldBeConvertedToFile();

	public EnhancedFileAttributes getAttributes();

}