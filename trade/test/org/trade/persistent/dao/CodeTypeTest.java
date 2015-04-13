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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class CodeTypeTest {

	private final static Logger _log = LoggerFactory
			.getLogger(CodeTypeTest.class);
	@Rule
	public TestName name = new TestName();

	/**
	 * Method setUpBeforeClass.
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * Method setUp.
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Method tearDownAfterClass.
	 * 
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testFindCodeValueByName() {

		try {
			CodeTypeHome codeTypeHome = new CodeTypeHome();
			CodeType codeType = codeTypeHome.findByName("MovingAverage");
			assertNotNull(codeType);
			_log.info("CodeType id: " + codeType.getIdCodeType());
			CodeValue codeValue = codeTypeHome.findByAttributeName(
					codeType.getName(), "Length");
			assertNotNull(codeValue);
			_log.info("CodeValue id: " + codeValue.getIdCodeValue());
		} catch (Exception | AssertionError ex) {
			String msg = "Error running " + name.getMethodName() + " msg: "
					+ ex.getMessage();
			_log.error(msg);
			fail(msg);
		}
	}
}
