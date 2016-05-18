package org.trade.strategy.data;

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
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.TradestrategyTest;
import org.trade.ui.TradeAppLoadConfig;

public class CandleSeriesTest {

	private final static Logger _log = LoggerFactory.getLogger(CandleSeriesTest.class);
	@Rule
	public TestName name = new TestName();

	private String symbol = "TEST";
	private Tradestrategy tradestrategy = null;

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
		try {
			TradeAppLoadConfig.loadAppProperties();
			this.tradestrategy = TradestrategyTest.getTestTradestrategy(symbol);
			assertNotNull("1", this.tradestrategy);
		} catch (Exception | AssertionError ex) {
			String msg = "Error running " + name.getMethodName() + " msg: " + ex.getMessage();
			_log.error(msg);
			fail(msg);
		}
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		TradestrategyTest.clearDBData();
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
	public void testCandleSeriessClone() {
		try {

			CandleSeries candleSeries = this.tradestrategy.getStrategyData().getBaseCandleSeries();
			CandleSeries series = (CandleSeries) this.tradestrategy.getStrategyData().getBaseCandleSeries().clone();
			if (candleSeries.equals(series)) {
				_log.info("CandleSeries: " + series.toString());
			}
			assertEquals("1", series, candleSeries);
		} catch (Exception | AssertionError ex) {
			String msg = "Error running " + name.getMethodName() + " msg: " + ex.getMessage();
			_log.error(msg);
			fail(msg);
		}
	}

}
