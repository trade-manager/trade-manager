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
package org.trade.ui.configuration;

import java.util.Vector;

import junit.framework.TestCase;

import org.jfree.data.DataUtilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.factory.ClassFactory;
import org.trade.dictionary.valuetype.CalculationType;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.persistent.dao.CodeAttribute;
import org.trade.persistent.dao.CodeType;
import org.trade.persistent.dao.CodeValue;
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.ui.TradeAppLoadConfig;

/**
 * Some tests for the {@link DataUtilities} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class ConfigurationPanelTest {

	private final static Logger _log = LoggerFactory
			.getLogger(ConfigurationPanelTest.class);

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
		TradeAppLoadConfig.loadAppProperties();
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
	public void testCreateIntegerClass() {
		try {
			CodeType codeType = new CodeType("Moving Average", "Moving Average");
			CodeAttribute codeAttribute = new CodeAttribute(codeType, "Length",
					"The length of the MA", "10", "java.lang.Integer", null);
			CodeValue codeValue = new CodeValue(codeAttribute, "20");

			Vector<Object> parm = new Vector<Object>();
			parm.add(codeValue.getCodeValue());

			Integer value = (Integer) ClassFactory.getCreateClass(codeValue
					.getCodeAttribute().getClassName(), parm, this);
			_log.info("Value is: " + value);
			TestCase.assertEquals(20, value, 0);

		} catch (Exception ex) {
			TestCase.fail("Error creating class: " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testCreateBooleanClass() {
		try {
			CodeType codeType = new CodeType("Moving Average", "Moving Average");
			CodeAttribute codeAttribute = new CodeAttribute(codeType, "Length",
					"The length of the MA", "true", "java.lang.Boolean", null);
			CodeValue codeValue = new CodeValue(codeAttribute, "true");

			Vector<Object> parm = new Vector<Object>();
			parm.add(codeValue.getCodeValue());

			Boolean value = (Boolean) ClassFactory.getCreateClass(codeValue
					.getCodeAttribute().getClassName(), parm, this);
			_log.info("Value is: " + value);
			TestCase.assertEquals(new Boolean(true), value);

		} catch (Exception ex) {
			TestCase.fail("Error creating class: " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testCreateStringClass() {
		try {
			CodeType codeType = new CodeType("Moving Average", "Moving Average");
			CodeAttribute codeAttribute = new CodeAttribute(codeType, "Length",
					"The length of the MA", "Test", "java.lang.String", null);
			CodeValue codeValue = new CodeValue(codeAttribute, "Simple");

			Vector<Object> parm = new Vector<Object>();
			parm.add(codeValue.getCodeValue());

			String value = (String) ClassFactory.getCreateClass(codeValue
					.getCodeAttribute().getClassName(), parm, this);
			TestCase.assertEquals("Simple", value);
			_log.info("Value is: " + value);
		} catch (Exception ex) {
			TestCase.fail("Error creating class: " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testCreateDecodeClass() {
		try {
			CodeType codeType = new CodeType("Moving Average", "Moving Average");
			CodeAttribute codeAttribute = new CodeAttribute(codeType,
					"SMAType", "The length of the MA", "LINEAR",
					"org.trade.dictionary.valuetype.CalculationType", null);
			CodeValue codeValue = new CodeValue(codeAttribute,
					CalculationType.LINEAR);

			Vector<Object> parm = new Vector<Object>();
			// parm.add(codeValue.getCodeValue());

			CalculationType value = (CalculationType) ClassFactory
					.getCreateClass(
							codeValue.getCodeAttribute().getClassName(), parm,
							this);
			value.setValue(CalculationType.LINEAR);
			TestCase.assertEquals(CalculationType.LINEAR, value.getCode());
			_log.info("Value is: " + value);
		} catch (Exception ex) {
			TestCase.fail("Error creating class: " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testCreateIndicatorSeriesClass() {
		try {

			final String packageName = "org.trade.strategy.data.";
			Strategy strategy = (Strategy) DAOStrategy.newInstance()
					.getObject();
			Vector<Object> parm = new Vector<Object>();
			parm.add(strategy);
			parm.add("20-SMA");
			parm.add(IndicatorSeries.MovingAverageSeries);
			parm.add("20 Simple Moving Average");
			parm.add(new Boolean(false));
			parm.add(new Integer(0));
			parm.add(new Boolean(false));
			String className = packageName
					+ IndicatorSeries.MovingAverageSeries;

			IndicatorSeries value = (IndicatorSeries) ClassFactory
					.getCreateClass(className, parm, this);

			TestCase.assertEquals(value.getClass().getName(), className);
			_log.info("Value is: " + value);
		} catch (Exception ex) {
			TestCase.fail("Error creating class: " + ex.getCause().getMessage());
		}
	}
}
