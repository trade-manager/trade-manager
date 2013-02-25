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

import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public abstract class SaxMapper extends DefaultHandler {
	/*
	 * Must be overridden by all subclasses...
	 */
	public abstract Object getMappedObject();

	public abstract TagTracker createTagTrackerNetwork() throws ParseException;

	// A stack for the tag trackers to coordinate on.
	private final Stack<TagTracker> tagStack = new Stack<TagTracker>();

	// The SAX 2 parser...
	private XMLReader xr;

	// Buffer for collecting data from the "characters" SAX event.
	private final CharArrayWriter contents = new CharArrayWriter();

	public SaxMapper() throws XMLModelException {
		try {

			final SAXParserFactory spf = SAXParserFactory.newInstance();
			// Create a JAXP SAXParser
			final SAXParser saxParser = spf.newSAXParser();

			// Get the encapsulated SAX XMLReader
			xr = saxParser.getXMLReader();
			/*
			 * Create the XML reader... Create the tag tracker network and
			 * initialize the stack with
			 * 
			 * it. This constructor anchors the tag tracking network to the
			 * beginning of the XML document. ( before the first tag name is
			 * located ).
			 * 
			 * By placing it first on the stack all future tag tracking will
			 * follow the network anchored by this root tag tracker.
			 * 
			 * The createTagTrackerNetwork() method is abstract. All subclasses
			 * are responsible for reacting to this request with the creation of
			 * a tag tracking network that will perform the mapping for the
			 * subclass.
			 */
			tagStack.push(createTagTrackerNetwork());

		} catch (final Exception e) {
			throw new XMLModelException(e);
		}
	}

	public Object fromXML(String url) throws XMLModelException {

		try {
			return fromXML(new InputSource(url));
		} catch (final Exception e) {
			throw new XMLModelException(e);
		}
	}

	public Object fromXML(InputStream in) throws XMLModelException {
		try {
			return fromXML(new InputSource(in));
		} catch (final Exception e) {
			throw new XMLModelException(e);
		}
	}

	public Object fromXML(Reader in) throws XMLModelException {
		try {
			return fromXML(new InputSource(in));
		} catch (final Exception e) {
			throw new XMLModelException(e);
		}
	}

	private synchronized Object fromXML(InputSource in) throws Exception {

		/*
		 * 1. The calling "fromXML" methods catch any parsing exceptions.
		 * 
		 * 2. The method is synchronized to keep multiple threads from accessing
		 * the XML parser at once. This is a limitation imposed by SAX.
		 * 
		 * Set the ContentHandler...
		 */
		xr.setContentHandler(this);
		// Parse the file...
		xr.parse(in);

		return getMappedObject();
	}

	/*
	 * Implement the content handler methods that will delegate SAX events to
	 * the tag tracker network.(non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes attr) throws SAXException {
		/*
		 * Resetting contents buffer. Assuming that tags either tag content or
		 * children, not both. This is usually the case with XML that is
		 * representing data structures in a programming language independent
		 * way. This assumption is not typically valid where XML is being used
		 * in the classical text mark up style where tagging is used to style
		 * content and several styles may overlap at once.
		 */
		contents.reset();

		// delegate the event handling to the tag tracker network.
		final TagTracker activeTracker = (TagTracker) tagStack.peek();
		activeTracker.startElement(namespaceURI, localName, qName, attr,
				tagStack);
	}

	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {

		// delegate the event handling to the tag tracker network.
		final TagTracker activeTracker = (TagTracker) tagStack.peek();
		try {
			activeTracker.endElement(namespaceURI, localName, qName, contents,
					tagStack);
		} catch (ParseException e) {
			throw new SAXException(e);
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// accumulate the contents into a buffer.
		contents.write(ch, start, length);
	}
}
