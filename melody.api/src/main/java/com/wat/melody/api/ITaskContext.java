package com.wat.melody.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.w3c.dom.Node;

import com.wat.melody.api.event.TaskCreatedEvent;
import com.wat.melody.api.event.TaskFinishedEvent;
import com.wat.melody.api.event.TaskStartedEvent;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.order.OrderNameSet;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;

/**
 * <p>
 * Contains contextual method and datas an {@link ITask} will need during its
 * processing.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITaskContext {

	/**
	 * <p>
	 * Get the {@link IProcessorManager} associated to this {@link ITask}.
	 * </p>
	 * 
	 * @return the {@link IProcessorManager} associated to this {@link ITask}.
	 * 
	 */
	public IProcessorManager getProcessorManager();

	/**
	 * <p>
	 * Get the {@link PropertiesSet} associated to this {@link ITask}.
	 * </p>
	 * 
	 * @return the {@link PropertiesSet} associated to this {@link ITask}.
	 * 
	 */
	public PropertiesSet getProperties();

	/**
	 * <p>
	 * Get the {@link Node} which was used to create this {@link ITask}.
	 * </p>
	 * 
	 * @return the {@link Node} associated to this {@link ITask}.
	 * 
	 */
	public Node getNode();

	/**
	 * <p>
	 * Handle {@link IProcessorManager}'s state updates.
	 * </p>
	 * 
	 * <p>
	 * <i> * If the {@link IProcessorManager} is running, this call will return
	 * immediately ; <BR/>
	 * * If the {@link IProcessorManager} is paused, this call will be blocked
	 * until the processing is either resumed or stopped ; <BR/>
	 * * If the {@link IProcessorManager} is stopped, an
	 * {@link InterruptedException} will be raised, telling to the current
	 * thread to stop as soon as possible ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws InterruptedException
	 *             if the {@link IProcessorManager} was interrupted. Means that
	 *             {@link ITask} must cleanly stop processing as soon as
	 *             possible.
	 */
	public void handleProcessorStateUpdates() throws InterruptedException;

	/**
	 * <p>
	 * Expand the given input String and return the expanded String.
	 * </p>
	 * 
	 * @param sToExpand
	 *            is the String to expand.
	 * 
	 * @return a <code>String</code>, which represents the expanded String.
	 * 
	 * @throws ExpressionSyntaxException
	 *             if an expression cannot be expanded because it is not a valid
	 *             expression (ex: circular ref, invalid character, ...).
	 */
	public String expand(String sToExpand) throws ExpressionSyntaxException;

	/**
	 * <p>
	 * Expand the content of the {@link File} points by the given {@link Path}
	 * and return the expanded String.
	 * </p>
	 * 
	 * @param fileToExpand
	 *            is the {@link Path} of the {@link File} to expand.
	 * 
	 * @return a <code>String</code>, which represents the expanded String.
	 * 
	 * @throws ExpressionSyntaxException
	 *             if an expression cannot be expanded because it is not a valid
	 *             expression (ex: circular ref, invalid character, ...).
	 * @throws IllegalFileException
	 *             if the given {@link Path} doesn't point to a valid
	 *             {@link File}.
	 * @throws IOException
	 *             if an IO error occurred while reading the {@link File} which
	 *             is pointed by the given input {@link Path}.
	 * @throws IllegalArgumentException
	 *             if fileToExpand is <code>null</code>.
	 */
	public String expand(Path fileToExpand) throws ExpressionSyntaxException,
			IllegalFileException, IOException;

	/**
	 * <p>
	 * Create an {@link ITask} object based on the given {@link Node} and
	 * processes it.
	 * </p>
	 * <p>
	 * <i> * Generate an event {@link TaskCreatedEvent} as soon as the task is
	 * created ; <BR/>
	 * * Generate a {@link TaskStartedEvent} just before the task's processing
	 * starts ; <BR/>
	 * * Generate a {@link TaskFinishedEvent} just after the task's processing
	 * ends ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param n
	 *            is a Task (in its native {@link Node} format) to create and
	 *            process.
	 * 
	 * @throws TaskException
	 *             if an error occurred while creating the {@link ITask} (e.g.
	 *             expansion failure, mandatory attribute not provided,
	 *             attribute value rejected, ..) while creating or processing
	 *             the {@link ITask}.
	 * @throws InterruptedException
	 *             if the {@link ITask}'s processing have been interrupted.
	 *             Means the caller must cleanly stop processing as soon as
	 *             possible.
	 */
	public void processTask(Node n) throws TaskException, InterruptedException;

	/**
	 * <p>
	 * Create an {@link ITask} object based on the given {@link Node} and
	 * processes it.
	 * </p>
	 * <p>
	 * <i> * Generate an event {@link TaskCreatedEvent} as soon as the task is
	 * created ; <BR/>
	 * * Generate a {@link TaskStartedEvent} just before the task's processing
	 * starts ; <BR/>
	 * * Generate a {@link TaskFinishedEvent} just after the task's processing
	 * ends ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param n
	 *            is a Task (in its native {@link Node} format) to create and
	 *            process.
	 * @param ps
	 *            is a specific set of variables which will be used during
	 *            variable's expansion.
	 * 
	 * @throws TaskException
	 *             if an error occurred while creating the {@link ITask} (e.g.
	 *             expansion failure, mandatory attribute not provided,
	 *             attribute value rejected, ..) while creating or processing
	 *             the {@link ITask}.
	 * @throws InterruptedException
	 *             if the {@link ITask}'s processing have been interrupted.
	 *             Means the caller must cleanly stop processing as soon as
	 *             possible.
	 */
	public void processTask(Node n, PropertiesSet ps) throws TaskException,
			InterruptedException;

	/**
	 * <p>
	 * Duplicate the {@link IProcessorManager} associate to this object into a
	 * new sub-{@link IProcessorManager}, which have the capacity to process
	 * specific {@link OrderNameSet} in its own {@link ISequenceDescriptor}.
	 * </p>
	 * 
	 * @return the duplicated {@link IProcessorManager}.
	 */
	public IProcessorManager createSubProcessorManager();

}