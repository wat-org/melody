package com.wat.melody.common.transfer.resources;

import java.util.HashMap;
import java.util.Map;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
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
	 * Nested element, which specifies the attributes of the resource to
	 * transfer.
	 */
	public static final String ATTIBUTE_NE = "attribute";

	/**
	 * Nested element, which specifies the attributes of the resource to
	 * transfer.
	 */
	public static final String FILE_ONLY_ATTIBUTE_NE = "file-attribute";

	/**
	 * Nested element, which specifies the attributes of the resource to
	 * transfer.
	 */
	public static final String DIR_ONLY_ATTIBUTE_NE = "dir-attribute";

	// Mandatory (with a default value)
	private LinkOption _linkOption = LinkOption.KEEP_LINKS;
	private TransferBehavior _transferBehavior = TransferBehavior.OVERWRITE_IF_SRC_NEWER;
	private boolean _template = false;
	// Optional
	private String _destName = null;
	private Map<String, ResourceAttribute> _fileExpectedAttributes = null;
	private Map<String, ResourceAttribute> _dirExpectedAttributes = null;
	// Optimization
	private ResourceAttribute[] _fileExpectedAttributesCache = null;
	private ResourceAttribute[] _dirExpectedAttributesCache = null;

	public ResourceSpecification() {
		super();
	}

	public ResourceSpecification(ResourceSpecification r) {
		super();
		setLinkOption(r.getLinkOption());
		setTransferBehavior(r.getTransferBehavior());
		setTemplate(r.getTemplate());
		setDestName(r.getDestName());
		putAllFileExpectedAttributes(r);
		putAllDirExpectedAttributes(r);
	}

	private void initFileExpectedAttributesMap() {
		if (getFileExpectedAttributesMap() == null) {
			_fileExpectedAttributes = new HashMap<String, ResourceAttribute>();
		}
	}

	private void updateFileExpectedAttributesCache() {
		_fileExpectedAttributesCache = getFileExpectedAttributesMap()
				.values()
				.toArray(
						_fileExpectedAttributesCache == null ? new ResourceAttribute[0]
								: _fileExpectedAttributesCache);
	}

	private void putAllFileExpectedAttributes(ResourceSpecification r) {
		if (r.getFileExpectedAttributesMap() == null) {
			return;
		}
		initFileExpectedAttributesMap();
		getFileExpectedAttributesMap().putAll(r.getFileExpectedAttributesMap());
		updateFileExpectedAttributesCache();
	}

	private void putFileExpectedAttributes(ResourceAttribute attribute) {
		initFileExpectedAttributesMap();
		getFileExpectedAttributesMap().put(attribute.name(), attribute);
		updateFileExpectedAttributesCache();
	}

	public ResourceAttribute[] getFileExpectedAttributes() {
		return _fileExpectedAttributesCache;
	}

	private void initDirExpectedAttributesMap() {
		if (getDirExpectedAttributesMap() == null) {
			_dirExpectedAttributes = new HashMap<String, ResourceAttribute>();
		}
	}

	private void updateDirExpectedAttributesCache() {
		_dirExpectedAttributesCache = getDirExpectedAttributesMap()
				.values()
				.toArray(
						_dirExpectedAttributesCache == null ? new ResourceAttribute[0]
								: _dirExpectedAttributesCache);
	}

	private void putAllDirExpectedAttributes(ResourceSpecification r) {
		if (r.getDirExpectedAttributesMap() == null) {
			return;
		}
		initDirExpectedAttributesMap();
		getDirExpectedAttributesMap().putAll(r.getDirExpectedAttributesMap());
		updateDirExpectedAttributesCache();
	}

	private void putDirExpectedAttributes(ResourceAttribute attribute) {
		initDirExpectedAttributesMap();
		getDirExpectedAttributesMap().put(attribute.name(), attribute);
		updateDirExpectedAttributesCache();
	}

	public ResourceAttribute[] getDirExpectedAttributes() {
		return _dirExpectedAttributesCache;
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

	public String getDestName() {
		return _destName;
	}

	@Attribute(name = DEST_NAME_ATTR)
	public String setDestName(String DestPath) {
		// can be null
		String previous = getDestName();
		_destName = DestPath;
		return previous;
	}

	@NestedElement(name = ATTIBUTE_NE)
	public void addAttribute(ResourceAttribute attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ResourceAttribute.class.getCanonicalName() + ".");
		}
		// will replace if the same if is already in
		putFileExpectedAttributes(attr);
		putDirExpectedAttributes(attr);
	}

	public Map<String, ResourceAttribute> getFileExpectedAttributesMap() {
		return _fileExpectedAttributes;
	}

	@NestedElement(name = FILE_ONLY_ATTIBUTE_NE)
	public void addFileAttribute(ResourceAttribute attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ResourceAttribute.class.getCanonicalName() + ".");
		}
		// will replace if the same if is already in
		putFileExpectedAttributes(attr);
	}

	public Map<String, ResourceAttribute> getDirExpectedAttributesMap() {
		return _dirExpectedAttributes;
	}

	@NestedElement(name = DIR_ONLY_ATTIBUTE_NE)
	public void addDirAttribute(ResourceAttribute attr) {
		if (attr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ResourceAttribute.class.getCanonicalName() + ".");
		}
		// will replace if the same if is already in
		putDirExpectedAttributes(attr);
	}

}