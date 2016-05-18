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
package org.trade.ui.base;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A sample DOM writer. This sample program illustrates how to traverse a DOM
 * tree in order to print a document that is parsed.
 * 
 * @version $Id: DOMWriter.java,v 1.1 2002/01/03 18:20:45 simon Exp $
 * @author Simon Allen
 */
public class DOMWriter {
	//
	// Constants
	//
	/** Default parser name. */
	// private static final String DEFAULT_PARSER_NAME =
	// "org.apache.xerces.parsers.DOMParser";

	// private static boolean setValidation = false; // defaults

	// private static boolean setNameSpaces = true;

	// private static boolean setSchemaSupport = true;

	// private static boolean setDeferredDOM = true;

	//
	// Data
	//
	/** Default Encoding */
	private static String PRINTWRITER_ENCODING = "UTF8";
	/*
	 * private static String MIME2JAVA_ENCODINGS[] = { "Default", "UTF-8",
	 * "US-ASCII", "ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4",
	 * "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8", "ISO-8859-9",
	 * "ISO-2022-JP", "SHIFT_JIS", "EUC-JP", "GB2312", "BIG5", "EUC-KR",
	 * "ISO-2022-KR", "KOI8-R", "EBCDIC-CP-US", "EBCDIC-CP-CA", "EBCDIC-CP-NL",
	 * "EBCDIC-CP-DK", "EBCDIC-CP-NO", "EBCDIC-CP-FI", "EBCDIC-CP-SE",
	 * "EBCDIC-CP-IT", "EBCDIC-CP-ES", "EBCDIC-CP-GB", "EBCDIC-CP-FR",
	 * "EBCDIC-CP-AR1", "EBCDIC-CP-HE", "EBCDIC-CP-CH", "EBCDIC-CP-ROECE",
	 * "EBCDIC-CP-YU", "EBCDIC-CP-IS", "EBCDIC-CP-AR2", "UTF-16" };
	 * 
	 * 
	 * private static String JAVA_SUPPORTED_ENCODINGS[] = { "Default", "8859_1",
	 * "8859_2", "8859_3", "8859_4", "8859_5", "8859_6", "8859_7", "8859_8",
	 * "8859_9", "Cp037", "Cp273", "Cp277", "Cp278", "Cp280", "Cp284", "Cp285",
	 * "Cp297", "Cp420", "Cp424", "Cp437", "Cp500", "Cp737", "Cp775", "Cp838",
	 * "Cp850", "Cp852", "Cp855", "Cp856", "Cp857", "Cp860", "Cp861", "Cp862",
	 * "Cp863", "Cp864", "Cp865", "Cp866", "Cp868", "Cp869", "Cp870", "Cp871",
	 * "Cp874", "Cp875", "Cp918", "Cp921", "Cp922", "Cp930", "Cp933", "Cp935",
	 * "Cp937", "Cp939", "Cp942", "Cp948", "Cp949", "Cp950", "Cp964", "Cp970",
	 * "Cp1006", "Cp1025", "Cp1026", "Cp1046", "Cp1097", "Cp1098", "Cp1112",
	 * "Cp1122", "Cp1123", "Cp1124", "Cp1250", "Cp1251", "Cp1252", "Cp1253",
	 * "Cp1254", "Cp1255", "Cp1256", "Cp1257", "Cp1258", "Cp1381", "Cp1383",
	 * "Cp33722", "MS874", "EUCJIS", "GB2312", "GBK", "ISO2022CN_CNS",
	 * "ISO2022CN_GB", "JIS", "JIS0208", "KOI8_R", "KSC5601","MS874", "SJIS",
	 * "Big5", "CNS11643", "MacArabic", "MacCentralEurope", "MacCroatian",
	 * "MacCyrillic", "MacDingbat", "MacGreek", "MacHebrew", "MacIceland",
	 * "MacRoman", "MacRomania", "MacSymbol", "MacThai", "MacTurkish",
	 * "MacUkraine", "SJIS", "Unicode", "UnicodeBig", "UnicodeLittle", "UTF8"};
	 */

	/** Print writer. */
	// protected PrintWriter m_out = null;
	protected FileWriter m_out = null;

	/** Canonical output. */
	protected boolean m_canonical = false;

	//
	// Constructors
	//
	/** Default constructor. */
	public DOMWriter() {
	}

	/**
	 * Method printDocument.
	 * 
	 * @param encoding
	 *            String
	 * @param canonical
	 *            boolean
	 * @param fileName
	 *            String
	 * @param doc
	 *            Node
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void printDocument(String encoding, boolean canonical, String fileName, Node doc)
			throws UnsupportedEncodingException, IOException {
		m_out = new FileWriter(fileName);
		m_canonical = canonical;

		if (null != encoding) {
			DOMWriter.setWriterEncoding(encoding);
		}

		print(doc);
		m_out.close();
	} // getWriterEncoding

	/**
	 * Method getWriterEncoding.
	 * 
	 * @return String
	 */
	private static String getWriterEncoding() {
		return (PRINTWRITER_ENCODING);
	} // getWriterEncoding

	/**
	 * Method setWriterEncoding.
	 * 
	 * @param encoding
	 *            String
	 */
	private static void setWriterEncoding(String encoding) {
		if (encoding.equalsIgnoreCase("DEFAULT")) {
			PRINTWRITER_ENCODING = "UTF8";
		} else if (encoding.equalsIgnoreCase("UTF-16")) {
			PRINTWRITER_ENCODING = "Unicode";
		}
	} // setWriterEncoding

	/*
	 * private static boolean isValidJavaEncoding(String encoding) { for (int i
	 * = 0; i < MIME2JAVA_ENCODINGS.length; i++) { if
	 * (encoding.equals(MIME2JAVA_ENCODINGS[i])) { return (true); } }
	 * 
	 * return (false); } // isValidJavaEncoding
	 */

	/**
	 * Prints the specified node, recursively. * @param node Node
	 * 
	 * @throws IOException
	 */
	private void print(Node node) throws IOException {
		// is there anything to do?
		if (node == null) {
			return;
		}

		int type = node.getNodeType();

		switch (type) {

		// print document
		case Node.DOCUMENT_NODE: {
			if (!m_canonical) {
				String Encoding = DOMWriter.getWriterEncoding();

				if (Encoding.equalsIgnoreCase("DEFAULT")) {
					Encoding = "UTF-8";
				} else if (Encoding.equalsIgnoreCase("Unicode")) {
					Encoding = "UTF-16";
				}

				m_out.write("<?xml version=\"1.0\" encoding=\"" + Encoding + "\"?>");
			}

			print(((Document) node).getDocumentElement());
			m_out.flush();

			break;
		}
			// print element with attributes
		case Node.ELEMENT_NODE: {
			m_out.write('<');
			m_out.write(node.getNodeName());

			Attr attrs[] = sortAttributes(node.getAttributes());

			for (Attr attr : attrs) {
				m_out.write(' ');
				m_out.write(attr.getNodeName());
				m_out.write("=\"");
				m_out.write(normalize(attr.getNodeValue()));
				m_out.write('"');
			}

			m_out.write('>');

			NodeList children = node.getChildNodes();

			if (children != null) {
				int len = children.getLength();

				for (int i = 0; i < len; i++) {
					print(children.item(i));
				}
			}

			break;
		}
			// handle entity reference nodes
		case Node.ENTITY_REFERENCE_NODE: {
			if (m_canonical) {
				NodeList children = node.getChildNodes();

				if (children != null) {
					int len = children.getLength();

					for (int i = 0; i < len; i++) {
						print(children.item(i));
					}
				}
			} else {
				m_out.write('&');
				m_out.write(node.getNodeName());
				m_out.write(';');
			}

			break;
		}
			// print cdata sections
		case Node.CDATA_SECTION_NODE: {
			if (m_canonical) {
				m_out.write(normalize(node.getNodeValue()));
			} else {
				m_out.write("<![CDATA[");
				m_out.write(node.getNodeValue());
				m_out.write("]]>");
			}

			break;
		}
			// print text
		case Node.TEXT_NODE: {
			m_out.write(normalize(node.getNodeValue()));

			break;
		}
			// print processing instruction
		case Node.PROCESSING_INSTRUCTION_NODE: {
			m_out.write("<?");
			m_out.write(node.getNodeName());

			String data = node.getNodeValue();

			if ((data != null) && (data.length() > 0)) {
				m_out.write(' ');
				m_out.write(data);
			}

			m_out.write("?>");

			break;
		}
		}

		if (type == Node.ELEMENT_NODE) {
			m_out.write("</");
			m_out.write(node.getNodeName());
			m_out.write('>');
		}

		m_out.flush();
	} // print(Node)

	/**
	 * Returns a sorted list of attributes. * @param attrs NamedNodeMap
	 * 
	 * @return Attr[]
	 */
	protected Attr[] sortAttributes(NamedNodeMap attrs) {
		int len = (attrs != null) ? attrs.getLength() : 0;
		Attr array[] = new Attr[len];

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
	} // sortAttributes(NamedNodeMap):Attr[]

	//
	// Main
	//
	/**
	 * Main program entry point. * @param s String
	 * 
	 * @return String
	 */
	/*
	 * public static void main(String argv[]) {
	 * 
	 * argopt.setUsage( new String[] { "usage: java dom.DOMWriter (options) uri
	 * ...","", "options:", " -n | -N Turn on/off namespace [default=on]", " -v
	 * | -V Turn on/off validation [default=on]", " -s | -S Turn on/off Schema
	 * support [default=on]", " -d | -D Turn on/off deferred DOM [default=on]
	 * ", " -c Canonical XML output.", " -h This help screen.", " -e Output Java
	 * Encoding.", " Default encoding: UTF-8"} );
	 * 
	 * // is there anything to do? if ( argv.length == 0 ) {
	 * argopt.printUsage(); System.exit(1); } // vars String parserName =
	 * DEFAULT_PARSER_NAME; boolean canonical = false; String encoding = "UTF8";
	 * // default encoding
	 * 
	 * argopt.parseArgumentTokens(argv, new char[] { 'p', 'e'} );
	 * 
	 * int c; String arg = null; while ( ( arg = argopt.getlistFiles() ) != null
	 * ) {
	 * 
	 * outer: while ( (c = argopt.getArguments()) != -1 ){ switch (c) { case
	 * 'c': canonical = true; break; case 'e': encoding =
	 * argopt.getStringParameter(); if ( encoding != null &&
	 * isValidJavaEncoding( encoding ) ) setWriterEncoding( encoding ); else {
	 * printValidJavaEncoding(); System.exit( 1 ); } break; case 'v':
	 * setValidation = true; break; case 'V': setValidation = false; break; case
	 * 'N': setNameSpaces = false; break; case 'n': setNameSpaces = true; break;
	 * case 'p': parserName = argopt.getStringParameter(); break; case 'd':
	 * setDeferredDOM = true; break; case 'D': setDeferredDOM = false; break;
	 * case 's': setSchemaSupport = true; break; case 'S': setSchemaSupport =
	 * false; break; case '?': case 'h': case '-': argopt.printUsage();
	 * System.exit(1); break; case -1: break outer; default: break; } }
	 * 
	 * 
	 * print(parserName, arg, canonical ); } } // main(String[])
	 */

	/** Normalizes the given string. */
	protected String normalize(String s) {
		StringBuffer str = new StringBuffer();
		int len = (s != null) ? s.length() : 0;

		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);

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
				if (m_canonical) {
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
	/*
	 * private static void printValidJavaEncoding() { System.err.println(
	 * "    ENCODINGS:"); System.err.print("   ");
	 * 
	 * for (int i = 0; i < MIME2JAVA_ENCODINGS.length; i++) {
	 * System.err.print(MIME2JAVA_ENCODINGS[i] + " ");
	 * 
	 * if ((i % 7) == 0) { System.err.println(); System.err.print("   "); } } }
	 * // printJavaEncoding()
	 */
}
