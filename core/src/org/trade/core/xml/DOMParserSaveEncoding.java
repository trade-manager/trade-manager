/* ===========================================================
 * TradeManager : a application to trade strategies for the Java(tm) platform
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
package org.trade.core.xml;

import org.trade.core.util.XMLDOMParserWrapper;

/**
 * The DOMParserSaveEncoding class extends DOMParser. It also provides the Java
 * Encoding of the XML document by overriding the startDocument method and
 * providing a way to capture the MIME encoding from the XML document which in
 * turn is converted to the Java Encoding by the internal MIME2Java class.
 * 
 */

public class DOMParserSaveEncoding extends XMLDOMParserWrapper {
	/*
	 * Default MIME so we check the file.encoding
	 */
	String _mimeEncoding = "DEFAULT";

	public DOMParserSaveEncoding() {
		super(false, true);
	}

	private String getMimeEncoding() {
		return (_mimeEncoding);
	}

	public String getJavaEncoding() {
		String javaEncoding = null;
		final String mimeEncoding = getMimeEncoding();

		if (mimeEncoding != null) {
			if (mimeEncoding.equals("DEFAULT")) {
				javaEncoding = System.getProperty("file.encoding");
			}
		}

		if (javaEncoding == null) // Should never return null
		{
			javaEncoding = "UTF8";
		}
		return (javaEncoding);
	}
}
