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
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

/**
 * 
 * @version $Id: ExampleFileFilter.java,v 1.1 2001/10/18 01:32:16 simon Exp $
 * @author Simon Allen
 */
public class ExampleFileFilter extends FileFilter {

	private Hashtable<String, ExampleFileFilter> filters = null;

	private String description = null;

	private String fullDescription = null;

	private boolean useExtensionsInDescription = true;

	/**
	 * Creates a file filter. If no filters are added, then all files are
	 * accepted.
	 * 
	
	 * @see #addExtension */
	public ExampleFileFilter() {
		this((String) null, (String) null);
	}

	/**
	 * Creates a file filter that accepts files with the given extension.
	 * Example: new ExampleFileFilter("jpg");
	 * 
	
	 * @param extension String
	 * @see #addExtension */
	public ExampleFileFilter(String extension) {
		this(extension, null);
	}

	/**
	 * Creates a file filter that accepts the given file type. Example: new
	 * ExampleFileFilter("jpg", "JPEG Image Images");
	 * 
	 * Note that the "." before the extension is not needed. If provided, it
	 * will be ignored.
	 * 
	
	 * @param extension String
	 * @param description String
	 * @see #addExtension */
	public ExampleFileFilter(String extension, String description) {
		this(new String[] { extension }, description);
	}

	/**
	 * Creates a file filter from the given string array. Example: new
	 * ExampleFileFilter(String {"gif", "jpg"});
	 * 
	 * Note that the "." before the extension is not needed adn will be ignored.
	 * 
	
	 * @param filters String[]
	 * @see #addExtension */
	public ExampleFileFilter(String[] filters) {
		this(filters, null);
	}

	/**
	 * Creates a file filter from the given string array and description.
	 * Example: new ExampleFileFilter(String {"gif", "jpg"}, "Gif and JPG
	 * Images");
	 * 
	 * Note that the "." before the extension is not needed and will be ignored.
	 * 
	
	 * @param filters String[]
	 * @param description String
	 * @see #addExtension */
	public ExampleFileFilter(String[] filters, String description) {
		this.filters = new Hashtable<String, ExampleFileFilter>(filters.length);

		for (String filter : filters) {
			// add filters one by one
			addExtension(filter);
		}

		setDescription(description);
	}

	/**
	 * Return true if this file should be shown in the directory pane, false if
	 * it shouldn't.
	 * 
	 * Files that begin with "." are ignored.
	 * 
	
	
	 * @param f File
	 * @return boolean
	 * @see #getExtension * @see FileFilter#accepts */
	public boolean accept(File f) {
		if (f != null) {
			if (f.isDirectory()) {
				return true;
			}

			String extension = getExtension(f);

			if ((extension != null) && (filters.get(getExtension(f)) != null)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return the extension portion of the file's name .
	 * 
	
	
	 * @param f File
	 * @return String
	 * @see #getExtension * @see FileFilter#accept */
	public String getExtension(File f) {
		if (f != null) {
			String filename = f.getName();
			int i = filename.lastIndexOf('.');

			if ((i > 0) && (i < (filename.length() - 1))) {
				return filename.substring(i + 1).toLowerCase();
			}
		}

		return null;
	}

	/**
	 * Adds a filetype "dot" extension to filter against.
	 * 
	 * For example: the following code will create a filter that filters out all
	 * files except those that end in ".jpg" and ".tif":
	 * 
	 * ExampleFileFilter filter = new ExampleFileFilter();
	 * filter.addExtension("jpg"); filter.addExtension("tif");
	 * 
	 * Note that the before the extension is not needed and will be ignored.
	 * @param extension String
	 */
	public void addExtension(String extension) {
		if (filters == null) {
			filters = new Hashtable<String, ExampleFileFilter>(5);
		}

		if (extension != null) {
			filters.put(extension.toLowerCase(), this);
		}

		fullDescription = null;
	}

	/**
	 * Returns the human readable description of this filter. For example: "JPEG
	 * and GIF Image Files (*.jpg, *.gif)"
	 * 
	
	
	
	
	 * @return String
	 * @see setDescription * @see setExtensionListInDescription * @see isExtensionListInDescription * @see FileFilter#getDescription */
	public String getDescription() {
		if (fullDescription == null) {
			if ((description == null) || isExtensionListInDescription()) {
				if (description != null) {
					fullDescription = description;
				}

				fullDescription += " (";

				// build the description from the extension list
				Enumeration<String> extensions = filters.keys();

				if (extensions != null) {
					fullDescription += "." + extensions.nextElement();

					while (extensions.hasMoreElements()) {
						fullDescription += ", " + extensions.nextElement();
					}
				}

				fullDescription += ")";
			} else {
				fullDescription = description;
			}
		}

		return fullDescription;
	}

	/**
	 * Sets the human readable description of this filter. For example:
	 * filter.setDescription("Gif and JPG Images");
	 * 
	
	
	
	 * @param description String
	 * @see setDescription * @see setExtensionListInDescription * @see isExtensionListInDescription */
	public void setDescription(String description) {
		this.description = description;
		fullDescription = null;
	}

	/**
	 * Determines whether the extension list (.jpg, .gif, etc) should show up in
	 * the human readable description.
	 * 
	 * Only relevent if a description was provided in the constructor or using
	 * setDescription();
	 * 
	
	
	
	 * @param b boolean
	 * @see getDescription * @see setDescription * @see isExtensionListInDescription */
	public void setExtensionListInDescription(boolean b) {
		useExtensionsInDescription = b;
		fullDescription = null;
	}

	/**
	 * Returns whether the extension list (.jpg, .gif, etc) should show up in
	 * the human readable description.
	 * 
	 * Only relevent if a description was provided in the constructor or using
	 * setDescription();
	 * 
	
	
	
	 * @return boolean
	 * @see getDescription * @see setDescription * @see setExtensionListInDescription */
	public boolean isExtensionListInDescription() {
		return useExtensionsInDescription;
	}
}
