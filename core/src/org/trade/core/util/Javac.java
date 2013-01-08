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

package org.trade.core.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.sun.tools.javac.Main;

/**
 */
public final class Javac {

	private String classpath;
	private String outputdir;
	private String sourcepath;
	private String bootclasspath;
	private String extdirs;
	private String encoding;
	private String target;

	/**
	 * Constructor for Javac.
	 * @param classpath String
	 * @param outputdir String
	 */
	public Javac(String classpath, String outputdir) {
		this.classpath = classpath;
		this.outputdir = outputdir;
	}

	/**
	 * Compile the given source files.
	 * 
	 * @param srcFiles
	
	 * @return null if success; or compilation errors */
	public String compile(String srcFiles[]) {
		StringWriter err = new StringWriter();
		PrintWriter errPrinter = new PrintWriter(err);

		String args[] = buildJavacArgs(srcFiles);
		int resultCode = Main.compile(args, errPrinter);
		errPrinter.close();
		return (resultCode == 0) ? null : err.toString();
	}

	/**
	 * Method compile.
	 * @param srcFiles File[]
	 * @return String
	 */
	public String compile(File srcFiles[]) {
		String paths[] = new String[srcFiles.length];
		for (int i = 0; i < paths.length; i++) {
			paths[i] = srcFiles[i].getAbsolutePath();
		}
		return compile(paths);
	}

	/**
	 * Method buildJavacArgs.
	 * @param srcFiles String[]
	 * @return String[]
	 */
	private String[] buildJavacArgs(String srcFiles[]) {
		List<String> args = new ArrayList<String>();

		if (classpath != null) {
			args.add("-classpath");
			args.add(classpath);
		}
		if (outputdir != null) {
			args.add("-d");
			args.add(outputdir);
		}
		if (sourcepath != null) {
			args.add("-sourcepath");
			args.add(sourcepath);
		}
		if (bootclasspath != null) {
			args.add("-bootclasspath");
			args.add(bootclasspath);
		}
		if (extdirs != null) {
			args.add("-extdirs");
			args.add(extdirs);
		}
		if (encoding != null) {
			args.add("-encoding");
			args.add(encoding);
		}
		if (target != null) {
			args.add("-target");
			args.add(target);
		}

		for (int i = 0; i < srcFiles.length; i++) {
			args.add(srcFiles[i]);
		}

		return args.toArray(new String[args.size()]);
	}

	/**
	 * Method getBootclasspath.
	 * @return String
	 */
	public String getBootclasspath() {
		return bootclasspath;
	}

	/**
	 * Method setBootclasspath.
	 * @param bootclasspath String
	 */
	public void setBootclasspath(String bootclasspath) {
		this.bootclasspath = bootclasspath;
	}

	/**
	 * Method getClasspath.
	 * @return String
	 */
	public String getClasspath() {
		return classpath;
	}

	/**
	 * Method setClasspath.
	 * @param classpath String
	 */
	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	/**
	 * Method getEncoding.
	 * @return String
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Method setEncoding.
	 * @param encoding String
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Method getExtdirs.
	 * @return String
	 */
	public String getExtdirs() {
		return extdirs;
	}

	/**
	 * Method setExtdirs.
	 * @param extdirs String
	 */
	public void setExtdirs(String extdirs) {
		this.extdirs = extdirs;
	}

	/**
	 * Method getOutputdir.
	 * @return String
	 */
	public String getOutputdir() {
		return outputdir;
	}

	/**
	 * Method setOutputdir.
	 * @param outputdir String
	 */
	public void setOutputdir(String outputdir) {
		this.outputdir = outputdir;
	}

	/**
	 * Method getSourcepath.
	 * @return String
	 */
	public String getSourcepath() {
		return sourcepath;
	}

	/**
	 * Method setSourcepath.
	 * @param sourcepath String
	 */
	public void setSourcepath(String sourcepath) {
		this.sourcepath = sourcepath;
	}

	/**
	 * Method getTarget.
	 * @return String
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Method setTarget.
	 * @param target String
	 */
	public void setTarget(String target) {
		this.target = target;
	}

}
