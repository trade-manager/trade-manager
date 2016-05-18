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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Property utilities library. Contains methods that allow to read, write, check
 * existence, merge and extract subsets of properties.
 * 
 * @author : Simon Allen
 */
public class CollectionUtilities {
	private CollectionUtilities() {
	}

	/**
	 * Method read.
	 * 
	 * @param filepath
	 *            String
	 * @return Properties
	 * @throws FileNotFoundException
	 */
	public static Properties read(String filepath) throws FileNotFoundException {
		Properties rval;
		BufferedInputStream bis = null;

		try {
			File file = new File(filepath);

			bis = new BufferedInputStream(new FileInputStream(file));
			rval = new Properties();

			rval.load(bis);

			return rval;
		} catch (IOException e) {
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
				}

				bis = null;
			}
		}

		// try relative to java.home
		try {
			File file = new File(System.getProperty("java.home") + File.separator + filepath);

			bis = new BufferedInputStream(new FileInputStream(file));
			rval = new Properties();

			rval.load(bis);

			return rval;
		} catch (IOException e) {
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
				}

				bis = null;
			}
		}

		throw new FileNotFoundException("Property file " + filepath + " not found");
	}

	/**
	 * Method write.
	 * 
	 * @param filepath
	 *            String
	 * @param theProperties
	 *            Properties
	 * @throws IOException
	 */
	public static void write(String filepath, Properties theProperties) throws IOException { // put
																								// filepath
																								// as
																								// a
																								// comment
		write(filepath, theProperties, filepath);
	}

	/**
	 * Method write.
	 * 
	 * @param filepath
	 *            String
	 * @param theProperties
	 *            Properties
	 * @param propComments
	 *            String
	 * @throws IOException
	 */
	public static void write(String filepath, Properties theProperties, String propComments) throws IOException {
		BufferedOutputStream bos = null;

		try {
			File file = new File(filepath);

			bos = new BufferedOutputStream(new FileOutputStream(file));

			theProperties.store(bos, propComments);
		} finally {
			if (bos != null) {
				bos.close();

				bos = null;
			}
		}
	}

	/**
	 * Method checkProperties.
	 * 
	 * @param myKeys
	 *            String[]
	 * @param p
	 *            Properties
	 * @throws MissingPropertiesException
	 */
	public static void checkProperties(String[] myKeys, Properties p) throws MissingPropertiesException {
		if ((myKeys == null) || (p == null)) {
			return;
		}

		MissingPropertiesException mpe = null;

		for (int ii = 0; ii < myKeys.length; ii++) {
			if (!p.containsKey(myKeys[ii])) {
				if (mpe == null) {
					mpe = new MissingPropertiesException();
				}

				mpe.addProperty(myKeys[ii]);
			}
		}

		if (mpe != null) {
			throw mpe;
		}

		return;
	}

	/**
	 * Return a Dictionary object which is a subset of the given Dictionary,
	 * where the tags all <b>begin</b> with the given tag.
	 * 
	 * Hastables and Properties can be used as they are Dictionaries.
	 * 
	 * @param superset
	 *            .
	 * 
	 * 
	 * @param tag
	 *            String
	 * @param result
	 *            Dictionary<String,Object>
	 */
	public static void getSubset(Dictionary<String, Object> superset, String tag, Dictionary<String, Object> result) {
		if ((result == null) || (tag == null) || (superset == null)) {
			throw new IllegalArgumentException(
					"Invalid arguments specified : superset = " + superset + " tag = " + tag + " result = " + result);
		}

		String key;
		Enumeration<String> enumKey = superset.keys();

		while (enumKey.hasMoreElements()) {
			key = enumKey.nextElement();

			if (key.startsWith(tag)) {
				result.put(key, superset.get(key));
			}
		}
	}

	/**
	 * Combine two properties lists. All properties from source are copied to
	 * destination. <i>destination</i> is the result.
	 * 
	 * Properties that exist in both source and destination will be overwritten
	 * with values from source.
	 * 
	 * Hastables and Properties can be used as they are Dictionaries.
	 * 
	 * 
	 * @param source
	 *            Dictionary<Object,Object>
	 * @param destination
	 *            Dictionary<Object,Object>
	 */
	public static void copyOverwrite(Dictionary<Object, Object> source, Dictionary<Object, Object> destination) {
		if ((destination == null) || (source == null)) {
			throw new IllegalArgumentException(
					"Invalid arguments specified : source = " + source + " destination = " + destination);
		}

		Object key;
		Enumeration<Object> enumKey = source.keys();

		while (enumKey.hasMoreElements()) {
			key = enumKey.nextElement();

			destination.put(key, source.get(key));
		}
	}

	/**
	 * Returns a semicolumn separated list of keys and values in the dictionary.
	 * 
	 * Here is an example of returned String "key1 = value1; key2 = value2;"
	 * 
	 * 
	 * 
	 * @param dict
	 *            Dictionary<Object,Object>
	 * @return : String.
	 */
	public static String dictionaryToString(Dictionary<Object, Object> dict) {
		Enumeration<Object> keys = dict.keys();
		Object key, value;
		StringBuffer result = new StringBuffer();

		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			value = dict.get(key);

			result.append(key.toString());
			result.append(" = ");
			result.append(value.toString());
			result.append("; ");
		}

		return result.toString();
	}

	/**
	 * Method main.
	 * 
	 * @param args
	 *            String[]
	 */
	public static void main(String[] args) {
		try {
			Properties p = CollectionUtilities.read(args[0]);

			System.out.println(p);
		} catch (Exception ex) {
		}
	}

	/**
	 * Method n2sort.
	 * 
	 * @param index
	 *            String[]
	 * @param asc
	 *            boolean
	 */
	public static void n2sort(String[] index, boolean asc) {
		for (int i = 0; i < index.length; i++) {
			for (int j = i + 1; j < index.length; j++) {
				if (compare(index[i], index[j], asc) == -1) {
					swap(i, j, index);
				}
			}
		}
	}

	/**
	 * Method swap.
	 * 
	 * @param i
	 *            int
	 * @param j
	 *            int
	 * @param index
	 *            String[]
	 */
	private static void swap(int i, int j, String[] index) {
		String tmp = index[i];

		index[i] = index[j];
		index[j] = tmp;
	}

	/**
	 * Method compare.
	 * 
	 * @param row1
	 *            String
	 * @param row2
	 *            String
	 * @param asc
	 *            boolean
	 * @return int
	 */
	private static int compare(String row1, String row2, boolean asc) {
		int result = row1.compareTo(row2);
		int returnVal = 0;

		if (result < 0) {
			returnVal = -1;
		} else if (result != 0) // result > 0
		{
			returnVal = 1;
		} else {
			returnVal = 0;
		}

		return asc ? -returnVal : returnVal;
	}
}
