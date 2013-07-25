package com.wat.melody.common.transfer;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.transfer.exception.TemplatingException;
import com.wat.melody.common.transfer.exception.TransferException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class TransferNoThread {

	/*
	 * TODO : remove link to ssh.types.GroupID and ssh.types.Modifiers
	 */
	private static Logger log = LoggerFactory.getLogger(TransferNoThread.class);

	private Transferable _transferable;
	private TemplatingHandler _templatingHandler;
	private FileSystem _sourceFileSystem;
	private FileSystem _destinationFileSystem;

	public TransferNoThread(FileSystem srcFS, FileSystem destFS,
			Transferable t, TemplatingHandler th) {
		setSourceFileSystem(srcFS);
		setDestinationFileSystem(destFS);
		setTransferable(t);
		setTemplatingHandler(th);
	}

	public void doTransfer() throws TransferException {
		Transferable t = getTransferable();
		log.debug(Msg.bind(Messages.TransferMsg_BEGIN, t));
		try {
			if (t.isSymbolicLink()) {
				ln();
			} else {
				template();
				chmod();
				chgrp();
			}
		} catch (IOException Ex) {
			throw new TransferException(Ex);
		}
		log.info(Msg.bind(Messages.TransferMsg_END, t));
	}

	protected void ln() throws IOException, TransferException {
		switch (getTransferable().getLinkOption()) {
		case KEEP_LINKS:
			createSymlink();
			break;
		case COPY_LINKS:
			ln_copy();
			break;
		case COPY_UNSAFE_LINKS:
			ln_copy_unsafe();
			break;
		}
	}

	protected void ln_copy_unsafe() throws IOException, TransferException {
		if (getTransferable().isSafeLink()) {
			createSymlink();
		} else {
			ln_copy();
		}
	}

	protected void createSymlink() throws IOException {
		Transferable t = getTransferable();
		Path link = t.getDestinationPath();
		Path target = t.getSymbolicLinkTarget();
		TransferHelper.createSymbolicLink(getDestinationFileSystem(), link,
				target);
	}

	protected void ln_copy() throws IOException, TransferException {
		Transferable t = getTransferable();
		if (!t.exists()) {
			deleteDestination();
			log.warn(Messages.bind(Messages.TransferMsg_COPY_UNSAFE_IMPOSSIBLE,
					t));
			return;
		}
		template();
		chmod();
		chgrp();
	}

	protected void deleteDestination() throws IOException {
		Path path = getTransferable().getDestinationPath();
		FileSystem destFS = getDestinationFileSystem();
		if (destFS.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			if (destFS.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
				destFS.deleteDirectory(path);
			} else {
				destFS.deleteIfExists(path);
			}
		}
	}

	protected void template() throws IOException, TransferException {
		Transferable t = getTransferable();
		if (t.getTemplate() == true) {
			if (getTemplatingHandler() == null) {
				throw new TransferException(
						Messages.TransferEx_NO_TEMPLATING_HANDLER);
			}
			Path template;
			try {
				template = getTemplatingHandler().doTemplate(t.getSourcePath());
			} catch (TemplatingException Ex) {
				throw new TransferException(Ex);
			}
			transfer(template);
		} else {
			transfer(t.getSourcePath());
		}
	}

	public void transfer(Path source) throws IOException {
		Transferable t = getTransferable();
		EnhancedFileAttributes sourceFileAttrs = t.getAttributes();
		Path dest = t.getDestinationPath();
		TransferBehavior tb = t.getTransferBehavior();
		if (TransferHelper.ensureDestinationIsRegularFile(
				getDestinationFileSystem(), sourceFileAttrs, dest, tb)) {
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_FILE_ALREADY_EXISTS);
			return;
		}
		transferFile(source, dest);
	}

	public abstract void transferFile(Path source, Path dest)
			throws IOException;

	protected void chmod() throws IOException {
		Transferable t = getTransferable();
		Path path = t.getDestinationPath();
		Modifiers modifiers = t.getModifiers();
		TransferHelper.chmod(getDestinationFileSystem(), path, modifiers);
	}

	protected void chgrp() throws IOException {
		Transferable t = getTransferable();
		Path path = t.getDestinationPath();
		GroupID group = t.getGroup();
		TransferHelper.chgrp(getDestinationFileSystem(), path, group);
	}

	protected FileSystem getSourceFileSystem() {
		return _sourceFileSystem;
	}

	private FileSystem setSourceFileSystem(FileSystem sourceFileSystem) {
		if (sourceFileSystem == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FileSystem.class.getCanonicalName()
					+ ".");
		}
		FileSystem previous = getSourceFileSystem();
		_sourceFileSystem = sourceFileSystem;
		return previous;
	}

	protected FileSystem getDestinationFileSystem() {
		return _destinationFileSystem;
	}

	private FileSystem setDestinationFileSystem(FileSystem destinationFileSystem) {
		if (destinationFileSystem == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FileSystem.class.getCanonicalName()
					+ ".");
		}
		FileSystem previous = getDestinationFileSystem();
		_destinationFileSystem = destinationFileSystem;
		return previous;
	}

	protected Transferable getTransferable() {
		return _transferable;
	}

	private Transferable setTransferable(Transferable t) {
		if (t == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ Transferable.class.getCanonicalName() + ".");
		}
		Transferable previous = getTransferable();
		_transferable = t;
		return previous;
	}

	protected TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	private TemplatingHandler setTemplatingHandler(TemplatingHandler th) {
		TemplatingHandler previous = getTemplatingHandler();
		_templatingHandler = th;
		return previous;
	}

}