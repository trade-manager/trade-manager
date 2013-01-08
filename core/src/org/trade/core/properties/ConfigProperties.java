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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Represents the applications configuration. This class is intended to be a bit
 * of a hack. I.e it wraps the apps property file, and provides a place to map
 * the keys in the file to constants.
 * 
 * @version $Id: ConfigProperties.java,v 1.6 2001/12/11 22:28:15 simon Exp $
 * @author Simon Allen
 */
public class ConfigProperties {
	private final static Logger _log = LoggerFactory
			.getLogger(ConfigProperties.class);

	private final static String ENVIRONMENT_VARIABLE_CONTAINING_INSTALLATION_HOME = "trade.install.dir";

	private final static String ENVIRONMENT_VARIABLE_PROPERTY_FILE = "config.properties";

	public final static String MANDATORY_PROPERTY = "mandatory_property";

	//
	// Private Attributes
	//
	private Properties m_properties = null;

	private static ConfigProperties m_theConfig = new ConfigProperties();

	/**
	 * Returns a string for a key.
	 * @param key String
	 * @return String
	 * @throws IOException
	 */
	public static String getPropAsString(String key) throws IOException {
		String strRet = null;

		strRet = m_theConfig.retrieveProperty(key);

		return strRet;
	}

	/**
	 * Method getDeploymentPropertyFileName.
	 * @return String
	 */
	public static String getDeploymentPropertyFileName() {
		String filename = null;

		try {
			File file = new File(
					System.getProperty(ENVIRONMENT_VARIABLE_CONTAINING_INSTALLATION_HOME),
					ENVIRONMENT_VARIABLE_PROPERTY_FILE);
			filename = file.toString();
		} catch (Throwable e) {
			// do nothing as we are an applet !!!

		}
		return filename;
	}

	/**
	 * Method getDeploymentProperties.
	 * @param context Object
	 * @param fileName String
	 * @return Properties
	 * @throws IOException
	 */
	public static Properties getDeploymentProperties(Object context,
			String fileName) throws IOException {
		Properties deploymentProperties = null;

		deploymentProperties = m_theConfig.getProperties(context, fileName);

		return deploymentProperties;
	}

	/**
	 * Method loadDeploymentProperties.
	 * @param context Object
	 * @param fileName String
	 * @throws IOException
	 */
	public static void loadDeploymentProperties(Object context, String fileName)
			throws IOException {
		m_theConfig.getProperties(context, fileName);
	}

	/**
	 * Method getSystemPropertyFileName.
	 * @return String
	 */
	public static String getSystemPropertyFileName() {
		return ENVIRONMENT_VARIABLE_PROPERTY_FILE;
	}

	/**
	 * Returns an int for a key. If the number is malformed, then it return 0.
	 * @param key String
	 * @return int
	 * @throws IOException
	 */
	public static int getPropAsInt(String key) throws IOException {
		int ret = 0;

		ret = Integer.parseInt(m_theConfig.retrieveProperty(key));

		return ret;
	}

	/**
	 * Returns an boolean for a key. If the number is malformed, then it return
	 * false.
	 * @param key String
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean getPropAsBoolean(String key) throws IOException {
		return Boolean.valueOf(m_theConfig.retrieveProperty(key));
	}

	/**
	 * Method getPropAsEnumeration.
	 * @param keyRoot String
	 * @return Enumeration<String>
	 * @throws IOException
	 */
	public static Enumeration<String> getPropAsEnumeration(String keyRoot)
			throws IOException {
		Vector<String> resVec = null;
		int iNumEntries = getPropAsInt(keyRoot + "_NumOfItems");
		StringBuffer key = new StringBuffer(keyRoot);
		int keyLen = keyRoot.length();

		resVec = new Vector<String>(iNumEntries);

		for (int iCount = 1; iCount < (iNumEntries + 1); iCount++) {
			String val = getPropAsString(key.append("_" + iCount).toString());

			key.setLength(keyLen); // reset key

			if (null != val) {
				resVec.addElement(val);
			}
		}

		return resVec.elements();
	}

	/**
	 * Method getPropertiesAsArrayOfProperties.
	 * @param keyRoot String
	 * @param keyNames Dictionary<?,?>
	 * @return Properties[]
	 * @throws IOException
	 */
	public static Properties[] getPropertiesAsArrayOfProperties(String keyRoot,
			Dictionary<?, ?> keyNames) throws IOException {
		int iNumItems = getPropAsInt(keyRoot + "_NumOfItems");
		Properties[] propArray = new Properties[iNumItems];

		for (int iCount = 1; iCount <= iNumItems; iCount++) {
			propArray[iCount - 1] = getSetOfProperties(keyRoot + "_" + iCount,
					keyNames);
		}

		return propArray;
	}

	// read configuration properties
	/**
	 * Method getProperties.
	 * @param context Object
	 * @param fileName String
	 * @return Properties
	 * @throws IOException
	 */
	private Properties getProperties(Object context, String fileName)
			throws IOException {
		Properties systemProperties = new Properties();

		loadPropertiesAsResource(m_theConfig, getSystemPropertyFileName(),
				systemProperties);

		loadPropertiesAsResource(context, fileName, systemProperties);

		Properties deploymentProperties = new Properties(systemProperties);

		loadPropertiesAsFile(getDeploymentPropertyFileName(),
				deploymentProperties);

		m_properties = deploymentProperties;

		return deploymentProperties;
	}

	/**
	 * Method getSetOfProperties.
	 * @param keyRoot String
	 * @param keyNames Dictionary<?,?>
	 * @return Properties
	 * @throws IOException
	 */
	private static Properties getSetOfProperties(String keyRoot,
			Dictionary<?, ?> keyNames) throws IOException {
		Enumeration<?> enumKey = keyNames.keys();
		Properties result = new Properties();

		while (enumKey.hasMoreElements()) {
			String key;
			String value;

			key = (String) enumKey.nextElement();

			boolean mandatory = MANDATORY_PROPERTY.equals(keyNames.get(key));

			if (mandatory) {
				value = getPropAsString(keyRoot + "_" + key);
			} else {
				try {
					value = getPropAsString(keyRoot + "_" + key);
				} catch (Exception e) {
					value = null;
				}
			}

			if (value != null) {
				result.put(key, value);
			}
		}

		return (result);
	}

	/**
	 * Method getCommaSeparatedStrings.
	 * @param key String
	 * @return Enumeration<?>
	 * @throws IOException
	 */
	public static Enumeration<?> getCommaSeparatedStrings(String key)
			throws IOException {
		String list = getPropAsString(key);
		StringTokenizer t = new StringTokenizer(list, ",");

		return t;
	}

	// read configuration properties
	/**
	 * Method retrieveProperty.
	 * @param key String
	 * @return String
	 * @throws IOException
	 */
	private String retrieveProperty(String key) throws IOException {
		String ret = null;

		if (null == m_properties) {
			Properties systemProperties = new Properties();

			loadPropertiesAsResource(m_theConfig, getSystemPropertyFileName(),
					systemProperties);

			Properties deploymentProperties = new Properties(systemProperties);

			loadPropertiesAsFile(getDeploymentPropertyFileName(),
					deploymentProperties);

			m_properties = deploymentProperties;
		}

		ret = m_properties.getProperty(key);

		if (null == ret) {
			throw new PropertyNotFoundException("The property \"" + key
					+ "\" was not found in the property file \""
					+ getDeploymentPropertyFileName() + "\" or \""
					+ getSystemPropertyFileName() + "\".  Check the file.");
		}

		return ret;
	}

	/**
	 * Method getPropertyAfterEnvSubstitution.
	 * @param key String
	 * @return String
	 * @throws IOException
	 */
	public static String getPropertyAfterEnvSubstitution(String key)
			throws IOException {
		String strRet = null;

		strRet = m_theConfig.retrieveProperty(key);

		// put env variables in the dictionary
		Dictionary<?, ?> toSubstitute = System.getProperties();
		TemplateParser tp = new TemplateParser(strRet, toSubstitute);
		String parsedVal = tp.parseTemplate();

		return parsedVal;
	}

	/**
	 * Method readFileAsString.
	 * @param filePath String
	 * @param loader ClassLoader
	 * @return String
	 * @throws java.io.IOException
	 */
	public static String readFileAsString(String filePath, ClassLoader loader)
			throws java.io.IOException {

		StringBuffer fileData = new StringBuffer(1000);
		InputStream inputStream = loader.getResourceAsStream(filePath);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

		BufferedReader reader = new BufferedReader(inputStreamReader);
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	/**
	 * Method loadPropertiesAsResource.
	 * @param context Object
	 * @param filename String
	 * @param properties Properties
	 * @throws IOException
	 */
	private void loadPropertiesAsResource(Object context, String filename,
			Properties properties) throws IOException {
		InputStream unbuffered;

		if (null == filename) {
			throw new PropertyFileNotFoundException(
					"No property file name found"
							+ " please check your command line parameters e.g. "
							+ "-Dconfig.properties=/filename.properties ");
		} else {
			unbuffered = context.getClass().getResourceAsStream(filename);
		}

		if (unbuffered == null) {
			throw new PropertyFileNotFoundException("Check "
					+ "to see if the property file \"" + filename
					+ "\" is installed and available in the class path.");
		} else {
			InputStream in = new BufferedInputStream(unbuffered);

			properties.load(in);
			in.close();
			unbuffered.close();
		}
	}

	/**
	 * Method loadPropertiesAsFile.
	 * @param filename String
	 * @param properties Properties
	 * @throws IOException
	 */
	private void loadPropertiesAsFile(String filename, Properties properties)
			throws IOException {
		if (null != filename) {
			File propertyFile = new File(filename);
			String propertyFilePath = propertyFile.getAbsolutePath();

			if (propertyFile.exists()) {
				FileInputStream is = new FileInputStream(propertyFile);

				properties.load(is);
				is.close();
			} else {
				_log.info("The property file " + propertyFilePath
						+ " does not exist -- using defaults");
			}
		} else {
			_log.info("The property file does not exist -- using defaults");
		}
	}

	/**
	 * Method reNumberDecodesInPropertiesFile.
	 * @param propertyFileLocation String
	 */
	public static void reNumberDecodesInPropertiesFile(
			String propertyFileLocation) {

		FileInputStream fileInputStream = null;
		Scanner scanString = null;
		try {
			/*
			 * Location of the properties file. Copy the source one to this Dir.
			 */
			File file = new File(propertyFileLocation);
			/*
			 * The name of the Decodes to be renumbered. Copy the output BELOW into
			 * the properties file and remember to set the _NumOfItems to the
			 * last value.
			 */
			final String codeName = "CODE_DECODE";

			/*
			 * lookupServiceProvideName current either PropertyFile or DBTable
			 */

			final String lookupServiceProvideName = "PropertyFile";

			fileInputStream = new FileInputStream(file.getAbsoluteFile());
			scanString = new Scanner(fileInputStream);
			Pattern pattern = Pattern.compile("_\\d*=");
			scanString.useDelimiter(pattern);
			int count = 0;
			String newText = new String();

			String token = null;
			String delimiter = null;
			String oldDelimiter = null;
			while (scanString.hasNext()) {

				token = scanString.next();
				delimiter = scanString.findInLine(pattern);
				if (null != token && token.contains(codeName)) {

					if (null != delimiter) {
						 _log.info("token: " + token);
						 _log.info("delimiter: " + delimiter);
						if (null != token
								&& !token.endsWith(lookupServiceProvideName)) {
							if (!delimiter.equals(oldDelimiter)) {
								count++;
							}
							newText = newText + token + "_" + count + "=";
						} else {
							newText = newText + token + delimiter;
						}
						oldDelimiter = delimiter;
					}
				}
			}
			newText = newText + token;
			_log.error("newText: " + newText);
		} catch (Exception ex) {
			_log.error("Error paring file: " + ex.getMessage(), ex);
		} finally {

			try {
				if (null != scanString)
					scanString.close();
				if (null != fileInputStream)
					fileInputStream.close();
			} catch (IOException e) {
				_log.error("Error closing input stream: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Method main.
	 * @param args String[]
	 */
	public static void main(String[] args) {
		String propertyFileLocation = "C:\\Temp\\trade.properties";
		ConfigProperties.reNumberDecodesInPropertiesFile(propertyFileLocation);
	}
}
