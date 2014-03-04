package com.wat.melody.cloud.protectedarea.xml;

import org.w3c.dom.Element;

import com.wat.melody.cloud.protectedarea.ProtectedAreaDatas;
import com.wat.melody.cloud.protectedarea.ProtectedAreaDatasValidator;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaDatasException;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProtectedAreaDatasLoader {

	/**
	 * XML attribute of a Protected Area Element Node, which define the
	 * Protected Area Identifier of the Protected Area.
	 */
	public static final String ID_ATTR = "id";

	/**
	 * XML attribute of an Protected Area Element Node, which define the region
	 * where the Protected Area is located.
	 */
	public static final String REGION_ATTR = "region";

	/**
	 * XML attribute of an Protected Area Element Node, which define the
	 * user-friendly name of the Protected Area.
	 */
	public static final String NAME_ATTR = "name";

	/**
	 * XML attribute of an Instance Element Node, which define the decription of
	 * the Protected Area.
	 */
	public static final String DESCRIPTION_ATTR = "description";

	private String loadRegion(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, REGION_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private String loadName(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, NAME_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private String loadDescription(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, DESCRIPTION_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	/**
	 * <p>
	 * Convert the given Protected Area {@link Element} into a
	 * {@link ProtectedAreaDatas}.
	 * </p>
	 * 
	 * <p>
	 * An Protected Area {@link Element} may have the attributes :
	 * <ul>
	 * <li>region : which should contains <tt>String</tt> ;</li>
	 * <li>name : which should contains <tt>String</tt> ;</li>
	 * <li>description : which should contains <tt>String</tt> ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param protectedAreaElmt
	 *            is a Protected Area {@link Element}.
	 * @param validator
	 *            is a call-back, which performs attributes's validation.
	 * 
	 * @return a {@link ProtectedAreaDatas} object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Protected Area {@link Element} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given {@link ProtectedAreaDatasValidator} is
	 *             <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the conversion failed (ex : the content of a Protected
	 *             Area {@link Element}'s attribute is not valid, or the 'herit'
	 *             XML attribute is not valid).
	 */
	public ProtectedAreaDatas load(Element protectedAreaElmt,
			ProtectedAreaDatasValidator validator) throws NodeRelatedException {
		if (protectedAreaElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		if (validator == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaDatasValidator.class.getCanonicalName()
					+ ".");
		}
		String region = loadRegion(protectedAreaElmt);
		String name = loadName(protectedAreaElmt);
		String desc = loadDescription(protectedAreaElmt);
		try {
			return new ProtectedAreaDatas(validator, region, name, desc);
		} catch (IllegalProtectedAreaDatasException Ex) {
			throw new NodeRelatedException(protectedAreaElmt, Ex);
		}
	}

}