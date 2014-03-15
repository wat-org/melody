package com.wat.melody.cloud.protectedarea;

import org.w3c.dom.Element;

import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;
import com.wat.melody.cloud.protectedarea.xml.ProtectedAreaDatasLoader;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;

/**
 * <p>
 * Decorate the given {@link ProtectedAreaController}. Add the ability to update
 * the related Protected Area {@link Element}'s datas when the related
 * {@link ProtectedAreaController} changes.
 * 
 * On create/destroy, Cloud Providers dynamically allocate/release id to
 * Protected Area. This class will update the related Protected Area
 * {@link Element}'s datas accordingly.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProtectedAreaControllerRelatedToAProtectedAreaElement extends
		BaseProtectedAreaController implements ProtectedAreaControllerListener {

	private ProtectedAreaController _protectedAreaController;
	private Element _protectedAreaElement;

	public ProtectedAreaControllerRelatedToAProtectedAreaElement(
			ProtectedAreaController protectedArea,
			Element relatedProtectedAreaElmt) {
		setProtectedAreaController(protectedArea);
		setProtectedAreaElement(relatedProtectedAreaElmt);
		protectedArea.addListener(this);
	}

	private ProtectedAreaController getProtectedAreaController() {
		return _protectedAreaController;
	}

	private ProtectedAreaController setProtectedAreaController(
			ProtectedAreaController protectedAreaController) {
		if (protectedAreaController == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid "
					+ ProtectedAreaController.class.getCanonicalName() + ".");
		}
		ProtectedAreaController previous = getProtectedAreaController();
		_protectedAreaController = protectedAreaController;
		return previous;
	}

	private Element getProtectedAreaElement() {
		return _protectedAreaElement;
	}

	private Element setProtectedAreaElement(Element relatedElmt) {
		if (relatedElmt == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		Element previous = getProtectedAreaElement();
		_protectedAreaElement = relatedElmt;
		return previous;
	}

	@Override
	public ProtectedAreaId getProtectedAreaId() {
		return getProtectedAreaController().getProtectedAreaId();
	}

	@Override
	public boolean isProtectedAreaDefined() {
		return getProtectedAreaController().isProtectedAreaDefined();
	}

	@Override
	public boolean protectedAreaExists() {
		return getProtectedAreaController().protectedAreaExists();
	}

	@Override
	public void ensureProtectedAreaIsCreated(ProtectedAreaName name,
			String description) throws ProtectedAreaException,
			InterruptedException {
		getProtectedAreaController().ensureProtectedAreaIsCreated(name,
				description);
	}

	@Override
	public void ensureProtectedAreaIsDestroyed() throws ProtectedAreaException,
			InterruptedException {
		getProtectedAreaController().ensureProtectedAreaIsDestroyed();
	}

	@Override
	public void ensureProtectedAreaContentIsUpToDate(FireWallRulesPerDevice list)
			throws ProtectedAreaException, InterruptedException {
		getProtectedAreaController().ensureProtectedAreaContentIsUpToDate(list);
	}

	@Override
	public FireWallRules getProtectedAreaFireWallRules() {
		return getProtectedAreaController().getProtectedAreaFireWallRules();
	}

	@Override
	public void onProtectedAreaCreated() throws ProtectedAreaException,
			InterruptedException {
		setData(getProtectedAreaElement(), ProtectedAreaDatasLoader.ID_ATTR,
				getProtectedAreaId().getValue());
		fireProtectedAreaCreated();
	}

	@Override
	public void onProtectedAreaDestroyed() throws ProtectedAreaException,
			InterruptedException {
		fireProtectedAreaDestroyed();
		removeData(getProtectedAreaElement(), ProtectedAreaDatasLoader.ID_ATTR);
	}

	protected void setData(Element elmt, String attr, String value) {
		if (value == null || value.length() == 0) {
			return;
		}
		elmt.setAttribute(attr, value);
	}

	protected void removeData(Element elmt, String attr) {
		elmt.removeAttribute(attr);
	}

}