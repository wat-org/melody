package com.wat.melody.common.transfer;

import java.nio.file.Path;

import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.transfer.resources.ResourceSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface Transferable {

	public Path getSourcePath();

	public Path getDestinationPath();

	public EnhancedFileAttributes getAttributes();

	public boolean exists();

	public boolean isRegularFile();

	public boolean isDirectory();

	public boolean isSymbolicLink();

	public boolean isSafeLink();

	public Path getSymbolicLinkTarget();

	public boolean getTemplate();

	public Modifiers getModifiers();

	public GroupID getGroup();

	public LinkOption getLinkOption();

	public TransferBehavior getTransferBehavior();

	public boolean linkShouldBeConvertedToFile();

	public ResourceSpecification getResourceSpecification();

	public ResourceSpecification setResourceSpecification(
			ResourceSpecification resourceSpecification);

}