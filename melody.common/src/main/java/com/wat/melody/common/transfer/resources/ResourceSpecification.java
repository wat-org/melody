package com.wat.melody.common.transfer.resources;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.exception.IllegalModifiersException;
import com.wat.melody.common.transfer.LinkOption;
import com.wat.melody.common.transfer.TransferBehavior;

/**
 * <p>
 * A {@link ResourceSpecification} describes how files and directories should be
 * transfered.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ResourceSpecification extends ResourceSelector {

	private static Modifiers createModifiers(String modifiers) {
		try {
			return Modifiers.parseString(modifiers);
		} catch (IllegalModifiersException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a FileModifiers with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static Modifiers DEFAULT_FILE_MODIFIERS = createModifiers("660");
	private static Modifiers DEFAULT_DIR_MODIFIERS = createModifiers("774");

	/**
	 * Attribute, which specifies the file's modifier.
	 */
	public static final String FILE_MODIFIERS_ATTR = "file-modifiers";

	/**
	 * Attribute, which specifies the directory's modifier.
	 */
	public static final String DIR_MODIFIERS_ATTR = "dir-modifiers";

	/**
	 * Attribute, which indicates if the matching resource should be templated
	 * or not.
	 */
	public static final String TEMPLATE_ATTR = "template";

	/**
	 * Attribute, which specifies the group to apply.
	 */
	public static final String GROUP_ATTR = "group";

	/**
	 * Attribute, which specifies the behavior when a link found.
	 */
	public static final String LINK_OPTION_ATTR = "link-option";

	/**
	 * Attribute, which specifies the behavior when the to file transfer already
	 * exists.
	 */
	public static final String TRANSFER_BEHAVIOR_ATTR = "transfer-behavior";

	/**
	 * Attribute, which specifies the path of the destination.
	 */
	public static final String DEST_PATH_ATTR = "dest-path";

	// Mandatory (with a default value)
	private Modifiers _fileModifiers = DEFAULT_FILE_MODIFIERS;
	private Modifiers _dirModifiers = DEFAULT_DIR_MODIFIERS;
	private LinkOption _linkOption = LinkOption.KEEP_LINKS;
	private TransferBehavior _transferBehavior = TransferBehavior.OVERWRITE_IF_SRC_NEWER;
	private boolean _template = false;
	// Optional
	private GroupID _group = null;
	private String _destPath = null;

	public ResourceSpecification() {
		super();
	}

	public ResourceSpecification(ResourceSpecification r) {
		super();
		setFileModifiers(r.getFileModifiers());
		setDirModifiers(r.getDirModifiers());
		setLinkOption(r.getLinkOption());
		setTransferBehavior(r.getTransferBehavior());
		setTemplate(r.getTemplate());
		if (r.getGroup() != null) {
			setGroup(r.getGroup());
		}
		if (r.getDestPath() != null) {
			setDestPath(r.getDestPath());
		}
	}

	public Modifiers getFileModifiers() {
		return _fileModifiers;
	}

	@Attribute(name = FILE_MODIFIERS_ATTR)
	public Modifiers setFileModifiers(Modifiers modifiers) {
		if (modifiers == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Modifiers.class.getCanonicalName()
					+ ".");
		}
		Modifiers previous = getFileModifiers();
		_fileModifiers = modifiers;
		return previous;
	}

	public Modifiers getDirModifiers() {
		return _dirModifiers;
	}

	@Attribute(name = DIR_MODIFIERS_ATTR)
	public Modifiers setDirModifiers(Modifiers modifiers) {
		if (modifiers == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Modifiers.class.getCanonicalName()
					+ ".");
		}
		Modifiers previous = getDirModifiers();
		_dirModifiers = modifiers;
		return previous;
	}

	public LinkOption getLinkOption() {
		return _linkOption;
	}

	@Attribute(name = LINK_OPTION_ATTR)
	public LinkOption setLinkOption(LinkOption linkOption) {
		if (linkOption == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + LinkOption.class.getCanonicalName()
					+ ".");
		}
		LinkOption previous = getLinkOption();
		_linkOption = linkOption;
		return previous;
	}

	public TransferBehavior getTransferBehavior() {
		return _transferBehavior;
	}

	@Attribute(name = TRANSFER_BEHAVIOR_ATTR)
	public TransferBehavior setTransferBehavior(
			TransferBehavior transferBehavior) {
		if (transferBehavior == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TransferBehavior.class.getCanonicalName() + ".");
		}
		TransferBehavior previous = getTransferBehavior();
		_transferBehavior = transferBehavior;
		return previous;
	}

	public boolean getTemplate() {
		return _template;
	}

	@Attribute(name = TEMPLATE_ATTR)
	public boolean setTemplate(boolean yesno) {
		boolean previous = getTemplate();
		_template = yesno;
		return previous;
	}

	public GroupID getGroup() {
		return _group;
	}

	@Attribute(name = GROUP_ATTR)
	public GroupID setGroup(GroupID group) {
		if (group == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + GroupID.class.getCanonicalName()
					+ ".");
		}
		GroupID previous = getGroup();
		_group = group;
		return previous;
	}

	public String getDestPath() {
		return _destPath;
	}

	@Attribute(name = DEST_PATH_ATTR)
	public String setDestPath(String DestPath) {
		if (DestPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " " + "(the destination path).");
		}
		String previous = getDestPath();
		_destPath = DestPath;
		return previous;
	}

}