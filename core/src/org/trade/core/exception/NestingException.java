/* ===========================================================
 * TradeManager : An application to trade strategies for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Project Info:  org.trade
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Oracle, Inc.
 * in the United States and other countries.]
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Original Author:  Simon Allen;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 */
package org.trade.core.exception;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class represents an exception capable of containing other exceptions. It
 * also supports adding multiple user-friendly messages. This is useful when you
 * want to display nice messages for the user, but you want the nested
 * (original) exception to be cached so you can log it or view it it you are a
 * developer.
 * 
 * <p>
 * Another reason you may want to use this class is to isolate dependencies
 * within your application. For example, if you are executing Java on the client
 * you do not want to expose any exceptions from a third party library. Doing so
 * would force all clients of your API to have the third party library available
 * on their machine, which might not be desirable in a distributed environment.
 * 
 * @author Simon Allen
 */
public class NestingException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1692132595046896286L;

	// ----- Member variables -----//
	public String m_exception = null;

	public String m_seedClassName = null;

	private NestingException m_nestedException = null;

	private Vector<ExceptionMessage> m_exceptionMessages = new Vector<ExceptionMessage>();

	private Vector<ExceptionContext> m_exceptionContexts = new Vector<ExceptionContext>();

	private Date m_timeStamp = new Date();

	private String m_stackTrace = null;

	/**
	 * Default constructor.
	 */
	public NestingException() {
		this(getNoExceptionMessage());
	}

	/**
	 * Constructor that takes an error message.
	 * 
	 * @param message
	 *            A user-friendly description of the exception.
	 */
	public NestingException(String message) {
		super(message);

		// init member variables
		m_stackTrace = captureStackTrace();
	}

	/**
	 * Constructor that allows nesting of another <code>Throwable</code>.
	 * 
	 * @param t
	 *            The <code>Throwable</code> to be nested.
	 */
	public NestingException(Throwable t) {
		this(t, getNoExceptionMessage());
	}

	/**
	 * Constructor allows the developer to set the exception message
	 * 
	 * @param t
	 *            java.lang.Throwable
	 * @param message
	 *            the exception message
	 */
	public NestingException(Throwable t, String message) {
		this(message);

		// Initialize member variables.
		m_exception = t.toString();
		m_stackTrace = ExceptionUtil.nestStackTrace(m_stackTrace, t);

		// Store the exception itself if it is a NestingException
		if (t instanceof NestingException) {
			m_nestedException = (NestingException) t;
			assimilateContext(m_nestedException);
		} else {
			m_seedClassName = t.getClass().toString();
		}
	}

	/**
	 * Constructor for NestingException.
	 * 
	 * @param exceptionMessage
	 *            ExceptionMessage
	 */
	public NestingException(ExceptionMessage exceptionMessage) {
		this();
		addExceptionMessage(exceptionMessage);
	}

	/**
	 * Constructor for NestingException.
	 * 
	 * @param exceptionMessage
	 *            ExceptionMessage
	 * @param exceptionContext
	 *            ExceptionContext
	 */
	public NestingException(ExceptionMessage exceptionMessage, ExceptionContext exceptionContext) {
		this();
		addExceptionMessage(exceptionMessage);
		addExceptionContext(exceptionContext);
	}

	/**
	 * Constructor.
	 * 
	 * @param exceptionMessage
	 *            The user friendly exception message.
	 * @param message
	 *            The standard message printed in stack traces.
	 */
	public NestingException(ExceptionMessage exceptionMessage, String message) {
		this(message);
		addExceptionMessage(exceptionMessage);
	}

	/**
	 * Constructor for NestingException.
	 * 
	 * @param exceptionMessage
	 *            ExceptionMessage
	 * @param exceptionContext
	 *            ExceptionContext
	 * @param message
	 *            String
	 */
	public NestingException(ExceptionMessage exceptionMessage, ExceptionContext exceptionContext, String message) {
		this(message);
		addExceptionMessage(exceptionMessage);
		addExceptionContext(exceptionContext);
	}

	/**
	 * Constructor for NestingException.
	 * 
	 * @param exceptionMessage
	 *            ExceptionMessage
	 * @param t
	 *            Throwable
	 */
	public NestingException(ExceptionMessage exceptionMessage, Throwable t) {
		this(t);
		addExceptionMessage(exceptionMessage);
	}

	/**
	 * Constructor for NestingException.
	 * 
	 * @param exceptionMessage
	 *            ExceptionMessage
	 * @param gruesomeDetails
	 *            String
	 * @param t
	 *            Throwable
	 */
	public NestingException(ExceptionMessage exceptionMessage, String gruesomeDetails, Throwable t) {
		this(t, gruesomeDetails);
		addExceptionMessage(exceptionMessage);
	}

	/**
	 * Any outstanding context from the exception provided will be added to this
	 * exception. The context in the exception provided will be cleared.
	 * 
	 * @param nestingException
	 *            NestingException
	 */
	private void assimilateContext(NestingException nestingException) {
		// Get the contexts to be added if any.
		Enumeration<ExceptionContext> enumExeptions = nestingException.getExceptionContexts();

		if (enumExeptions.hasMoreElements()) {
			// Add each of the contexts to this
			while (enumExeptions.hasMoreElements()) {
				addExceptionContext(enumExeptions.nextElement());
			}

			// Clear the context of the nesting exception.
			nestingException.clearContexts();
		}
	}

	/**
	 * Method getExceptionContexts.
	 * 
	 * @return Enumeration<ExceptionContext>
	 */
	private Enumeration<ExceptionContext> getExceptionContexts() {
		return m_exceptionContexts.elements();
	}

	private void clearContexts() {
		m_exceptionContexts = new Vector<ExceptionContext>(1);
	}

	/**
	 * Adds a message to this exception. Multiple messages can be added.
	 * 
	 * @param exceptionMessage
	 *            The user-friendly exception message.
	 * 
	 * @see ExceptionMessage
	 */
	public void addExceptionMessage(ExceptionMessage exceptionMessage) {
		m_exceptionMessages.addElement(exceptionMessage);

		// Here we loop through any outstanding contexts and apply them.
		Enumeration<ExceptionContext> enumeration;
		enumeration = m_exceptionContexts.elements();

		while (enumeration.hasMoreElements()) {
			addExceptionContext(enumeration.nextElement());
		}
	}

	/**
	 * Method addExceptionMessage.
	 * 
	 * @param exceptionMessage
	 *            ExceptionMessage
	 * @param exceptionContext
	 *            ExceptionContext
	 */
	public void addExceptionMessage(ExceptionMessage exceptionMessage, ExceptionContext exceptionContext) {
		addExceptionMessage(exceptionMessage);
		addExceptionContext(exceptionContext);
	}

	/**
	 * Adds an enumeration of <code>ExceptionMessages</code> to this exception.
	 * 
	 * @param messages
	 *            The enumeration of <code>ExceptionMessage</code> objects.
	 */
	public void addExceptionMessages(Enumeration<?> messages) {
		while (messages.hasMoreElements()) {
			ExceptionMessage m = (ExceptionMessage) messages.nextElement();
			m_exceptionMessages.addElement(m);
		}
	}

	/**
	 * Add context to exception messages within this exception. The context is
	 * effectively a named parameter used within the messages.
	 * <p>
	 * The rules:
	 * <p>
	 * 1. If this exception contains messages the context will be applied only
	 * to the most recent message. 2. If this exception contains no messages the
	 * context will be applied to the next exception message that is added if
	 * one is ever added. 3. Regardless of the outcome of (1) and (2) above, the
	 * context will be applied to ALL messages contained within any child
	 * exceptions (exceptions nested within this exception).
	 * 
	 * @param exceptionContext
	 *            ExceptionContext
	 */
	public void addExceptionContext(ExceptionContext exceptionContext) {
		ExceptionMessage mostRecent;
		mostRecent = m_exceptionMessages.lastElement();

		if (null != mostRecent) {
			// There is at least one exception message, so we add the context
			// to the most recent message.
			mostRecent.addExceptionContext(exceptionContext);
		} else {
			// There are no exception messages so we hold onto the context
			// in case a message is added later.
			m_exceptionContexts.addElement(exceptionContext);
		}

		// Regardless of whether we cached the context we will apply the
		// context recursively to any nested exceptions we find.
		if (null != m_nestedException) {
			Enumeration<?> enumeration;
			enumeration = m_nestedException.getAllExceptionMessages();

			ExceptionMessage exceptionMessage;
			while (enumeration.hasMoreElements()) {
				exceptionMessage = (ExceptionMessage) enumeration.nextElement();
				exceptionMessage.addExceptionContext(exceptionContext);
			}
		}
	}

	/**
	 * Adds a string message to this exception. Multiple messages can be
	 * contained in the exception.
	 * 
	 * @param message
	 *            The user-friendly exception message.
	 * @param code
	 *            ExceptionCode
	 */
	public void addExceptionMessage(ExceptionCode code, String message) {
		addExceptionMessage(new ExceptionMessage(code, message));
	}

	/**
	 * Adds a string message to this exception. Multiple messages can be
	 * contained in the exception.
	 * 
	 * @param message
	 *            The user-friendly exception message.
	 * @param code
	 *            ExceptionCode
	 * @param nestingException
	 *            NestingException
	 */
	public void addExceptionMessage(ExceptionCode code, String message, NestingException nestingException) {
		m_exceptionMessages.addElement(new ExceptionMessage(code, message));
		assimilateContext(nestingException);
	}

	/**
	 * Method addExceptionMessage.
	 * 
	 * @param userFriendlyMessage
	 *            ExceptionMessage
	 * @param nestingException
	 *            NestingException
	 */
	public void addExceptionMessage(ExceptionMessage userFriendlyMessage, NestingException nestingException) {
		m_exceptionMessages.addElement(userFriendlyMessage);
		assimilateContext(nestingException);
	}

	/**
	 * Remove the specified exception message from the exception.
	 * 
	 * 
	 * @param message
	 *            ExceptionMessage
	 */
	public void removeExceptionMessage(ExceptionMessage message) {
		m_exceptionMessages.removeElement(message);
	}

	/**
	 * Remove any exception messages that match the code passed.
	 * 
	 * 
	 * @param code
	 *            ExceptionCode
	 */
	public void removeExceptionMessages(ExceptionCode code) {
		Vector<ExceptionMessage> remove = new Vector<ExceptionMessage>();
		int i;
		int nbrMessages = m_exceptionMessages.size();
		for (i = 0; i < nbrMessages; i++) {
			ExceptionMessage msg = m_exceptionMessages.elementAt(i);
			if (msg.getExceptionCode().equals(code)) {
				remove.addElement(msg);
			}
		}

		int removeSize = remove.size();
		for (i = 0; i < removeSize; i++) {
			removeExceptionMessage(remove.elementAt(i));
		}
	}

	/**
	 * 
	 * @return true if the object has user friendly error messages.
	 */
	public boolean hasExceptionMessages() {
		boolean returnValue = false;

		if (m_nestedException != null) {
			if (!m_exceptionMessages.isEmpty() || m_nestedException.hasExceptionMessages()) {
				returnValue = true;
			}
		} else {
			if (!m_exceptionMessages.isEmpty()) {
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Obtain an <code>Enumeration</code> of user-friendly messages for this
	 * exception. This does not includes messages from nested exceptions.
	 * 
	 * 
	 * @return <code>Enumeration</code> of <code>ExceptionMessage</code> objects
	 *         containing user-friendly text.
	 */
	protected Enumeration<ExceptionMessage> getExceptionMessages() {
		return m_exceptionMessages.elements();
	}

	/**
	 * Obtain an <code>Enumeration</code> of messages for this exception. This
	 * includes messages from nested exceptions if any exist.
	 * 
	 * 
	 * 
	 * @return <code>Enumeration</code> of <code>ExceptionMessage</code> objects
	 *         containing exception message text. * @see
	 *         #getAllUserFriendlyMessages()
	 */
	public Enumeration<?> getAllExceptionMessages() {
		Enumerator enumMsg;

		if (m_nestedException != null) {
			enumMsg = (Enumerator) m_nestedException.getAllExceptionMessages();
			enumMsg.prependEnumeration(m_exceptionMessages.elements());
		} else {
			enumMsg = new Enumerator(m_exceptionMessages.elements());
		}

		return enumMsg;
	}

	/**
	 * Returns the time stamp of when the exception occurred
	 * 
	 * 
	 * @return time stamp
	 */
	public Date getTimeStamp() {
		return m_timeStamp;
	}

	/**
	 * Prints the stack trace for this exception to the console.
	 */
	public void printStackTrace() {
		m_stackTrace = ExceptionUtil.fillInExceptionMessage(this, m_stackTrace, getMessage());

		System.out.print(m_stackTrace);
	}

	/**
	 * Prints the stack trace for this exception into a specified
	 * <code>PrintWriter</code>.
	 * 
	 * @param writer
	 *            The <code>PrintWriter</code> to use.
	 */
	public void printStackTrace(PrintWriter writer) {
		m_stackTrace = ExceptionUtil.fillInExceptionMessage(this, m_stackTrace, getMessage());

		writer.print(m_stackTrace);
	}

	// public String getStackTrace()
	// {
	// return m_stackTrace;
	// }

	/**
	 * Method getNoExceptionMessage.
	 * 
	 * @return String
	 */
	static String getNoExceptionMessage() {
		return ("SEE NESTED EXCEPTION MESSAGE BELOW");
	}

	/**
	 * This just places the stack trace in a string.
	 * 
	 * @return String
	 */
	private String captureStackTrace() {
		String stackTrace = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(out);

		super.printStackTrace(writer);

		writer.flush();
		stackTrace = out.toString();
		return stackTrace;
	}
}
