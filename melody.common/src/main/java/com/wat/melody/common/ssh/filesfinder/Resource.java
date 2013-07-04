package com.wat.melody.common.ssh.filesfinder;

import java.io.IOException;
import java.nio.file.Path;

import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.LinkOption;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.TransferBehavior;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface Resource {

	public Path getPath();

	public Path getDestination();

	/*
	 * TODO : shouldn't throw I/O. Should get the target during construction,
	 * and store it
	 */
	public Path getSymbolicLinkTarget() throws IOException;

	public boolean isSymbolicLink();

	/*
	 * TODO : shouldn't throw I/O. Should get the target during construction,
	 * and store it
	 */
	public boolean isSafeLink() throws IOException;

	public boolean exists();

	public boolean isDirectory();

	public boolean isFile();

	public Modifiers getDirModifiers();

	public Modifiers getFileModifiers();

	public GroupID getGroup();

	public LinkOption getLinkOption();

	public boolean getTemplate();

	public TransferBehavior getTransferBehavior();

	public ResourceSpecification setResourceSpecification(
			ResourceSpecification spec);

}