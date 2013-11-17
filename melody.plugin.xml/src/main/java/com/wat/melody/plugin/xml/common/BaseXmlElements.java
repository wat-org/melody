package com.wat.melody.plugin.xml.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITask;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.WrapperPath;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.common.xpath.XPathFunctionHelper;
import com.wat.melody.plugin.xml.common.exception.XmlPluginException;
import com.wat.melody.plugin.xml.common.types.Condition;
import com.wat.melody.plugin.xml.common.types.ConditionIfExists;
import com.wat.melody.plugin.xml.common.types.ConditionIfNotExists;
import com.wat.melody.plugin.xml.common.types.ElementsSelector;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class BaseXmlElements implements ITask {

	private static Logger log = LoggerFactory.getLogger(BaseXmlElements.class);

	public static final String FILE_ATTR = "file";
	public static final String GREP_ATTR = "grep";
	public static final String ELEMENTS_SELECTOR_NE = "elements-selector";
	public static final String ONLY_IF_EXISTS_NE = "only-if-exists";
	public static final String ONLY_IF_NOT_EXISTS_NE = "only-if-not-exists";

	private Doc _preparedDoc = null;
	private String _grep = "[update-xml]";
	private Path _preparedFile = null;
	private List<Element> _selectedElements = new ArrayList<Element>();
	private List<Condition> _conditions = new ArrayList<Condition>();

	public BaseXmlElements() {
		super();
	}

	@Override
	public void validate() throws XmlPluginException {
		// nothing to do
	}

	@Override
	public void doProcessing() throws XmlPluginException {
		// nothing to do
	}

	protected boolean shouldProcess() {
		if (!allConditionsMatches()) {
			log.debug(Msg.bind(Messages.ApplyMsg_CONDITION_NOT_MATCH,
					getExtraLogInfo()));
			return false;
		}

		if (getSelectedElements().size() == 0) {
			log.debug(Msg.bind(Messages.ApplyMsg_NO_MATCH, getExtraLogInfo()));
			return false;
		}
		return true;
	}

	protected boolean allConditionsMatches() {
		for (Condition c : getConditions()) {
			if (!c.matches(getPreparedDoc())) {
				return false;
			}
		}
		return true;
	}

	protected void loadPreparedDoc(Path path) throws XmlPluginException {
		try {
			// the file must exists
			FS.validateFileExists(path.toString());

			// the file must be xml
			Doc doc = new Doc();
			doc.load(path.toString());
			setPreparedDoc(doc);

			// remove useless Text Nodes
			DocHelper.removeTextNode((Element) doc.getDocument()
					.getFirstChild());
		} catch (MelodyException | IOException Ex) {
			throw new XmlPluginException(Msg.bind(
					Messages.XmlFileEx_ERROR_WHILE_LOADING, path), Ex);
		}
	}

	protected void savePreparedDoc() throws XmlPluginException {
		try {
			String jbossConfigStr = getPreparedDoc().dump().replaceAll(
					" xmlns=\"\"", "");
			byte[] jbossConfigBytes = jbossConfigStr.getBytes();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File(getPreparedDoc()
						.getSourceFile()));
				fos.write(jbossConfigBytes, 0, jbossConfigBytes.length);
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		} catch (IOException Ex) {
			throw new XmlPluginException(Msg.bind(
					Messages.XmlFileEx_IO_WHILE_SAVING, getPreparedDoc()
							.getSourceFile()), Ex);
		}
	}

	public Doc getPreparedDoc() {
		return _preparedDoc;
	}

	public Doc setPreparedDoc(Doc doc) {
		if (doc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Doc.class.getCanonicalName() + ".");
		}
		Doc previous = getPreparedDoc();
		_preparedDoc = doc;
		return previous;
	}

	public Path getPreparedFile() {
		return _preparedFile;
	}

	@Attribute(name = FILE_ATTR, mandatory = true)
	public Path setPreparedFile(WrapperPath path) throws IllegalDocException,
			IllegalFileException, MelodyException, IOException {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}

		// load the xml file
		loadPreparedDoc(path);

		Path previous = getPreparedFile();
		_preparedFile = path;
		return previous;
	}

	public String getExtraLogInfo() {
		return _grep;
	}

	@Attribute(name = GREP_ATTR)
	public String setExtraLogInfo(String grep) {
		if (grep == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getExtraLogInfo();
		_grep = grep;
		return previous;
	}

	public List<Element> getSelectedElements() {
		return _selectedElements;
	}

	@NestedElement(name = ELEMENTS_SELECTOR_NE, type = Type.ADD)
	public void addElementsSelector(ElementsSelector elementsSelector)
			throws XmlPluginException {
		if (elementsSelector == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ElementsSelector.class.getCanonicalName() + ".");
		}

		// elementSelector must be valid XPath expr
		String xpr = elementsSelector.getElementsSelector();
		NodeList nl = null;
		try {
			nl = getPreparedDoc().evaluateAsNodeList(xpr);
		} catch (XPathExpressionException Ex) {
			throw new XmlPluginException(Msg.bind(
					Messages.ElementsSelectorEx_NOT_XPATH, xpr), Ex);
		}

		// elementSelector must selects only Element
		try {
			getSelectedElements().addAll(XPathFunctionHelper.toElementList(nl));
		} catch (IllegalArgumentException Ex) {
			throw new XmlPluginException(Msg.bind(
					Messages.ElementsSelectorEx_NOT_MATCH_ELEMENT, xpr));
		}
	}

	public List<Condition> getConditions() {
		return _conditions;
	}

	@NestedElement(name = ONLY_IF_EXISTS_NE)
	public void addOnlyIfExistsCondition(ConditionIfExists condition)
			throws XmlPluginException {
		if (condition == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Condition.class.getCanonicalName()
					+ ".");
		}

		// condition must be valid XPath expr
		String xpr = condition.getCondition();
		try {
			getPreparedDoc().evaluateAsNodeList(xpr);
		} catch (XPathExpressionException Ex) {
			throw new XmlPluginException(Msg.bind(
					Messages.ConditionEx_NOT_XPATH, xpr), Ex);
		}

		_conditions.add(condition);
	}

	@NestedElement(name = ONLY_IF_NOT_EXISTS_NE)
	public void addOnlyIfNotExistsCondition(ConditionIfNotExists condition)
			throws XmlPluginException {
		if (condition == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Condition.class.getCanonicalName()
					+ ".");
		}

		// condition must be valid XPath expr
		String xpr = condition.getCondition();
		try {
			getPreparedDoc().evaluateAsNodeList(xpr);
		} catch (XPathExpressionException Ex) {
			throw new XmlPluginException(Msg.bind(
					Messages.ConditionEx_NOT_XPATH, xpr), Ex);
		}

		_conditions.add(condition);
	}

}