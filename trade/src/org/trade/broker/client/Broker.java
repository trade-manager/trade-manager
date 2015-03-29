package org.trade.broker.client;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;

import org.trade.persistent.dao.Tradestrategy;
import org.trade.strategy.StrategyChangeListener;
import org.trade.strategy.StrategyRuleException;

public abstract class Broker extends SwingWorker<Void, Void> implements
		StrategyChangeListener {

	protected AtomicInteger ruleComplete = new AtomicInteger(0);
	protected AtomicInteger strategiesRunning = new AtomicInteger(0);
	protected final Object lockBackTestWorker = new Object();

	public Broker() {

	}

	/**
	 * Method strategyComplete.
	 * 
	 * @param strategyClassName
	 *            String
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#strategyComplete(Tradestrategy)
	 */
	public synchronized void strategyComplete(String strategyClassName,
			Tradestrategy tradestrategy) {
		synchronized (lockBackTestWorker) {
			strategiesRunning.getAndDecrement();
			lockBackTestWorker.notifyAll();
		}
	}

	/**
	 * Method strategyStarted.
	 * 
	 * @param strategyClassName
	 *            String
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#strategyStarted(Tradestrategy)
	 */
	public synchronized void strategyStarted(String strategyClassName,
			Tradestrategy tradestrategy) {
		synchronized (lockBackTestWorker) {
			strategiesRunning.getAndIncrement();
			lockBackTestWorker.notifyAll();
		}
	}

	/**
	 * Method ruleComplete.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#ruleComplete(Tradestrategy)
	 */
	public synchronized void ruleComplete(Tradestrategy tradestrategy) {
		synchronized (lockBackTestWorker) {
			ruleComplete.getAndIncrement();
			lockBackTestWorker.notifyAll();
		}
	}

	/**
	 * Method strategyError.
	 * 
	 * @param strategyError
	 *            StrategyRuleException
	 * @see org.trade.strategy.StrategyChangeListener#strategyError(StrategyRuleException)
	 */
	public void strategyError(StrategyRuleException strategyError) {
		this.cancel(true);
	}

}
