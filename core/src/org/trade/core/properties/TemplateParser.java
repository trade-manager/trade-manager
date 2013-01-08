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
package org.trade.core.properties;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * This class parses a template substituting keys inclosed in "#(" and ")#".
 * Characters '(', '#', and ')' are used as a default mark up, and can be chaged
 * with setBrackets() and setDelimiter() methods.
 * 
 * E.g. template (String) : <font face="arial" size="2"
 * color="#(application_form_errors_color)#"> tags (Hashtable) : {
 * "application_form_errors_color", "#0fffff" } Output: <font face="arial"
 * size="2" color="#0fffff">
 * 
 * @author Simon Allen
 * @version $Id: TemplateParser.java,v 1.1 2001/09/21 20:45:26 clay Exp $
 */
public class TemplateParser {
	private char m_leftBracket = '(';

	private char m_rightBracket = ')';

	private char m_delimiter = '#';

	private boolean m_insertMissingTags = false;

	private String BEGIN_FOR_EACH = "BEGIN_FOR_EACH";

	private String END_FOR_EACH = "END_FOR_EACH";

	private String m_template = "";

	private Dictionary<?, ?> m_tags;

	private int m_lastParsedCharPosition = 0;

	private int m_lastCharPosition = 0;

	private int m_iterationNumber = -1;

	private StringBuffer m_errorMessages = new StringBuffer();

	private Vector<String> m_missingKeys = new Vector<String>();

	/**
	 * 
	 * @param template
	 *            - the template.
	 * @param tags
	 *            - the tags and values.
	
	 * @throws <code>InvalidParameterException</code> when tag in the template
	 *         is not found in the tags Hashtable. */
	public TemplateParser(String template, Dictionary<?, ?> tags) {
		if ((template == null) || (tags == null)) {
			throw new IllegalArgumentException(
					"Null argument passed to TemplateParser() constructor. template="
							+ template + ", tags=" + tags);
		}

		m_template = template;
		m_tags = tags;
		m_lastCharPosition = m_template.length() - 1;
	}

	/**
	 * If 'insertUnmatchedKeys' option is true parseTemplate method will insert,
	 * #(key_name)# strings found in the template, into output if value for the
	 * key_name was not specified.
	 * 
	 * Otherwise #(key_name)# pattern is ignored and nothing will appear in it's
	 * place in the output.
	 * 
	
	 * @param option boolean
	 */
	public void setInsertMissingTags(boolean option) {
		m_insertMissingTags = option;
	}

	/**
	 * Set characters used as brackets enclosing the key. Default values are '('
	 * and ')'.
	 * 
	 * E.g. #(key_name)#.
	 * 
	
	
	 * @param left char
	 * @param right char
	 */
	public void setBrackets(char left, char right) {
		m_leftBracket = left;
		m_rightBracket = right;
	}

	/**
	 * Set character used as a delimiter enclosing the key. Default value is
	 * '#'.
	 * 
	 * E.g. #(key_name)#.
	 * 
	
	 * @param delimiter char
	 */
	public void setDelimiter(char delimiter) {
		m_delimiter = delimiter;
	}

	/**
	 * This method parses the template substituting keys with values supplied in
	 * tags Dictionary.
	 * @return String
	 */
	public String parseTemplate() {
		StringBuffer parsedTemplate = new StringBuffer();

		m_errorMessages.setLength(0);
		m_missingKeys.removeAllElements();

		while (true) {
			NextToken result = getNextToken();

			if (!result.finishedParsing()) {
				if (result.foundToken()) {
					parsedTemplate.append(m_template.substring(
							result.getLastParsedPosition(),
							result.getPositionBeforeKey() + 1));

					if (result.getMissingTag() == null) {
						if (!result.getKey().endsWith("[]")) {
							parsedTemplate.append(result.getValue());
						} else {
							try {
								Object[] array = result.getArrayOfValues();

								if ((m_iterationNumber >= 0)
										&& (m_iterationNumber < array.length)) {
									parsedTemplate
											.append(array[m_iterationNumber]
													.toString());
								}
							} catch (Exception e) {
								m_missingKeys.addElement(result.getKey());
								addErrorMessage("Key "
										+ result.getKey()
										+ " not found in the parameters. Iteration # "
										+ m_iterationNumber);
							}
						}
					} else {
						if (BEGIN_FOR_EACH.equals(result.getKey())) {
							String parsedSubtemplate = processForEachSubtemplate(result);

							if (parsedSubtemplate == null) {
								m_missingKeys.addElement(END_FOR_EACH);
								addErrorMessage("Tag " + END_FOR_EACH
										+ " not found in the template.");
							} else {
								parsedTemplate.append(parsedSubtemplate);
							}
						} else {
							if (m_insertMissingTags) {
								parsedTemplate.append(result.getMissingTag());
							}

							m_missingKeys.addElement(result.getKey());
							addErrorMessage("Key " + result.getKey()
									+ " not found in parameters.");
						}
					}
				} else
				// result.getFoundToken() == false
				{
					// This will happen when we find a pair of # characters
					// which don't enclose a key #(key)#
					parsedTemplate.append(m_template.substring(
							result.getLastParsedPosition(),
							result.getParsedPosition()));
				}
			} else
			// If finished parsing append the rest of the template
			{
				parsedTemplate.append(m_template.substring(
						result.getLastParsedPosition(),
						result.getParsedPosition()));

				break;
			}
		}

		return parsedTemplate.toString();
	}

	/**
	 * This method parses the template substituting keys with values supplied in
	 * tags Dictionary.
	 * @return Properties
	 */
	public Properties findTemplateTags() {
		Properties tags = new Properties();

		m_errorMessages.setLength(0);
		m_missingKeys.removeAllElements();

		while (true) {
			NextToken result = getNextToken();

			if (!result.finishedParsing()) {
				if (result.foundToken()
						&& !BEGIN_FOR_EACH.equals(result.getKey())
						&& !END_FOR_EACH.equals(result.getKey())) {
					tags.put(result.getKey(), result.getKey());
				}
			} else {
				break;
			}
		}

		return (tags);
	}

	/**
	 * This method parses the template substituting keys with values supplied in
	 * tags Hashtable.
	 * 
	
	
	
	 * @return NextToken
	 * @throws <code>InvalidParameterException</code> when tag in the template
	 *         is not found in the tags Hashtable. */
	public NextToken getNextToken() {
		int delimiterPosition = -1;
		int nextDelimiterPosition = -1;
		NextToken result = new NextToken();

		delimiterPosition = m_template.indexOf(m_delimiter,
				m_lastParsedCharPosition);

		if (delimiterPosition != -1) {
			nextDelimiterPosition = m_template.indexOf(m_delimiter,
					delimiterPosition + 1);
		}

		if ((delimiterPosition == -1) || (nextDelimiterPosition == -1)) {
			result.setLastParsedPosition(m_lastParsedCharPosition);
			result.setParsedPosition(m_lastCharPosition + 1);
			result.setFoundToken(false);
			result.setFinishedParsing(true);

			return (result);
		} else {
			int leftBracketPosition = delimiterPosition + 1;
			int rightBracketPosition = nextDelimiterPosition - 1;

			if ((leftBracketPosition < rightBracketPosition)
					&& (m_template.charAt(leftBracketPosition) == m_leftBracket)
					&& (m_template.charAt(rightBracketPosition) == m_rightBracket)) {
				// We have correct syntax element : #(key)#
				if ((rightBracketPosition - leftBracketPosition) > 0) {
					String key = m_template.substring(leftBracketPosition + 1,
							rightBracketPosition);

					result.setKey(key);

					Object value = m_tags.get(key);

					if (value != null) {
						result.setValue(value);
					} // If key not found #(key)# is ignored and nothing is
						// inserted in it's place in the output
					else {
						StringBuffer missingTag = new StringBuffer();

						missingTag.append(m_delimiter);
						missingTag.append(m_leftBracket);
						missingTag.append(key);
						missingTag.append(m_rightBracket);
						missingTag.append(m_delimiter);
						result.setMissingTag(missingTag.toString());
					}
				} else {
					result.setKey("");
					result.setValue("");
				}

				result.setPositionAfterKey(nextDelimiterPosition + 1);
				result.setPositionBeforeKey(delimiterPosition - 1);
				result.setFoundToken(true);
			} else {
				result.setFoundToken(false);
			}

			result.setLastParsedPosition(m_lastParsedCharPosition);

			if (result.foundToken()) {
				m_lastParsedCharPosition = nextDelimiterPosition + 1;
			} else {
				m_lastParsedCharPosition = nextDelimiterPosition - 1;
			}

			result.setParsedPosition(m_lastParsedCharPosition);
			result.setFinishedParsing(false);
		}

		return result;
	}

	/**
	 * Method processForEachSubtemplate.
	 * @param beginToken NextToken
	 * @return String
	 */
	private String processForEachSubtemplate(NextToken beginToken) {
		NextToken endToken;
		StringBuffer result = new StringBuffer();
		int numberOfIterations = 0;

		do {
			endToken = getNextToken();

			if ((endToken != null) && (endToken.getKey() != null)
					&& endToken.getKey().endsWith("[]")) {
				try {
					Object[] array = endToken.getArrayOfValues();

					if ((array != null) && (array.length > numberOfIterations)) {
						numberOfIterations = array.length;
					}
				} catch (Exception e) // A class cast exception can happen
				// here
				{
				}
			}
		} while ((endToken != null) && !END_FOR_EACH.equals(endToken.getKey()));

		if (endToken == null) {
			return null;
		}

		String subtemplate = m_template.substring(
				beginToken.getPositionAfterKey(),
				endToken.getPositionBeforeKey() + 1);
		TemplateParser parser;
		String parsedSubtemplate;

		for (int i = 0; i < numberOfIterations; i++) {
			parser = new TemplateParser(subtemplate, m_tags);

			parser.setIterationNumber(i);

			parsedSubtemplate = parser.parseTemplate();

			if (parsedSubtemplate != null) {
				result.append(parsedSubtemplate);
			} else {
				break; // Something went wrong, it does not make sense to
				// continue.
			}
		}

		if ((numberOfIterations == 0) && m_insertMissingTags) // if insert
																// missing
		// tags is true
		// insert
		// subtemplate into
		// the output
		{
			result.append(m_delimiter);
			result.append(m_leftBracket);
			result.append(BEGIN_FOR_EACH);
			result.append(m_rightBracket);
			result.append(m_delimiter);
			result.append(subtemplate);
			result.append(m_delimiter);
			result.append(m_leftBracket);
			result.append(END_FOR_EACH);
			result.append(m_rightBracket);
			result.append(m_delimiter);
		}

		return result.toString();
	}

	/**
	 * This class holds intermediate results of parsing the template.
	 * @author Simon Allen
	 * @version $Revision: 1.0 $
	 */
	public class NextToken {
		private String m_key = null;

		private Object m_value = null;

		private Object[] m_arrayOfValues = null;

		private String m_missingTag = null;

		private boolean m_foundToken = false;

		private boolean m_finishedParsing = false;

		private int m_parsedPosition = 0;

		private int m_lastParsedPosition = 0;

		private int m_positionBeforeKey = -1;

		private int m_positionAfterKey = -1;

		/**
		 * Method setFoundToken.
		 * @param foundToken boolean
		 */
		public void setFoundToken(boolean foundToken) {
			m_foundToken = foundToken;
		}

		/**
		 * Method foundToken.
		 * @return boolean
		 */
		public boolean foundToken() {
			return (m_foundToken);
		}

		/**
		 * Method setFinishedParsing.
		 * @param finishedParsing boolean
		 */
		public void setFinishedParsing(boolean finishedParsing) {
			m_finishedParsing = finishedParsing;
		}

		/**
		 * Method finishedParsing.
		 * @return boolean
		 */
		public boolean finishedParsing() {
			return (m_finishedParsing);
		}

		/**
		 * Method getParsedPosition.
		 * @return int
		 */
		public int getParsedPosition() {
			return (m_parsedPosition);
		}

		/**
		 * Method setParsedPosition.
		 * @param position int
		 */
		public void setParsedPosition(int position) {
			m_parsedPosition = position;
		}

		/**
		 * Method getLastParsedPosition.
		 * @return int
		 */
		public int getLastParsedPosition() {
			return (m_lastParsedPosition);
		}

		/**
		 * Method setLastParsedPosition.
		 * @param position int
		 */
		public void setLastParsedPosition(int position) {
			m_lastParsedPosition = position;
		}

		/**
		 * Method getPositionBeforeKey.
		 * @return int
		 */
		public int getPositionBeforeKey() {
			return (m_positionBeforeKey);
		}

		/**
		 * Method setPositionBeforeKey.
		 * @param position int
		 */
		public void setPositionBeforeKey(int position) {
			m_positionBeforeKey = position;
		}

		/**
		 * Method getPositionAfterKey.
		 * @return int
		 */
		public int getPositionAfterKey() {
			return (m_positionAfterKey);
		}

		/**
		 * Method setPositionAfterKey.
		 * @param position int
		 */
		public void setPositionAfterKey(int position) {
			m_positionAfterKey = position;
		}

		/**
		 * Method getKey.
		 * @return String
		 */
		public String getKey() {
			return (m_key);
		}

		/**
		 * Method setKey.
		 * @param key String
		 */
		public void setKey(String key) {
			m_key = key;
		}

		/**
		 * Method getMissingTag.
		 * @return String
		 */
		public String getMissingTag() {
			return (m_missingTag);
		}

		/**
		 * Method setMissingTag.
		 * @param tag String
		 */
		public void setMissingTag(String tag) {
			m_missingTag = tag;
		}

		/**
		 * Method getValue.
		 * @return Object
		 */
		public Object getValue() {
			return (m_value);
		}

		/**
		 * Method setValue.
		 * @param value Object
		 */
		public void setValue(Object value) {
			if (value instanceof Vector) {
				Vector<?> v = (Vector<?>) value;

				m_arrayOfValues = v.toArray();
			} else if (value instanceof ArrayOfValues) {
				m_arrayOfValues = ((ArrayOfValues) value).getValues();
			} else {
				m_value = value;
			}
		}

		/**
		 * Method getArrayOfValues.
		 * @return Object[]
		 */
		public Object[] getArrayOfValues() {
			return (m_arrayOfValues);
		}
	}

	/**
	 * Method setIterationNumber.
	 * @param iterationNumber int
	 */
	private void setIterationNumber(int iterationNumber) {
		m_iterationNumber = iterationNumber;
	}

	/**
	 * Return list of error messages found while parsing last template.
	 * 
	
	 * @return String with error messages */
	public String getErrorMessages() {
		return m_errorMessages.toString();
	}

	/**
	 * Return list of error messages found while parsing last template.
	 * 
	
	 * @param message String
	 */
	private void addErrorMessage(String message) {
		if (message != null) {
			m_errorMessages.append(message);
		}

		m_errorMessages.append("\r\n");
	}

	/**
	 * This method returns an <code>Enumeration</code> of key names that were
	 * not found in the tags Hashtable and were not substituted while parsing
	 * the template during last invokation of the parseTemplate method.
	 * 
	
	 * @return Enumeration of missing parameter's names. */
	public Enumeration<String> getMissingParameters() {
		return m_missingKeys.elements();
	}
}
