package org.trade.ui.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.dictionary.valuetype.Exchange;
import org.trade.dictionary.valuetype.SECType;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.StrategyHome;
import org.trade.persistent.dao.Tradingday;
import org.trade.strategy.data.CandleDataset;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandlePeriod;
import org.trade.ui.MainPanelMenu;
import org.trade.ui.TradeAppLoadConfig;
import org.trade.ui.base.BasePanel;
import org.trade.ui.base.BasePanelMenu;
import org.trade.ui.base.ComponentPrintService;
import org.trade.ui.base.ImageBuilder;
import org.trade.ui.base.WaitCursorEventQueue;
import org.trade.ui.widget.Clock;

/**
 */
public class CandlestickChartApp extends BasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4275291770705110409L;
	/**
	 * 
	 */

	private final static Logger _log = LoggerFactory
			.getLogger(CandlestickChartApp.class);

	private JPanel m_menuPanel = null;

	// Main method
	/**
	 * Method main.
	 * 
	 * @param args
	 *            String[]
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					TradeAppLoadConfig.loadAppProperties();
					JFrame frame = new JFrame();
					String symbol = "MSFT";
					// StrategyData data = CandlestickChartTest
					// .getPriceDataSetYahooDay(symbol);
					Integer numberOfDays = 2;
					StrategyData strategyData = CandlestickChartApp
							.getPriceDataSetYahooIntraday(symbol, numberOfDays,
									BarSize.FIVE_MIN);
					CandlestickChart chart = new CandlestickChart(symbol,
							strategyData, Tradingday.newInstance(new Date()));
					CandlestickChartApp panel = new CandlestickChartApp(chart);

					frame.getContentPane().add(panel);
					frame.setSize(1200, 900);
					Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

					frame.setLocation((d.width - frame.getSize().width) / 2,
							(d.height - frame.getSize().height) / 2);
					frame.setIconImage(ImageBuilder.getImage("trade.gif"));
					frame.validate();
					frame.repaint();
					frame.setVisible(true);
					EventQueue waitQue = new WaitCursorEventQueue(500);
					Toolkit.getDefaultToolkit().getSystemEventQueue()
							.push(waitQue);
				} catch (Exception ex) {
					_log.error(
							"Error getting Yahoo data msg: " + ex.getMessage(),
							ex);
				}
			}
		});
	}

	/**
	 * Constructor for CandlestickChartApp.
	 * 
	 * @param chart
	 *            CandlestickChart
	 */
	public CandlestickChartApp(CandlestickChart chart) {

		this.setLayout(new BorderLayout());

		JPanel jPanel1 = new JPanel();
		jPanel1.setLayout(new BorderLayout());

		JPanel jPanelProgressBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JProgressBar progressBar = new JProgressBar(0, 0);
		jPanelProgressBar.add(progressBar);

		JPanel jPanelClock = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Clock clock = new Clock();
		jPanelClock.add(clock);

		JPanel jPanelStatus = new JPanel();
		jPanelStatus.setLayout(new GridLayout());
		JTextField jTextFieldStatus = new JTextField();
		jTextFieldStatus.setRequestFocusEnabled(false);
		jTextFieldStatus.setMargin(new Insets(5, 5, 5, 5));
		jTextFieldStatus.setBackground(Color.white);
		jTextFieldStatus.setBorder(BorderFactory.createLoweredBevelBorder());
		jPanelStatus.add(jTextFieldStatus);

		JPanel jPanel3 = new JPanel();
		jPanel3.setLayout(new BorderLayout());
		jPanel3.add(jPanelClock, BorderLayout.WEST);
		jPanel3.add(jPanelProgressBar, BorderLayout.EAST);
		jPanel3.add(jPanelStatus, BorderLayout.CENTER);

		JPanel jPanel2 = new JPanel();
		jPanel2.setLayout(new BorderLayout());
		jPanel2.add(chart, BorderLayout.CENTER);
		jPanel1.add(jPanel2, BorderLayout.CENTER);
		jPanel1.add(jPanel3, BorderLayout.SOUTH);
		m_menuPanel = new JPanel();
		m_menuPanel.setLayout(new BorderLayout());
		jPanel1.add(m_menuPanel, BorderLayout.NORTH);
		this.add(jPanel1, BorderLayout.CENTER);
		this.setStatusBar(jTextFieldStatus);
		this.setProgressBar(progressBar);

		MainPanelMenu m_menuBar = new MainPanelMenu(this);
		setMenu(m_menuBar);
		/* This is always true as main panel needs to receive all events */
		setSelected(true);

	}

	/**
	 * Method setMenu.
	 * 
	 * @param menu
	 *            BasePanelMenu
	 */
	public void setMenu(BasePanelMenu menu) {
		m_menuPanel.removeAll();
		m_menuPanel.add(menu, BorderLayout.NORTH);
		super.setMenu(menu);
	}

	public void doWindowClose() {

	}

	public void doWindowActivated() {

	}

	/**
	 * Method doWindowDeActivated.
	 * 
	 * @return boolean
	 */
	public boolean doWindowDeActivated() {
		return true;
	}

	public void doWindowOpen() {

	}

	public void doPrint() {
		try {

			PageFormat pageFormat = new PageFormat();
			ComponentPrintService vista = new ComponentPrintService(
					((JFrame) this.getFrame()).getContentPane(), pageFormat);
			vista.scaleToFit(true);
			// vista.print();
			PrinterJob pj = PrinterJob.getPrinterJob();
			vista.scaleToFit(true);
			pj.validatePage(pageFormat);
			pj.setPageable(vista);

			if (pj.printDialog()) {
				pj.print();
			}

		} catch (Exception ex) {
			_log.error("Error printing msg: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Method getPriceDataSetYahooDay.
	 * 
	 * @param symbol
	 *            String
	 * @return StrategyData
	 */
	protected static StrategyData getPriceDataSetYahooDay(String symbol) {
		try {

			List<Candle> candles = new ArrayList<Candle>();
			Strategy daoStrategy = (Strategy) DAOStrategy.newInstance()
					.getObject();
			StrategyHome home = new StrategyHome();
			String name = daoStrategy.getName();
			Strategy strategy = home.findByName(name);
			Contract contract = new Contract(SECType.STOCK, symbol,
					Exchange.SMART, Currency.USD, null, null);
			Date today = new Date();
			Date startDate = TradingCalendar.addMonth(today, -3);

			/*
			 * Yahoo finance So IBM form 1/1/2012 thru 06/30/2012
			 * http://ichart.finance
			 * .yahoo.com/table.csv?s=IBM&a=0&b=1&c=2012&d=5
			 * &e=30&f=2012&ignore=.csv"
			 */

			String strUrl = "http://ichart.finance.yahoo.com/table.csv?s="
					+ symbol + "&a=" + TradingCalendar.getMonth(startDate)
					+ "&b=" + TradingCalendar.getDayOfMonth(startDate) + "&c="
					+ TradingCalendar.getYear(startDate) + "&d="
					+ TradingCalendar.getMonth(today) + "&e="
					+ TradingCalendar.getDayOfMonth(today) + "&f="
					+ TradingCalendar.getYear(today) + "&ignore=.csv";
			URL url = new URL(strUrl);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			DateFormat df = new SimpleDateFormat("y-M-d");
			String inputLine;
			in.readLine();
			while ((inputLine = in.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(inputLine, ",");
				Date date = df.parse(st.nextToken());
				Tradingday tradingday = Tradingday.newInstance(date);
				double open = Double.parseDouble(st.nextToken());
				double high = Double.parseDouble(st.nextToken());
				double low = Double.parseDouble(st.nextToken());
				double close = Double.parseDouble(st.nextToken());
				long volume = Long.parseLong(st.nextToken());
				// double adjClose = Double.parseDouble( st.nextToken() );
				CandlePeriod period = new CandlePeriod(
						TradingCalendar.getBusinessDayStart(date),
						TradingCalendar.addSeconds(
								TradingCalendar.getBusinessDayEnd(date), -1));

				Candle candle = new Candle(contract, period, open, high, low,
						close, volume, (open + close) / 2,
						((int) volume / 100), new Date());

				candle.setContract(contract);
				candle.setTradingday(tradingday);
				candle.setLastUpdateDate(candle.getStartPeriod());
				candles.add(candle);
			}
			in.close();

			Collections.reverse(candles);
			CandleDataset candleDataset = new CandleDataset();
			int daySeconds = (int) ((TradingCalendar.getBusinessDayEnd(today)
					.getTime() - TradingCalendar.getBusinessDayStart(today)
					.getTime()) / 1000);
			CandleSeries candleSeries = new CandleSeries(contract.getSymbol(),
					contract, daySeconds, startDate, today);
			candleDataset.addSeries(candleSeries);
			StrategyData strategyData = new StrategyData(strategy,
					candleDataset);
			CandleDataset.populateSeries(strategyData, candles);
			return strategyData;
		} catch (Exception ex) {
			_log.error("Error getting Yahoo data msg: " + ex.getMessage(), ex);
		}
		return null;
	}

	/**
	 * Method getPriceDataSetYahooIntraday.
	 * 
	 * @param symbol
	 *            String
	 * @param days
	 *            int
	 * @param periodSeconds
	 *            int
	 * @return StrategyData
	 */
	protected static StrategyData getPriceDataSetYahooIntraday(String symbol,
			int days, int periodSeconds) {
		try {
			Date today = new Date();
			Date startDate = TradingCalendar.addDays(today, days * -1);
			startDate = TradingCalendar.getMostRecentTradingDay(startDate);
			Strategy daoStrategy = (Strategy) DAOStrategy.newInstance()
					.getObject();
			StrategyHome home = new StrategyHome();
			String name = daoStrategy.getName();
			Strategy strategy = home.findByName(name);
			Contract contract = new Contract(SECType.STOCK, symbol,
					Exchange.SMART, Currency.USD, null, null);
			CandleDataset candleDataset = new CandleDataset();
			CandleSeries candleSeries = new CandleSeries(contract.getSymbol(),
					contract, periodSeconds, startDate, today);
			candleDataset.addSeries(candleSeries);
			StrategyData strategyData = new StrategyData(strategy,
					candleDataset);

			/*
			 * Yahoo finance
			 * http://chartapi.finance.yahoo.com/instrument/1.0/IBM
			 * /chartdata;type=quote;range=1d/csv/
			 */

			String strUrl = "http://chartapi.finance.yahoo.com/instrument/1.0/"
					+ symbol + "/chartdata;type=quote;range=" + days + "d/csv/";

			_log.info("URL : " + strUrl);
			URL url = new URL(strUrl);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));

			String inputLine;
			in.readLine();
			while ((inputLine = in.readLine()) != null) {

				if (inputLine.indexOf(":") == -1) {
					StringTokenizer scanLine = new StringTokenizer(inputLine,
							",");
					while (scanLine.hasMoreTokens()) {
						Date time = new Date(Long.parseLong(scanLine
								.nextToken()) * 1000);

						// values:Timestamp,close,high,low,open,volume
						double close = Double.parseDouble(scanLine.nextToken());
						double high = Double.parseDouble(scanLine.nextToken());
						double low = Double.parseDouble(scanLine.nextToken());
						double open = Double.parseDouble(scanLine.nextToken());
						long volume = Long.parseLong(scanLine.nextToken());
						_log.info("Time : " + time + " Open: " + open
								+ " High: " + high + " Low: " + low
								+ " Close: " + close + " Volume: " + volume);
						if (startDate.before(time)) {
							strategyData.buildCandle(time, open, high, low,
									close, volume, (open + close) / 2,
									((int) volume / 100), periodSeconds
											/ BarSize.FIVE_MIN, null);
						}
					}
				}
			}
			in.close();
			return strategyData;
		} catch (Exception ex) {
			_log.error("Error getting Yahoo data msg: " + ex.getMessage(), ex);
		}
		return null;
	}

	/**
	 */
	class ComponentPrintable implements Printable {
		private Component m_component;

		/**
		 * Constructor for ComponentPrintable.
		 * 
		 * @param c
		 *            Component
		 */
		public ComponentPrintable(Component c) {
			m_component = c;
		}

		/**
		 * Method print.
		 * 
		 * @param g
		 *            Graphics
		 * @param pageFormat
		 *            PageFormat
		 * @param pageIndex
		 *            int
		 * @return int
		 * @see java.awt.print.Printable#print(Graphics, PageFormat, int)
		 */
		public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
			if (pageIndex > 0)
				return NO_SUCH_PAGE;
			Graphics2D g2 = (Graphics2D) g;
			g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			boolean wasBuffered = disableDoubleBuffering(m_component);
			m_component.printAll(g2);
			restoreDoubleBuffering(m_component, wasBuffered);
			return PAGE_EXISTS;
		}

		/**
		 * Method disableDoubleBuffering.
		 * 
		 * @param c
		 *            Component
		 * @return boolean
		 */
		private boolean disableDoubleBuffering(Component c) {
			if (!(c instanceof JComponent))
				return false;
			JComponent jc = (JComponent) c;
			boolean wasBuffered = jc.isDoubleBuffered();
			jc.setDoubleBuffered(false);
			return wasBuffered;
		}

		/**
		 * Method restoreDoubleBuffering.
		 * 
		 * @param c
		 *            Component
		 * @param wasBuffered
		 *            boolean
		 */
		private void restoreDoubleBuffering(Component c, boolean wasBuffered) {
			if (c instanceof JComponent)
				((JComponent) c).setDoubleBuffered(wasBuffered);
		}
	}
}
