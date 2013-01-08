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
package org.trade.core.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class contains static methods that have to do with XML. It uses the IBM
 * parser classes from 'XML for Java v.2.0.15'
 * 
 * New note 01-16-00:
 * 
 * The parser is updated to ignore DTDs and external references. It will return
 * the same dom tree regardless of external references and DTDs
 * 
 * @version $Id: XMLDOMParserWrapper.java,v 1.1 2002/03/06 22:27:55 simon Exp $
 * @author Simon Allen
 */
public class XMLDOMParserWrapper {

	// note: even though this is capitalized it is not static final
	// change this to a method variable so the whole class will be
	// thread-safe
	private static String PRINTWRITER_ENCODING = "UTF-8";

	private static boolean canonical = false; // this means that the <? xml

	// version 1.0 ?> is added, +
	// cdata is used when possible

	private DocumentBuilder m_db = null;

	private XMLDOMParserErrorHandler m_errorHandler = null;

	/** Output goes here */
	private PrintWriter m_out = null;

	/** Indent level */
	private int m_indent = 0;

	/** Indentation will be in multiples of basicIndent */
	private final String m_basicIndent = "  ";

	public static final int DEFAULT = 0;

	public static final int CREATE_STYPE_SHEET = 1;

	public static final int CREATE_KNOWLEDGE = 2;

	public static final int CREATE_RULE = 3;

	private final static Logger _log = LoggerFactory
			.getLogger(XMLDOMParserWrapper.class);

	/**
	 * Constructor for XMLDOMParserWrapper.
	 * 
	 * @param validation
	 *            boolean
	 * @param elementContentWhitespace
	 *            boolean
	 */
	public XMLDOMParserWrapper(boolean validation,
			boolean elementContentWhitespace) {
		try {

			// Step 1: create a DocumentBuilderFactory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			dbf.setValidating(validation);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(elementContentWhitespace);
			dbf.setCoalescing(true);

			// The opposite of creating entity ref nodes is expanding them
			// inline
			dbf.setExpandEntityReferences(!true);

			// Step 2: create a DocumentBuilder
			setDocumentBuilder(dbf.newDocumentBuilder());
			setErrorHandler(new XMLDOMParserErrorHandler(new PrintWriter(
					new OutputStreamWriter(System.err, PRINTWRITER_ENCODING),
					true)));
			getDocumentBuilder().setErrorHandler(getErrorHandler());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Get Document Builder
	 * 
	 * 
	 * @param db
	 *            DocumentBuilder
	 */
	private void setDocumentBuilder(DocumentBuilder db) {
		m_db = db;
	}

	/**
	 * Get Document Builder
	 * 
	 * 
	 * @return DocumentBuilder
	 */
	public DocumentBuilder getDocumentBuilder() {
		return m_db;
	}

	/**
	 * Set ErrorHandler
	 * 
	 * 
	 * @param errorHandler
	 *            XMLDOMParserErrorHandler
	 */
	private void setErrorHandler(XMLDOMParserErrorHandler errorHandler) {
		m_errorHandler = errorHandler;
	}

	/**
	 * Get Document Builder
	 * 
	 * 
	 * @return DocumentBuilder
	 */
	public XMLDOMParserErrorHandler getErrorHandler() {
		return m_errorHandler;
	}

	/**
	 * Parses an xml string, turning it into a dom tree using the specified
	 * DOMParser object.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param file
	 *            File
	 * @return Document the root of the DOM tree as a document * @exception
	 *         SAXException if the xml is not properly formed. * @throws
	 *         IOException
	 */
	public Document parse(File file) throws SAXException, IOException {
		if (null == file) {
			throw new SAXException(
					"empyt file string is sometimes represented as null");
		}

		Document doc = null;

		// Step 3: parse the input file to get a Document object
		doc = getDocumentBuilder().parse(file);

		return doc;
	}

	/**
	 * Parses an xml string, turning it into a dom tree using the specified
	 * DOMParser object.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param xml
	 *            String
	 * @return Document the root of the DOM tree as a document * @exception
	 *         SAXException if the xml is not properly formed. * @throws
	 *         IOException
	 */
	public Document parse(String xml) throws SAXException, IOException {
		if (null == xml) {
			throw new SAXException(
					"empyt xml string is sometimes represented as null");
		}

		Document doc = null;

		// Step 3: parse the input file to get a Document object
		doc = getDocumentBuilder().parse(
				new InputSource(new ByteArrayInputStream(xml.getBytes())));

		return doc;
	}

	/**
	 * Method documentToXml.
	 * 
	 * @param doc
	 *            Document
	 * @return String
	 */
	public static String documentToXml(Document doc) {
		return print(doc, DEFAULT);
	}

	/**
	 * Method documentToXml.
	 * 
	 * @param doc
	 *            Document
	 * @param function
	 *            int
	 * @return String
	 */
	public static String documentToXml(Document doc, int function) {
		return print(doc, function);
	}

	/**
	 * Method print.
	 * 
	 * @param node
	 *            Node
	 * @param function
	 *            int
	 * @return String
	 */
	private static String print(Node node, int function) {

		// is there anything to do?
		if (node == null) {
			return null;
		}

		StringBuffer xml = new StringBuffer();
		int type = node.getNodeType();
		String nodeName = null;

		switch (type) {

		// print document
		case Node.DOCUMENT_NODE: {
			String Encoding = PRINTWRITER_ENCODING;

			if ((null != Encoding) && (!"".equals(Encoding))) {
				if (true) // ! canonical )
				{
					if (Encoding.equalsIgnoreCase("DEFAULT")) {
						Encoding = "UTF-8";
					} else {
						if (Encoding.equalsIgnoreCase("Unicode")) {
							Encoding = "UTF-16";
						}
					}
				}

				// only add this if the encoding is not null or empty string
				xml.append("<?xml version=\"1.0\" encoding=\"" + Encoding
						+ "\"?>");
			} else {

				// if no endoding is speified then still add xml version 1.0
				xml.append("<?xml version=\"1.0\"?>");
			}
			Element element = ((Document) node).getDocumentElement();
			xml.append(print(element, function));

			break;
		}

		// print element with attributes
		case Node.ELEMENT_NODE: {

			nodeName = node.getNodeName();
			_log.info("nodeName" + nodeName);
			if (function == CREATE_STYPE_SHEET) {
				// nodeName = formatElementName(node.getNodeName());
				nodeName = node.getNodeName();
			} else if (function == CREATE_KNOWLEDGE) {
				nodeName = formatElementName(node.getNodeName()).toUpperCase();

				if (!"XSL:".equals(nodeName.substring(0, 4))) {
					String displayName = formatDisplayName(node.getNodeName());

					nodeName = "Attribute name=\""
							+ nodeName
							+ "\" displayName=\""
							+ displayName
							+ "\" type=\"String\" length=\"80\" precision=\"0\" required=\"false\"";
				} else {
					NodeList children = node.getChildNodes();

					if (children != null) {
						int len = children.getLength();

						for (int i = 0; i < len; i++) {
							xml.append(print(children.item(i), function));
						}
					}

					break;
				}

			} else if (function == CREATE_RULE) {
				if ("Member".equals(nodeName)) {
					nodeName = "Attribute ";
				} else {
					NodeList children = node.getChildNodes();

					if (children != null) {
						int len = children.getLength();

						for (int i = 0; i < len; i++) {
							xml.append(print(children.item(i), function));
						}
					}

					break;
				}

			}

			xml.append('<');

			xml.append(nodeName);

			Attr[] attrs = sortAttributes(node.getAttributes());

			for (Attr attr : attrs) {

				if (function == CREATE_RULE) {
					if ((attr.getNodeName().equals("class") || attr
							.getNodeName().equals("attrib"))) {
						xml.append(' ');
						xml.append(attr.getNodeName());
						xml.append("=\"");
						// xml.append(normalize(attr.getNodeValue()));
						xml.append(attr.getNodeValue());
						xml.append('"');

					}
				} else {
					xml.append(' ');
					xml.append(attr.getNodeName());
					xml.append("=\"");
					// xml.append(normalize(attr.getNodeValue()));
					xml.append(attr.getNodeValue());
					xml.append('"');
				}
			}

			if ((function == CREATE_KNOWLEDGE) || (function == CREATE_RULE)) {
				xml.append("/>");
			} else {
				xml.append('>');
			}

			NodeList children = node.getChildNodes();

			if (children != null) {
				int len = children.getLength();

				for (int i = 0; i < len; i++) {
					xml.append(print(children.item(i), function));
				}
			}

			break;
		}

		// handle entity reference nodes
		case Node.ENTITY_REFERENCE_NODE: {
			if (canonical) {
				NodeList children = node.getChildNodes();

				if (children != null) {
					int len = children.getLength();

					for (int i = 0; i < len; i++) {
						xml.append(print(children.item(i), function));
					}
				}
			} else {
				xml.append('&');
				xml.append(node.getNodeName());
				xml.append(';');
			}

			break;
		}

		// print cdata sections
		case Node.CDATA_SECTION_NODE: {

			if (canonical) {
				xml.append(normalize(node.getNodeValue()));
			} else {
				xml.append("<![CDATA[");

				xml.append(node.getNodeValue());
				xml.append("]]>");
			}

			break;
		}

		// print text
		case Node.TEXT_NODE: {
			if ((function == CREATE_STYPE_SHEET)
					&& (null != node.getNodeValue())
					&& (node.getNodeValue().trim().length() > 0)) {
				xml.append("<xsl:value-of select=\""
						+ node.getParentNode().getNodeName() + "\"/>");
			} else {
				if (function != CREATE_RULE) {
					xml.append(normalize(node.getNodeValue()));
				}

			}

			break;
		}

		// print processing instruction
		case Node.PROCESSING_INSTRUCTION_NODE: {
			xml.append("<?");
			xml.append(node.getNodeName());

			String data = node.getNodeValue();

			if ((data != null) && (data.length() > 0)) {
				xml.append(' ');
				xml.append(data);
			}

			xml.append("?>");

			break;
		}
		}

		if ((type == Node.ELEMENT_NODE) && (function != CREATE_KNOWLEDGE)
				&& (function != CREATE_RULE)) {
			xml.append("</");
			xml.append(nodeName);
			xml.append('>');
		}

		return xml.toString();
	} // print(Node)

	/**
	 * Method sortAttributes.
	 * 
	 * @param attrs
	 *            NamedNodeMap
	 * @return Attr[]
	 */
	private static Attr[] sortAttributes(NamedNodeMap attrs) {
		int len = (attrs != null) ? attrs.getLength() : 0;
		Attr[] array = new Attr[len];

		for (int i = 0; i < len; i++) {
			array[i] = (Attr) attrs.item(i);
		}

		for (int i = 0; i < (len - 1); i++) {
			String name = array[i].getNodeName();
			int index = i;

			for (int j = i + 1; j < len; j++) {
				String curName = array[j].getNodeName();

				if (curName.compareTo(name) < 0) {
					name = curName;
					index = j;
				}
			}

			if (index != i) {
				Attr temp = array[i];

				array[i] = array[index];
				array[index] = temp;
			}
		}

		return (array);
	}

	/**
	 * Method normalize.
	 * 
	 * @param s
	 *            String
	 * @return String
	 */
	private static String normalize(String s) {
		StringBuffer str = new StringBuffer();
		int len = (s != null) ? s.length() : 0;

		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);

			// weed out characters between 0 - 31 that are not
			// LF, CR, TAB all others are not allowed in XML
			// elements
			int charInt = Integer.valueOf(Integer.toString(ch)).intValue();

			if (charInt < 32) {
				if ((charInt == 10 /* line feed */) || (charInt == 13 /*
																		 * carridge
																		 * return
																		 */)
						|| (charInt == 9 /* tab */)) {
				} else {

					// set it to spaces
					ch = ' ';
				}
			}

			switch (ch) {

			case '<': {
				str.append("&lt;");

				break;
			}
			case '>': {
				str.append("&gt;");

				break;
			}
			case '&': {
				str.append("&amp;");

				break;
			}
			case '"': {
				str.append("&quot;");

				break;
			}
			case '\r':
			case '\n': {
				if (canonical) {
					str.append("&#");
					str.append(Integer.toString(ch));
					str.append(';');

					break;
				}

				// else, default append char
			}
			default: {
				str.append(ch);
			}
			}
		}

		return (str.toString());
	} // normalize(String):String

	/**
	 * Indent to the current level in multiples of basicIndent
	 */
	private void outputIndentation() {
		for (int i = 0; i < m_indent; i++) {
			m_out.print(m_basicIndent);
		}
	}

	/**
	 * Echo common attributes of a DOM2 Node and terminate output with an EOL
	 * character.
	 * 
	 * @param n
	 *            Node
	 */
	private void printlnCommon(Node n) {
		m_out.print(" nodeName=\"" + n.getNodeName() + "\"");

		String val = null;

		val = n.getNamespaceURI();

		if (val != null) {
			m_out.print(" uri=\"" + val + "\"");
		}

		val = n.getPrefix();

		if (val != null) {
			m_out.print(" pre=\"" + val + "\"");
		}

		val = n.getLocalName();

		if (val != null) {
			m_out.print(" local=\"" + val + "\"");
		}

		val = n.getNodeValue();

		if (val != null) {
			m_out.print(" nodeValue=");

			if (val.trim().equals("")) {

				// Whitespace
				m_out.print("[WS]");
			} else {
				m_out.print("\"" + n.getNodeValue() + "\"");
			}
		}

		m_out.println();
	}

	/**
	 * Recursive routine to print out DOM tree nodes
	 * 
	 * @param n
	 *            Node
	 */
	@SuppressWarnings("unused")
	private void echo(Node n) {

		// Indent to the current level before printing anything
		outputIndentation();

		int type = n.getNodeType();

		switch (type) {

		case Node.ATTRIBUTE_NODE:
			m_out.print("ATTR:");
			printlnCommon(n);
			break;

		case Node.CDATA_SECTION_NODE:
			m_out.print("CDATA:");
			printlnCommon(n);
			break;

		case Node.COMMENT_NODE:
			m_out.print("COMM:");
			printlnCommon(n);
			break;

		case Node.DOCUMENT_FRAGMENT_NODE:
			m_out.print("DOC_FRAG:");
			printlnCommon(n);
			break;

		case Node.DOCUMENT_NODE:
			m_out.print("DOC:");
			printlnCommon(n);
			break;

		case Node.DOCUMENT_TYPE_NODE:
			m_out.print("DOC_TYPE:");
			printlnCommon(n);

			// Print entities if any
			NamedNodeMap nodeMap = ((DocumentType) n).getEntities();

			m_indent += 2;

			for (int i = 0; i < nodeMap.getLength(); i++) {
				Entity entity = (Entity) nodeMap.item(i);

				echo(entity);
			}

			m_indent -= 2;
			break;

		case Node.ELEMENT_NODE:
			m_out.print("ELEM:");
			printlnCommon(n);

			// Print attributes if any. Note: element attributes are not
			// children of ELEMENT_NODEs but are properties of their
			// associated ELEMENT_NODE. For this reason, they are printed
			// with 2x the indent level to indicate this.
			NamedNodeMap atts = n.getAttributes();

			m_indent += 2;

			for (int i = 0; i < atts.getLength(); i++) {
				Node att = atts.item(i);

				echo(att);
			}

			m_indent -= 2;
			break;

		case Node.ENTITY_NODE:
			m_out.print("ENT:");
			printlnCommon(n);
			break;

		case Node.ENTITY_REFERENCE_NODE:
			m_out.print("ENT_REF:");
			printlnCommon(n);
			break;

		case Node.NOTATION_NODE:
			m_out.print("NOTATION:");
			printlnCommon(n);
			break;

		case Node.PROCESSING_INSTRUCTION_NODE:
			m_out.print("PROC_INST:");
			printlnCommon(n);
			break;

		case Node.TEXT_NODE:
			m_out.print("TEXT:");
			printlnCommon(n);
			break;

		default:
			m_out.print("UNSUPPORTED NODE: " + type);
			printlnCommon(n);
			break;
		}

		// Print children if any
		m_indent++;

		for (Node child = n.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			echo(child);
		}

		m_indent--;
	}

	/**
	 * Method formatDisplayName.
	 * 
	 * @param nodeName
	 *            String
	 * @return String
	 */
	private static String formatDisplayName(String nodeName) {
		char[] val = nodeName.toCharArray();
		int position = 0;
		StringBuffer newNodeName = new StringBuffer(nodeName);

		for (int i = 0; i < val.length; i++) {
			if (!Character.isLetterOrDigit(val[i]) || (i == 0)) {
				String replace = null;

				if (i == 0) {
					replace = "" + Character.toUpperCase(val[i]);

					newNodeName.replace(position, position + 1, replace);
				} else {
					replace = " " + Character.toUpperCase(val[i + 1]);

					newNodeName.replace(position, position + 2, replace);
				}
			}

			position++;
		}

		return newNodeName.toString();
	}

	/**
	 * Method formatElementName.
	 * 
	 * @param nodeName
	 *            String
	 * @return String
	 */
	private static String formatElementName(String nodeName) {
		char[] val = nodeName.toCharArray();
		int position = 0;
		StringBuffer newNodeName = new StringBuffer(nodeName);

		for (int i = 0; i < val.length; i++) {
			if (Character.isUpperCase(val[i])) {
				String replace = null;

				if (i == 0) {
					replace = "" + Character.toLowerCase(val[i]);

					newNodeName.replace(position, position + 1, replace);
				} else {
					replace = "_" + Character.toLowerCase(val[i]);

					newNodeName.replace(position, position + 1, replace);

					position++;
				}
			}

			position++;
		}

		return newNodeName.toString();
	}

	/**
	 */
	private static class XMLDOMParserErrorHandler implements ErrorHandler {
		/** Error handler output goes here */
		private PrintWriter out;

		/**
		 * Constructor for XMLDOMParserErrorHandler.
		 * 
		 * @param out
		 *            PrintWriter
		 */
		XMLDOMParserErrorHandler(PrintWriter out) {
			this.out = out;
		}

		/**
		 * Returns a string describing parse exception details
		 * 
		 * @param spe
		 *            SAXParseException
		 * @return String
		 */
		private String getParseExceptionInfo(SAXParseException spe) {
			String systemId = spe.getSystemId();

			if (systemId == null) {
				systemId = "null";
			}

			String info = "URI=" + systemId + " Line=" + spe.getLineNumber()
					+ ": " + spe.getMessage();

			return info;
		}

		// The following methods are standard SAX ErrorHandler methods.
		// See SAX documentation for more info.
		/**
		 * Method warning.
		 * 
		 * @param spe
		 *            SAXParseException
		 * @throws SAXException
		 * @see org.xml.sax.ErrorHandler#warning(SAXParseException)
		 */
		public void warning(SAXParseException spe) throws SAXException {
			out.println("Warning: " + getParseExceptionInfo(spe));
		}

		/**
		 * Method error.
		 * 
		 * @param spe
		 *            SAXParseException
		 * @throws SAXException
		 * @see org.xml.sax.ErrorHandler#error(SAXParseException)
		 */
		public void error(SAXParseException spe) throws SAXException {
			String message = "Error: " + getParseExceptionInfo(spe);

			throw new SAXException(message);
		}

		/**
		 * Method fatalError.
		 * 
		 * @param spe
		 *            SAXParseException
		 * @throws SAXException
		 * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
		 */
		public void fatalError(SAXParseException spe) throws SAXException {
			String message = "Fatal Error: " + getParseExceptionInfo(spe);

			throw new SAXException(message);
		}
	}
}
