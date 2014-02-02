package org.trade.ui.strategy;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import jsyntaxpane.DefaultSyntaxKit;
import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BrokerModel;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.DynamicCode;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.Rule;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.TradestrategyTest;
import org.trade.persistent.dao.Tradingday;
import org.trade.strategy.StrategyRule;
import org.trade.strategy.data.StrategyData;
import org.trade.ui.TradeAppLoadConfig;
import org.trade.ui.base.StreamEditorPane;

/**
 */
public class StrategyPanelTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(StrategyPanelTest.class);

	private String symbol = "TEST";
	private PersistentModel tradePersistentModel = null;
	private Tradestrategy tradestrategy = null;
	private String m_templateName = null;
	private String m_strategyDir = null;
	private String m_tmpDir = "temp";

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		try {
			TradeAppLoadConfig.loadAppProperties();
			m_templateName = ConfigProperties
					.getPropAsString("trade.strategy.template");
			TestCase.assertNotNull(m_templateName);
			m_strategyDir = ConfigProperties
					.getPropAsString("trade.strategy.default.dir");
			TestCase.assertNotNull(m_strategyDir);
			this.tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			this.tradestrategy = TradestrategyTest.getTestTradestrategy(symbol);
			TestCase.assertNotNull(this.tradestrategy);
			List<Strategy> strategies = this.tradePersistentModel
					.findStrategies();
			TestCase.assertNotNull(strategies);
			for (Strategy strategy : strategies) {
				String fileName = m_strategyDir + "/"
						+ StrategyRule.PACKAGE.replace('.', '/')
						+ strategy.getClassName() + ".java";

				String content = readFile(fileName);
				TestCase.assertNotNull(content);
				if (strategy.getRules().isEmpty()) {
					Rule nextRule = new Rule(strategy, 1, null, new Date(),
							content.getBytes(), new Date());
					strategy.add(nextRule);
					this.tradePersistentModel.persistAspect(nextRule);
				}
			}
		} catch (Exception ex) {
			TestCase.fail("Error getting Yahoo data msg: "
					+ ex.getCause().getMessage());
		}
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		File dir = new File(m_tmpDir);
		StrategyPanel.deleteDir(dir);
		TradestrategyTest.clearDBData();
	}

	@Test
	public void testJEditorPaneTextEquals() {

		try {
			DefaultSyntaxKit.initKit();
			JEditorPane sourceText = new JEditorPane();
			JScrollPane jScrollPane = new JScrollPane(sourceText);
			jScrollPane.setEnabled(true);
			sourceText.setContentType("text/java");
			sourceText.setFont(new Font("monospaced", Font.PLAIN, 12));
			sourceText.setBackground(Color.white);
			sourceText.setForeground(Color.black);
			sourceText.setSelectedTextColor(Color.black);
			sourceText.setSelectionColor(Color.red);
			sourceText.setEditable(true);

			String fileName = m_strategyDir + "/"
					+ StrategyRule.PACKAGE.replace('.', '/') + m_templateName
					+ ".java";
			String content = readFile(fileName);
			sourceText.setText(content);
			TestCase.assertEquals(content, sourceText.getText());
			writeFile(fileName, content);
			String content1 = readFile(fileName);
			sourceText.setText(null);
			sourceText.setText(content1);
			TestCase.assertEquals(content1, sourceText.getText());

		} catch (Exception ex) {
			TestCase.fail("Error creating instance : " + ex.getMessage());
		}
	}

	/**
	 * Method readFile.
	 * 
	 * @param fileName
	 *            String
	 * @return String
	 * @throws IOException
	 */
	private String readFile(String fileName) throws IOException {

		FileReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		inputStreamReader = new FileReader(fileName);
		bufferedReader = null;
		bufferedReader = new BufferedReader(inputStreamReader);
		String newLine = "\n";
		StringBuffer sb = new StringBuffer();
		String line;

		while ((line = bufferedReader.readLine()) != null) {
			sb.append(line + newLine);
		}

		if (null != bufferedReader)
			bufferedReader.close();

		if (null != inputStreamReader)
			inputStreamReader.close();
		return sb.toString();
	}

	/**
	 * Method writeFile.
	 * 
	 * @param fileName
	 *            String
	 * @param content
	 *            String
	 * @throws IOException
	 */
	private void writeFile(String fileName, String content) throws IOException {

		OutputStream out = new FileOutputStream(fileName);
		out.write(content.getBytes());
		out.flush();
		out.close();
	}

	@Test
	public void testDoCompileAndRunStrategy() {
		try {
			BrokerModel m_brokerManagerModel = (BrokerModel) ClassFactory
					.getServiceForInterface(BrokerModel._brokerTest, this);

			Vector<Object> parm = new Vector<Object>(0);
			parm.add(m_brokerManagerModel);
			parm.add(this.tradestrategy.getStrategyData());
			parm.add(this.tradestrategy.getIdTradeStrategy());
			_log.info("Ready to create Strategy");
			DynamicCode dynacode = new DynamicCode();
			dynacode.addSourceDir(new File(m_strategyDir));
			StrategyRule strategyProxy = (StrategyRule) dynacode
					.newProxyInstance(StrategyRule.class, StrategyRule.PACKAGE
							+ m_templateName, parm);
			_log.info("Created Strategy" + strategyProxy);
			strategyProxy.execute();
			try {
				do {
					Thread.sleep(1000);
				} while (!strategyProxy.isWaiting());

			} catch (InterruptedException e) {
				_log.info(" Thread interupt: " + e.getMessage());
			}

			StrategyData.doDummyData(tradestrategy.getStrategyData()
					.getBaseCandleSeries(), Tradingday.newInstance(new Date()),
					1, BarSize.FIVE_MIN, true, 1);
			TestCase.assertFalse(tradestrategy.getStrategyData()
					.getBaseCandleSeries().isEmpty());
			strategyProxy.cancel();

		} catch (Exception ex) {
			TestCase.fail("Error creating instance : " + ex.getMessage());
		}
	}

	@Test
	public void testDoCompileRule() {
		File srcDirFile = null;
		try {
			BrokerModel m_brokerManagerModel = (BrokerModel) ClassFactory
					.getServiceForInterface(BrokerModel._brokerTest, this);

			Vector<Object> parm = new Vector<Object>(0);
			parm.add(m_brokerManagerModel);
			parm.add(this.tradestrategy.getStrategyData());
			parm.add(this.tradestrategy.getIdTradeStrategy());
			Strategy strategy = this.tradePersistentModel
					.findStrategyById(this.tradestrategy.getStrategy()
							.getIdStrategy());
			Integer version = this.tradePersistentModel
					.findRuleByMaxVersion(strategy);
			Rule myRule = null;
			for (Rule rule : strategy.getRules()) {
				if (version.equals(rule.getVersion()))
					myRule = rule;
			}
			TestCase.assertNotNull(myRule);
			String fileDir = m_tmpDir + "/"
					+ StrategyRule.PACKAGE.replace('.', '/');
			String className = strategy.getClassName() + ".java";

			srcDirFile = new File(fileDir);
			srcDirFile.mkdirs();
			srcDirFile.deleteOnExit();
			FileWriter fileWriter = new FileWriter(fileDir + className);
			PrintWriter writer = new PrintWriter(fileWriter);
			writer.println(new String(myRule.getRule()));
			writer.flush();
			writer.close();
			fileWriter.close();

			_log.info("Ready to create Strategy");
			DynamicCode dynacode = new DynamicCode();
			dynacode.addSourceDir(new File(m_tmpDir));
			StrategyRule strategyRule = (StrategyRule) dynacode
					.newProxyInstance(StrategyRule.class, StrategyRule.PACKAGE
							+ strategy.getClassName(), parm);
			TestCase.assertNotNull(strategyRule);

		} catch (Exception ex) {
			TestCase.fail("Error compiling rule : " + ex.getMessage());
		}
	}

	@Test
	public void testDoCompile() {
		try {
			StrategyPanel strategyPanel = new StrategyPanel(
					this.tradePersistentModel);
			List<Strategy> strategies = this.tradePersistentModel
					.findStrategies();
			TestCase.assertNotNull("No strategies", strategies);
			TestCase.assertEquals(false, strategies.isEmpty());

			Strategy strategy = strategies.get(0);
			TestCase.assertNotNull(strategy);
			Rule myrule = null;

			Collections.sort(strategy.getRules(), Rule.VERSION_ORDER);

			for (Rule rule : strategy.getRules()) {
				myrule = rule;
				break;
			}
			if (null == myrule) {
				myrule = new Rule();
				myrule.setVersion(0);
				myrule.setStrategy(strategy);

			} else {
				myrule.setVersion(myrule.getVersion() + 1);
				myrule.setIdRule(null);
			}
			TestCase.assertNotNull(myrule);
			strategyPanel.doCompile(myrule);
		} catch (Exception ex) {
			TestCase.fail("Error saving rule : " + ex.getMessage());
		}
	}

	@Test
	public void testDoSave() {
		try {
			StrategyPanel strategyPanel = new StrategyPanel(
					this.tradePersistentModel);
			List<Strategy> strategies = this.tradePersistentModel
					.findStrategies();
			TestCase.assertNotNull("No strategies", strategies);
			TestCase.assertEquals(false, strategies.isEmpty());

			Strategy strategy = strategies.get(0);
			TestCase.assertNotNull(strategy);
			Rule myrule = null;

			Collections.sort(strategy.getRules(), Rule.VERSION_ORDER);

			for (Rule rule : strategy.getRules()) {
				myrule = rule;
			}
			if (null == myrule) {
				myrule = new Rule();
				myrule.setVersion(0);
				myrule.setStrategy(strategy);

			} else {
				myrule.setVersion(myrule.getVersion() + 1);
				myrule.setIdRule(null);
			}
			myrule.setComment("Test Ver: " + myrule.getVersion());
			myrule.setCreateDate(new Date());
			StreamEditorPane textArea = new StreamEditorPane("text/rtf");
			new JScrollPane(textArea);
			String fileDir = m_strategyDir + "/"
					+ StrategyRule.PACKAGE.replace('.', '/');
			String className = strategy.getClassName() + ".java";
			String fileName = fileDir + className;
			String content = strategyPanel.readFile(fileName);
			textArea.setText(content);
			myrule.setRule(textArea.getText().getBytes());
			myrule = this.tradePersistentModel.persistAspect(myrule);
			TestCase.assertNotNull(myrule.getIdRule());
			Rule ruleSaved = this.tradePersistentModel.findRuleById(myrule
					.getIdRule());
			TestCase.assertNotNull(ruleSaved.getIdRule());
			String javaCode = new String(ruleSaved.getRule());
			TestCase.assertEquals(javaCode, textArea.getText());
			_log.info("Java file to Saved: " + javaCode);

		} catch (Exception ex) {
			TestCase.fail("Error saving rule : " + ex.getMessage());
		}
	}
}
