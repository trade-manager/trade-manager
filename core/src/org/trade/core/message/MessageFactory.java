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
package org.trade.core.message;

import org.trade.core.exception.ExceptionCode;
import org.trade.core.exception.ExceptionContext;
import org.trade.core.exception.ExceptionMessage;

/**
 * Exception messages represent the user friendly messages returned by the
 * system. This class should include every message that can possibly be returned
 * by the system.
 * 
 * @author Simon Allen
 */
public class MessageFactory implements IMessageFactory {

	// This is a special case and represents an internal error. It will
	// be used when other messages cannot be located.

	public final static IMessageFactory SYSTEM_ERROR = new MessageFactory(
			createDefaultMessage());

	// Start modifications here, please follow the formatting.

	public final static IMessageFactory ERROR_UNABLE_TO_PROCESS_REQUEST = new MessageFactory(
			"ERROR_UNABLE_TO_PROCESS_REQUEST");

	public final static IMessageFactory MISSING_XML_ELEMENT = new MessageFactory(
			"MISSING_XML_ELEMENT");

	public final static IMessageFactory XML_STRUCTURE_DOES_NOT_MATCH_REQUEST_TYPE = new MessageFactory(
			"XML_STRUCTURE_DOES_NOT_MATCH_REQUEST_TYPE");

	// ----------------- Used by all handlers and the security authorization
	// component
	public final static IMessageFactory ERROR_UNABLE_TO_PERFORM_SECURITY_AUTHORIZATION = new MessageFactory(
			"ERROR_UNABLE_TO_PERFORM_SECURITY_AUTHORIZATION");

	public final static IMessageFactory ERROR_UNAUTHORIZED_REQUEST = new MessageFactory(
			"ERROR_UNAUTHORIZED_REQUEST");

	// Used by the XmlAdapter class.
	public final static IMessageFactory XML_NOT_WELL_FORMED = new MessageFactory(
			"XML_NOT_WELL_FORMED");

	public final static IMessageFactory XML_REQUEST_TYPE_NOT_RECOGNIZED = new MessageFactory(
			"XML_REQUEST_TYPE_NOT_RECOGNIZED");

	private ExceptionMessage m_exceptionMessage;

	// TODO: This should be private, but it is needed for IntRequest to support
	// old valuetype mechanism

	/**
	 * Constructor for MessageFactory.
	 * 
	 * @param exceptionMessage
	 *            ExceptionMessage
	 */
	public MessageFactory(ExceptionMessage exceptionMessage) {
		m_exceptionMessage = exceptionMessage;
	}

	/**
	 * Constructor for MessageFactory.
	 * 
	 * @param indexIntoMessageFile
	 *            String
	 */
	private MessageFactory(String indexIntoMessageFile) {
		try {
			m_exceptionMessage = MessageTranslator
					.retrieveExceptionMessage(indexIntoMessageFile);
		} catch (Exception e) {
			// Log the fact we could not load the message
			// and default to the generic system error.
			m_exceptionMessage = createDefaultMessage();
		}
	}

	/**
	 * Method create.
	 * 
	 * @return ExceptionMessage
	 * @see org.trade.core.message.IMessageFactory#create()
	 */
	public ExceptionMessage create() {
		return new ExceptionMessage(m_exceptionMessage);
	}

	/**
	 * @param fieldSequence
	 *            This should be used when checking repeating groups because it
	 *            will cause a group number to be appended to each field
	 *            reference.
	 * @return ExceptionMessage
	 * @see org.trade.core.message.IMessageFactory#create(int)
	 */
	public ExceptionMessage create(int fieldSequence) {
		return new ExceptionMessage(m_exceptionMessage.getExceptionCode()
				.createSequencedCode(fieldSequence), m_exceptionMessage);
	}

	/**
	 * Convenience method to add context to the exception message.
	 * 
	 * @param exceptionContext
	 *            ExceptionContext
	 * @return ExceptionMessage
	 * @see org.trade.core.message.IMessageFactory#create(ExceptionContext)
	 */
	public ExceptionMessage create(ExceptionContext exceptionContext) {
		ExceptionMessage returnValue;
		returnValue = new ExceptionMessage(m_exceptionMessage);

		returnValue.addExceptionContext(exceptionContext);

		return returnValue;
	}

	/**
	 * Convenience method to add context to the exception message.
	 * 
	 * @param exceptionContext1
	 *            ExceptionContext
	 * @param exceptionContext2
	 *            ExceptionContext
	 * @return ExceptionMessage
	 * @see org.trade.core.message.IMessageFactory#create(ExceptionContext,
	 *      ExceptionContext)
	 */
	public ExceptionMessage create(ExceptionContext exceptionContext1,
			ExceptionContext exceptionContext2) {
		ExceptionMessage returnValue;
		returnValue = new ExceptionMessage(m_exceptionMessage);

		returnValue.addExceptionContext(exceptionContext1);
		returnValue.addExceptionContext(exceptionContext2);

		return returnValue;
	}

	/**
	 * Method createDefaultMessage.
	 * 
	 * @return ExceptionMessage
	 */
	private static final ExceptionMessage createDefaultMessage() {
		return new ExceptionMessage(new ExceptionCode("SYS0001"),
				"Unable to process request due to a system error");
	}
}
