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

import java.io.File;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.filechooser.FileView;

/**
 * 
 * @version $Id: ExampleFileChooser.java,v 1.1 2001/10/18 01:32:16 simon Exp $
 * @author Simon Allen
 */
public class ExampleFileChooser extends FileView {
	private Hashtable<String, Icon> icons = new Hashtable<String, Icon>(5);

	private Hashtable<File, String> fileNames = new Hashtable<File, String>(5);

	private Hashtable<File, String> fileDescriptions = new Hashtable<File, String>(5);

	private Hashtable<String, String> typeDescriptions = new Hashtable<String, String>(5);

	/**
	 * The name of the file.
	 * 
	 * 
	 * @param f
	 *            File
	 * @param fileName
	 *            String
	 * @see #getName
	 */
	public void setName(File f, String fileName) {
		fileNames.put(f, fileName);
	}

	/**
	 * The name of the file.
	 * 
	 * 
	 * 
	 * @param f
	 *            File
	 * @return String
	 * @see #setName * @see FileView#getName
	 */
	public String getName(File f) {
		return fileNames.get(f);
	}

	/**
	 * Adds a human readable description of the file.
	 * 
	 * @param f
	 *            File
	 * @param fileDescription
	 *            String
	 */
	public void putDescription(File f, String fileDescription) {
		fileDescriptions.put(f, fileDescription);
	}

	/**
	 * A human readable description of the file.
	 * 
	 * 
	 * @param f
	 *            File
	 * @return String
	 * @see FileView#getDescription
	 */
	public String getDescription(File f) {
		return fileDescriptions.get(f);
	};

	/**
	 * Adds a human readable type description for files. Based on "dot"
	 * extension strings, e.g: ".gif". Case is ignored.
	 * 
	 * @param extension
	 *            String
	 * @param typeDescription
	 *            String
	 */
	public void putTypeDescription(String extension, String typeDescription) {
		typeDescriptions.put(typeDescription, extension);
	}

	/**
	 * Adds a human readable type description for files of the type of the
	 * passed in file. Based on "dot" extension strings, e.g: ".gif". Case is
	 * ignored.
	 * 
	 * @param f
	 *            File
	 * @param typeDescription
	 *            String
	 */
	public void putTypeDescription(File f, String typeDescription) {
		putTypeDescription(getExtension(f), typeDescription);
	}

	/**
	 * A human readable description of the type of the file.
	 * 
	 * 
	 * @param f
	 *            File
	 * @return String
	 * @see FileView#getTypeDescription
	 */
	public String getTypeDescription(File f) {
		return typeDescriptions.get(getExtension(f));
	}

	/**
	 * Conveinience method that returnsa the "dot" extension for the given file.
	 * 
	 * @param f
	 *            File
	 * @return String
	 */
	public String getExtension(File f) {
		String name = f.getName();

		if (name != null) {
			int extensionIndex = name.lastIndexOf('.');

			if (extensionIndex < 0) {
				return null;
			}

			return name.substring(extensionIndex + 1).toLowerCase();
		}

		return null;
	}

	/**
	 * Adds an icon based on the file type "dot" extension string, e.g: ".gif".
	 * Case is ignored.
	 * 
	 * @param extension
	 *            String
	 * @param icon
	 *            Icon
	 */
	public void putIcon(String extension, Icon icon) {
		icons.put(extension, icon);
	}

	/**
	 * Icon that reperesents this file. Default implementation returns null. You
	 * might want to override this to return something more interesting.
	 * 
	 * 
	 * @param f
	 *            File
	 * @return Icon
	 * @see FileView#getIcon
	 */
	public Icon getIcon(File f) {
		Icon icon = null;
		String extension = getExtension(f);

		if (extension != null) {
			icon = icons.get(extension);
		}

		return icon;
	}

	/**
	 * Whether the file is hidden or not. This implementation returns true if
	 * the filename starts with a "."
	 * 
	 * 
	 * @param f
	 *            File
	 * @return Boolean
	 * @see FileView#isHidden
	 */
	public Boolean isHidden(File f) {
		String name = f.getName();

		if ((name != null) && !name.equals("") && (name.charAt(0) == '.')) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	};

	/**
	 * Whether the directory is traversable or not. Generic implementation
	 * returns true for all directories.
	 * 
	 * You might want to subtype ExampleFileView to do somethimg more
	 * interesting, such as recognize compound documents directories; in such a
	 * case you might return a special icon for the diretory that makes it look
	 * like a regular document, and return false for isTraversable to not allow
	 * users to descend into the directory.
	 * 
	 * 
	 * @param f
	 *            File
	 * @return Boolean
	 * @see FileView#isTraversable
	 */
	public Boolean isTraversable(File f) {
		if (f.isDirectory()) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	};
}
