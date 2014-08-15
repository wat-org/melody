package com.wat.melody.common.transfer.resources;

import java.util.HashMap;
import java.util.Map;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.common.transfer.LinkOption;
import com.wat.melody.common.transfer.TransferBehaviors;
import com.wat.melody.common.transfer.resources.attributes.AttributeBase;
import com.wat.melody.common.transfer.resources.attributes.AttributeDosArchive;
import com.wat.melody.common.transfer.resources.attributes.AttributeDosHidden;
import com.wat.melody.common.transfer.resources.attributes.AttributeDosReadOnly;
import com.wat.melody.common.transfer.resources.attributes.AttributeDosSystem;
import com.wat.melody.common.transfer.resources.attributes.AttributePosixGroup;
import com.wat.melody.common.transfer.resources.attributes.AttributePosixPermissions;
import com.wat.melody.common.transfer.resources.attributes.AttributePosixUser;
import com.wat.melody.common.transfer.resources.attributes.NamedAttribute;
import com.wat.melody.common.transfer.resources.attributes.Scope;

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

	/**
	 * Attribute, which specifies the behavior when the file to transfer is a
	 * link.
	 */
	public static final String LINK_OPTION_ATTR = "link-option";

	/**
	 * Attribute, which specifies the behavior when the file to transfer already
	 * exists.
	 */
	public static final String TRANSFER_BEHAVIOR_ATTR = "transfer-behavior";

	/**
	 * Attribute, which indicates if the matching resource should be templated
	 * or not.
	 */
	public static final String TEMPLATE_ATTR = "template";

	/**
	 * Attribute, which specifies the name of the destination.
	 */
	public static final String DEST_NAME_ATTR = "dest-name";

	/**
	 * Nested element, which specifies the posix-permissions of the resource to
	 * transfer.
	 */
	public static final String POSIX_PERMISSIONS_ATTIBUTE_NE = "posix-permissions";

	/**
	 * Nested element, which specifies the posix-group-id of the resource to
	 * transfer.
	 */
	public static final String POSIX_GROUP_ATTIBUTE_NE = "posix-group";

	/**
	 * Nested element, which specifies the posix-user-id of the resource to
	 * transfer.
	 */
	public static final String POSIX_USER_ATTIBUTE_NE = "posix-user";

	/**
	 * Nested element, which specifies the dos-archive of the resource to
	 * transfer.
	 */
	public static final String DOS_ARCHIVE_ATTIBUTE_NE = "dos-archive";

	/**
	 * Nested element, which specifies the dos-hidden of the resource to
	 * transfer.
	 */
	public static final String DOS_HIDDEN_ATTIBUTE_NE = "dos-hidden";

	/**
	 * Nested element, which specifies the dos-readonly of the resource to
	 * transfer.
	 */
	public static final String DOS_READONLY_ATTIBUTE_NE = "dos-readonly";

	/**
	 * Nested element, which specifies the dos-system of the resource to
	 * transfer.
	 */
	public static final String DOS_SYSTEM_ATTIBUTE_NE = "dos-system";

	/**
	 * Nested element, which specifies a named attribute of the resource to
	 * transfer.
	 */
	public static final String NAMED_ATTIBUTE_NE = "attribute";

	// Mandatory (with a default value)
	private LinkOption _linkOption = LinkOption.KEEP_LINKS;
	private TransferBehaviors _transferBehaviors = TransferBehaviors.DEFAULT;
	private boolean _template = false;
	// Optional
	private String _destName = null;
	private Map<String, AttributeBase<?>> _fileAttributes = null;
	private Map<String, AttributeBase<?>> _dirAttributes = null;
	private Map<String, AttributeBase<?>> _linkAttributes = null;
	// Optimization
	private AttributeBase<?>[] _fileAttributesCache = null;
	private AttributeBase<?>[] _dirAttributesCache = null;
	private AttributeBase<?>[] _linkAttributesCache = null;

	public ResourceSpecification() {
		super();
	}

	public ResourceSpecification(ResourceSpecification r) {
		super();
		setLinkOption(r.getLinkOption());
		setTransferBehaviors(r.getTransferBehaviors());
		setTemplate(r.getTemplate());
		setDestName(r.getDestName());
		putAllFileAttributes(r);
		putAllDirAttributes(r);
		putAllLinkAttributes(r);
	}

	private void initFileAttributesMap() {
		if (getFileAttributesMap() == null) {
			_fileAttributes = new HashMap<String, AttributeBase<?>>();
		}
	}

	private void initDirAttributesMap() {
		if (getDirAttributesMap() == null) {
			_dirAttributes = new HashMap<String, AttributeBase<?>>();
		}
	}

	private void initLinkAttributesMap() {
		if (getLinkAttributesMap() == null) {
			_linkAttributes = new HashMap<String, AttributeBase<?>>();
		}
	}

	private void putAllFileAttributes(ResourceSpecification r) {
		if (r.getFileAttributesMap() == null) {
			return;
		}
		initFileAttributesMap();
		getFileAttributesMap().putAll(r.getFileAttributesMap());
		updateFileAttributesCache();
	}

	private void putAllDirAttributes(ResourceSpecification r) {
		if (r.getDirAttributesMap() == null) {
			return;
		}
		initDirAttributesMap();
		getDirAttributesMap().putAll(r.getDirAttributesMap());
		updateDirAttributesCache();
	}

	private void putAllLinkAttributes(ResourceSpecification r) {
		if (r.getLinkAttributesMap() == null) {
			return;
		}
		initLinkAttributesMap();
		getLinkAttributesMap().putAll(r.getLinkAttributesMap());
		updateLinkAttributesCache();
	}

	private void putFileAttribute(AttributeBase<?> attribute) {
		initFileAttributesMap();
		getFileAttributesMap().put(attribute.name(), attribute);
		updateFileAttributesCache();
	}

	private void putDirAttribute(AttributeBase<?> attribute) {
		initDirAttributesMap();
		getDirAttributesMap().put(attribute.name(), attribute);
		updateDirAttributesCache();
	}

	private void putLinkAttribute(AttributeBase<?> attribute) {
		initLinkAttributesMap();
		getLinkAttributesMap().put(attribute.name(), attribute);
		updateLinkAttributesCache();
	}

	private void updateFileAttributesCache() {
		_fileAttributesCache = getFileAttributesMap().values().toArray(
				new AttributeBase<?>[0]);
	}

	private void updateDirAttributesCache() {
		_dirAttributesCache = getDirAttributesMap().values().toArray(
				new AttributeBase<?>[0]);
	}

	private void updateLinkAttributesCache() {
		_linkAttributesCache = getLinkAttributesMap().values().toArray(
				new AttributeBase<?>[0]);
	}

	public AttributeBase<?>[] getFileAttributes() {
		return _fileAttributesCache;
	}

	public AttributeBase<?>[] getDirExpectedAttributes() {
		return _dirAttributesCache;
	}

	public AttributeBase<?>[] getLinkAttributes() {
		return _linkAttributesCache;
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

	public TransferBehaviors getTransferBehaviors() {
		return _transferBehaviors;
	}

	@Attribute(name = TRANSFER_BEHAVIOR_ATTR)
	public TransferBehaviors setTransferBehaviors(
			TransferBehaviors transferBehaviors) {
		if (transferBehaviors == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TransferBehaviors.class.getCanonicalName() + ".");
		}
		TransferBehaviors previous = getTransferBehaviors();
		_transferBehaviors = transferBehaviors;
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

	public String getDestName() {
		return _destName;
	}

	@Attribute(name = DEST_NAME_ATTR)
	public String setDestName(String destName) {
		// can be null
		String previous = getDestName();
		_destName = destName;
		return previous;
	}

	public Map<String, AttributeBase<?>> getFileAttributesMap() {
		return _fileAttributes;
	}

	public Map<String, AttributeBase<?>> getDirAttributesMap() {
		return _dirAttributes;
	}

	public Map<String, AttributeBase<?>> getLinkAttributesMap() {
		return _linkAttributes;
	}

	private void putAttribute(AttributeBase<?> attr) {
		// will replace if the same if is already in
		if (attr.getScopes().contains(Scope.DIRECTORIES)) {
			putDirAttribute(attr);
		}
		if (attr.getScopes().contains(Scope.FILES)) {
			putFileAttribute(attr);
		}
		if (attr.getScopes().contains(Scope.LINKS)) {
			putLinkAttribute(attr);
		}
	}

	@NestedElement(name = POSIX_PERMISSIONS_ATTIBUTE_NE)
	public void addPosixPermissions(AttributePosixPermissions attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AttributePosixPermissions.class.getCanonicalName() + ".");
		}
		putAttribute(attr);
	}

	@NestedElement(name = POSIX_GROUP_ATTIBUTE_NE)
	public void addPosixGroup(AttributePosixGroup attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AttributePosixGroup.class.getCanonicalName() + ".");
		}
		putAttribute(attr);
	}

	@NestedElement(name = POSIX_USER_ATTIBUTE_NE)
	public void addPosixGroup(AttributePosixUser attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AttributePosixUser.class.getCanonicalName() + ".");
		}
		putAttribute(attr);
	}

	@NestedElement(name = DOS_ARCHIVE_ATTIBUTE_NE)
	public void addPosixGroup(AttributeDosArchive attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AttributeDosArchive.class.getCanonicalName() + ".");
		}
		putAttribute(attr);
	}

	@NestedElement(name = DOS_HIDDEN_ATTIBUTE_NE)
	public void addPosixGroup(AttributeDosHidden attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AttributeDosHidden.class.getCanonicalName() + ".");
		}
		putAttribute(attr);
	}

	@NestedElement(name = DOS_READONLY_ATTIBUTE_NE)
	public void addPosixGroup(AttributeDosReadOnly attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AttributeDosReadOnly.class.getCanonicalName() + ".");
		}
		putAttribute(attr);
	}

	@NestedElement(name = DOS_SYSTEM_ATTIBUTE_NE)
	public void addPosixGroup(AttributeDosSystem attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AttributeDosSystem.class.getCanonicalName() + ".");
		}
		putAttribute(attr);
	}

	@NestedElement(name = NAMED_ATTIBUTE_NE)
	public void addNamedAttribute(NamedAttribute attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NamedAttribute.class.getCanonicalName() + ".");
		}
		putAttribute(attr);
	}

}