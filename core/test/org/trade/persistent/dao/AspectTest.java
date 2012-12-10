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
package org.trade.persistent.dao;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.Aspect;
import org.trade.core.dao.AspectHome;
import org.trade.core.dao.Aspects;


/**
 * Some tests for the {@link DataUtilities} class.
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class AspectTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(AspectTest.class);

	AspectHome aspectHome = null;

	/**
	 * Method setUp.
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		aspectHome = new AspectHome();
	}

	/**
	 * Method tearDown.
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {

	}

	@Test
	public void testFindAspectByClassName() {

		// Create new instance of Strategy and set
		// values in it by reading them from form object

		try {
			String className = "org.trade.persistent.dao.Strategy";
			_log.info("Find Aspects by className: " + className);

			Aspects transientInstance = aspectHome.findByClassName(className);
			for (Aspect aspect : transientInstance.getAspect()) {
				_log.info("Aspect added Id = " + aspect.getId());
			}

		} catch (Exception ex) {
			fail("Error finding row " + ex.getMessage());
		}
	}
}
