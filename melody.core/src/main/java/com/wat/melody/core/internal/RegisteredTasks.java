package com.wat.melody.core.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.ISequenceDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskBuilder;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.order.OrderNameSet;
import com.wat.melody.common.order.exception.IllegalOrderNameException;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.common.xpath.XPathExpander;
import com.wat.melody.core.internal.taskbuilder.AllCondition;
import com.wat.melody.core.internal.taskbuilder.AnyCondition;
import com.wat.melody.core.internal.taskbuilder.SourceShortcutBuilder;
import com.wat.melody.core.internal.taskbuilder.ICondition;
import com.wat.melody.core.internal.taskbuilder.JavaTaskBuilder;
import com.wat.melody.core.internal.taskbuilder.MatchCondition;
import com.wat.melody.core.internal.taskbuilder.TrueCondition;
import com.wat.melody.core.nativeplugin.attributes.RemoveAttribute;
import com.wat.melody.core.nativeplugin.attributes.SetAttributeValue;
import com.wat.melody.core.nativeplugin.call.Call;
import com.wat.melody.core.nativeplugin.foreach.Foreach;
import com.wat.melody.core.nativeplugin.order.Order;
import com.wat.melody.core.nativeplugin.property.Property;
import com.wat.melody.core.nativeplugin.sequence.Sequence;
import com.wat.melody.core.nativeplugin.source.Source;
import com.wat.melody.core.nativeplugin.synchronize.Synchronize;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class RegisteredTasks extends Hashtable<String, List<ITaskBuilder>>
		implements IRegisteredTasks {

	private static final long serialVersionUID = 3001756548734600804L;

	/**
	 * <p>
	 * Tests if the given subject {@link Class} is a valid {@link ITask}.
	 * </p>
	 * 
	 * @param c
	 *            is the subject {@link Class} to test.
	 * 
	 * @return <tt>true</tt> if the given subject {@link Class} is a valid
	 *         {@link ITask}, or <tt>false</tt> if not.
	 */
	public static boolean isValidTask(Class<? extends ITask> c) {
		if (!Modifier.isPublic(c.getModifiers())
				|| Modifier.isAbstract(c.getModifiers())) {
			return false;
		}
		for (Constructor<?> ctor : c.getConstructors()) {
			// If the Class have a public no-argument constructor
			if (ctor.getParameterTypes().length == 0
					&& Modifier.isPublic(ctor.getModifiers())
					&& !Modifier.isAbstract(ctor.getModifiers())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Contain all native Task Java Classes : {@link Sequence}, {@link Order},
	 * {@link Call}, {@link Foreach}, {@link Property},
	 * {@link SetAttributeValue}, {@link RemoveAttribute}.
	 * </p>
	 */
	public RegisteredTasks() {
		registerJavaTask(Sequence.class);
		registerJavaTask(Order.class);
		registerJavaTask(Call.class);
		registerJavaTask(Foreach.class);
		registerJavaTask(Source.class);
		registerJavaTask(Synchronize.class);
		registerJavaTask(Property.class);
		registerJavaTask(SetAttributeValue.class);
		registerJavaTask(RemoveAttribute.class);
	}

	@Override
	public void registerJavaTask(Class<? extends ITask> c) {
		register(new JavaTaskBuilder(c));
	}

	@Override
	public void registerExtension(String extensionPath,
			String defaultSequenceDescriptorPath) throws IllegalDocException,
			IllegalFileException, IllegalOrderException,
			IllegalOrderNameException, IOException {
		if (extensionPath == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			FS.validateFileExists(extensionPath + "/META-INF/melody.xml");
		} catch (IllegalFileException Ex) {
			// the extension doesn't contains a descriptor && default sd == null
			if (defaultSequenceDescriptorPath == null) {
				return;
			}
			// the extension doesn't contains a descriptor: load the default sd
			ISequenceDescriptor sd = new SequenceDescriptor();
			sd.load(defaultSequenceDescriptorPath);
			OrderNameSet orders = Order.findAvailableOrderNames(sd);
			ICondition c = new TrueCondition();
			for (OrderName order : orders) {
				register(new SourceShortcutBuilder(order, sd.getSourceFile(), c));
			}

			return;
		}

		Doc doc = new Doc();
		try {
			doc.load(extensionPath + "/META-INF/melody.xml");
		} catch (MelodyException ignored) {
			throw new RuntimeException("cannot happened.");
		}
		NodeList nl = null;
		try {
			nl = doc.evaluateAsNodeList("/melody-extension/sequence-descriptors/sequence-descriptor");
		} catch (XPathExpressionException e) {
			throw new RuntimeException("cannot happened.");
		}
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element e = (Element) nl.item(i);
			ISequenceDescriptor sd = retrieveSequenceDescriptor(e,
					extensionPath);
			OrderNameSet orders = Order.findAvailableOrderNames(sd);
			ICondition c = retrieveSequenceDescriptorCondition(e);
			for (OrderName order : orders) {
				register(new SourceShortcutBuilder(order, sd.getSourceFile(), c));
			}
		}
	}

	private static ISequenceDescriptor retrieveSequenceDescriptor(Element e,
			String extensionPath) throws IllegalDocException,
			IllegalFileException, IllegalOrderException, IOException {
		String sdpath = null;
		try {
			sdpath = XPathExpander.evaluateAsString("@path", e);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("cannot happened.");
		}
		ISequenceDescriptor sd = new SequenceDescriptor();
		sd.load(new File(extensionPath + "/" + sdpath).getAbsolutePath());
		return sd;
	}

	private static ICondition retrieveSequenceDescriptorCondition(Element e) {
		NodeList ocnl = null;
		try {
			ocnl = XPathExpander.evaluateAsNodeList("condition", e);
			if (ocnl == null || ocnl.getLength() == 0) {
				return new TrueCondition();
			}
			AnyCondition anyc = new AnyCondition();
			for (int i = 0; i < ocnl.getLength(); i++) {
				if (ocnl.item(i).getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				AllCondition allc = new AllCondition();
				anyc.add(allc);
				Element oce = (Element) ocnl.item(i);
				NodeList mcnl = null;
				mcnl = XPathExpander.evaluateAsNodeList("match", oce);
				for (int j = 0; j < mcnl.getLength(); j++) {
					if (mcnl.item(j).getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					Element mce = (Element) mcnl.item(j);
					String expression = XPathExpander.evaluateAsString(
							"@expression", mce);
					String value = XPathExpander
							.evaluateAsString("@value", mce);
					MatchCondition mc = new MatchCondition(expression, value);
					allc.add(mc);
				}
			}
			return anyc;
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("cannot happened.");
		}
	}

	public void register(ITaskBuilder tb) {
		if (tb == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid "
					+ ITaskBuilder.class.getCanonicalName() + ".");
		}

		Class<? extends ITask> c = tb.getTaskClass();
		if (!isValidTask(c)) {
			throw new RuntimeException("The given Java Class is not a "
					+ "valid " + ITask.class.getCanonicalName()
					+ ". This Java Class is either not public, or abstract "
					+ "or it as no 0-arg constructor.");
		}

		String taskName = tb.getTaskName();
		List<ITaskBuilder> tbs = get(taskName);
		// initialize the ITaskBuilder List if needed
		if (tbs == null) {
			tbs = new ArrayList<ITaskBuilder>();
			super.put(taskName, tbs);
		}
		// add the given ItaskBuilder at the end of the list
		tbs.add(tb);
	}

	public List<ITaskBuilder> get(String taskName) {
		if (taskName == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a Task Name).");
		}
		return super.get(taskName.toLowerCase());
	}

	@Override
	public ITaskBuilder retrieveEligibleTaskBuilder(String taskName,
			Element elmt, PropertySet ps) {
		if (elmt == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ " (a Task Name).");
		}
		if (ps == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + PropertySet.class.getCanonicalName()
					+ " (a Task Name).");
		}
		List<ITaskBuilder> tbs = get(taskName);
		if (tbs == null) {
			return null;
		}
		for (ITaskBuilder tb : tbs) {
			if (tb.isEligible(elmt, ps)) {
				return tb;
			}
		}
		// should never go here
		return null;
	}

}