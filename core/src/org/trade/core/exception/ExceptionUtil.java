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
import java.io.Serializable;
import java.util.Enumeration;

/**
 * This class is used to hold generic routines for manipulating exceptions.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class ExceptionUtil implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1278830639842508059L;

	ExceptionUtil() {
		super();
	}

	/**
	 * Method captureStackTrace.
	 * 
	 * @param t
	 *            Throwable
	 * @return String
	 */
	public static final String captureStackTrace(Throwable t) {
		String stackTrace = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(out);

		t.printStackTrace(writer);

		writer.flush();
		stackTrace = out.toString();
		return stackTrace;
	}

	/**
	 * Method nestStackTrace.
	 * 
	 * @param stackTrace
	 *            String
	 * @param t
	 *            Throwable
	 * @return String
	 */
	public static final String nestStackTrace(String stackTrace, Throwable t) {
		String newStackTrace = null;

		if (t instanceof NestingException) {
			NestingException ex = (NestingException) t;
			newStackTrace = stackTrace + ex.getStackTrace();
		} else {
			newStackTrace = stackTrace + captureStackTrace(t);
		}

		return newStackTrace;
	}

	/**
	 * Method fillInExceptionMessage.
	 * 
	 * @param nestingException
	 *            NestingException
	 * @param stackTrace
	 *            String
	 * @param errorMesg
	 *            String
	 * @return String
	 */
	public static final String fillInExceptionMessage(
			NestingException nestingException, String stackTrace,
			String errorMesg) {
		if (stackTrace != null) {
			int index = stackTrace.indexOf(':');

			String s1 = stackTrace.substring(0, index + 1);
			index = stackTrace.indexOf("\tat");
			String s2;
			if (index >= 0) {
				s2 = stackTrace.substring(index);
			} else {
				s2 = "";
			}

			// Construct the first line of the stack trace.
			StringBuffer buf = new StringBuffer();
			buf.append(s1);
			buf.append(' ');
			buf.append(errorMesg);
			buf.append('\n');

			// Construct the lines in the stack trace that display the
			// user friendly messages.
			Enumeration<?> enumeration = nestingException
					.getAllExceptionMessages();
			while (enumeration.hasMoreElements()) {
				ExceptionMessage exceptionMessage;
				exceptionMessage = (ExceptionMessage) enumeration.nextElement();

				buf.append('\t');
				buf.append(exceptionMessage.getExceptionCode());
				buf.append(": ");
				buf.append(exceptionMessage.getMessage());
				buf.append('\n');
			}

			// Construct all of the "at ..." lines.
			buf.append(s2);
			stackTrace = buf.toString();
		} else {
			stackTrace = "";
		}

		return stackTrace;
	}
}
