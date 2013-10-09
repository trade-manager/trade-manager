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
package org.trade.strategy;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.swing.event.EventListenerList;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BrokerModel;
import org.trade.broker.BrokerModelException;
import org.trade.core.factory.ClassFactory;
import org.trade.core.util.TradingCalendar;
import org.trade.core.util.Worker;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.DAOEntryLimit;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.dictionary.valuetype.OverrideConstraints;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.TimeInForce;
import org.trade.dictionary.valuetype.TriggerMethod;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Entrylimit;
import org.trade.persistent.dao.PositionOrders;
import org.trade.persistent.dao.TradePosition;
import org.trade.persistent.dao.Account;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.candle.CandlePeriod;

/**
 */
public abstract class AbstractStrategyRule extends Worker implements
		SeriesChangeListener, StrategyRule, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4876874276185644936L;

	private final static Logger _log = LoggerFactory
			.getLogger(AbstractStrategyRule.class);

	/*
	 * Message handler that allows the main controller to listen for errors.
	 * Storage for registered change listeners.
	 */
	private transient EventListenerList listenerList;

	private BrokerModel brokerModel;
	private PersistentModel tradePersistentModel;
	private DAOEntryLimit entryLimits = new DAOEntryLimit();
	private StrategyData datasetContainer = null;
	private Tradestrategy tradestrategy = null;
	private PositionOrders positionOrders = null;
	private Integer idTradestrategy = null;
	private String symbol = null;
	private boolean seriesChanged = false;
	private final Object lockStrategyWorker = new Object();
	private boolean listeningCandles = false;
	private int currentCandleCount = -1;
	private Date strategyLastFired = new Date();

	/**
	 * Constructor for AbstractStrategyRule. An abstract class that implements
	 * the base functionality for a trading strategies this class monitors the
	 * candle data set for changes. This class runs in its own thread. there
	 * will be one Strategy running per tradestrategy.
	 * 
	 * @param brokerManagerModel
	 *            BrokerModel
	 * @param datasetContainer
	 *            StrategyData
	 * @param idTradestrategy
	 *            Integer
	 */
	public AbstractStrategyRule(BrokerModel brokerManagerModel,
			StrategyData datasetContainer, Integer idTradestrategy) {
		this.listenerList = new EventListenerList();
		this.brokerModel = brokerManagerModel;
		this.datasetContainer = datasetContainer;
		this.idTradestrategy = idTradestrategy;
	}

	/**
	 * Method error. All errors are sent via this method to any class that is
	 * listening to this strategy. Usually this is the main controller.
	 * 
	 * @param id
	 *            int
	 * @param errorCode
	 *            int
	 * @param errorMsg
	 *            String
	 * @see org.trade.strategy.StrategyRule#error(int, int, String)
	 */
	public void error(int id, int errorCode, String errorMsg) {

		if (id > 0) {
			_log.error("StrategyWorkerError symbol: " + symbol + " Error Id: "
					+ id + " Error Code: " + errorCode + " Error Msg: "
					+ errorMsg);
		}
		this.fireStrategyError(new StrategyRuleException(id, errorCode,
				"Symbol: " + symbol + " " + errorMsg));
		/*
		 * For Errors close the strategy down.
		 */
		if (id == 1) {
			this.cancel();
		}
	}

	/**
	 * Registers an object to receive notification of changes to the
	 * strategyRule.
	 * 
	 * @param listener
	 *            the object to register.
	 * 
	 * 
	 * @see #removeChangeListener(StrategyChangeListener)
	 */
	public void addMessageListener(StrategyChangeListener listener) {
		this.listenerList.add(StrategyChangeListener.class, listener);
	}

	/**
	 * Deregisters an object so that it no longer receives notification of
	 * changes to the strategyRule.
	 * 
	 * @param listener
	 *            the object to deregister.
	 * 
	 * 
	 * @see #addChangeListener(StrategyChangeListener)
	 */
	public void removeMessageListener(StrategyChangeListener listener) {
		this.listenerList.remove(StrategyChangeListener.class, listener);
	}

	public void removeAllMessageListener() {
		StrategyChangeListener[] listeners = this.listenerList
				.getListeners(StrategyChangeListener.class);
		for (int i = 0; i < listeners.length; i++) {
			removeMessageListener(listeners[i]);
		}
	}

	/**
	 * Notifies all registered listeners that the strategyRule has an error.
	 * 
	 * 
	 * @param strategyError
	 *            StrategyRuleException
	 * @see #addChangeListener(StrategyChangeListener)
	 */
	protected void fireStrategyError(StrategyRuleException strategyError) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == StrategyChangeListener.class) {
				((StrategyChangeListener) listeners[i + 1])
						.strategyError(strategyError);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the strategyRule has completed.
	 * 
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see #addChangeListener(StrategyChangeListener)
	 */
	protected void fireStrategyComplete(String strategyClassName,
			Tradestrategy tradestrategy) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == StrategyChangeListener.class) {
				((StrategyChangeListener) listeners[i + 1]).strategyComplete(
						strategyClassName, tradestrategy);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the strategyRule has started.
	 * 
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see #addChangeListener(StrategyChangeListener)
	 */
	protected void fireStrategyStarted(String strategyClassName,
			Tradestrategy tradestrategy) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == StrategyChangeListener.class) {
				((StrategyChangeListener) listeners[i + 1]).strategyStarted(
						strategyClassName, tradestrategy);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the strategyRule rule has
	 * completed.
	 * 
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see #addChangeListener(StrategyChangeListener)
	 */
	protected void fireRuleComplete(Tradestrategy tradestrategy) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == StrategyChangeListener.class) {
				((StrategyChangeListener) listeners[i + 1])
						.ruleComplete(tradestrategy);
			}
		}
	}

	/**
	 * The main process thread. This will run until it is either canceled or is
	 * done.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.trade.strategy.impl.Worker#doInBackground()
	 */

	protected Void doInBackground() {

		/*
		 * We initialize here to keep this instances as part of this worker
		 * thread
		 */
		try {

			this.tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			// Get an instances for this thread.
			this.tradestrategy = this.tradePersistentModel
					.findTradestrategyById(this.idTradestrategy);
			this.tradestrategy.setDatasetContainer(this.datasetContainer);
			this.symbol = this.tradestrategy.getContract().getSymbol();

			_log.info("Starting: " + this.getClass().getName()
					+ " engine doInBackground Symbol: " + this.symbol
					+ " idTradestrategy: " + this.idTradestrategy);

			/*
			 * Process the current candle if there is one on startup.
			 */

			currentCandleCount = this.datasetContainer.getBaseCandleSeries()
					.getItemCount() - 1;

			seriesChanged = true;

			reFreshPositionOrders();

			do {
				/*
				 * Lock until a candle arrives. First time in we process the
				 * current candle.
				 */
				synchronized (lockStrategyWorker) {
					while (!seriesChanged) {
						lockStrategyWorker.wait();
					}
					seriesChanged = false;
				}

				if (!this.isCancelled()) {

					/*
					 * If candle count > than current we have a new candle
					 * 
					 * If equal then we have an updated candle.
					 * 
					 * The currentCandleCount is greater than the candle series.
					 * Then another thread must have cleared the candle series
					 * so shut down the strategy.
					 */
					CandleSeries candleSeries = this.tradestrategy
							.getDatasetContainer().getBaseCandleSeries();

					boolean newCandle = false;
					if ((candleSeries.getItemCount() - 1) > currentCandleCount) {
						/*
						 * Add one to the currentCandleCount until we catch up
						 * to the candleSeries candle count. As it is possible
						 * the candle count in another thread gets ahead of this
						 * thread and so this thread is playing catch up.
						 */
						currentCandleCount++;
						newCandle = true;

					} else if (currentCandleCount > (candleSeries
							.getItemCount() - 1)) {

						_log.info("Cancelled due to candleSeries clear Symbol: "
								+ getSymbol()
								+ " class: "
								+ this.getClass().getName());
						this.cancel();
						break;
					} else if (currentCandleCount == (candleSeries
							.getItemCount() - 1)) {
						/*
						 * We have an updated candle
						 */
					}

					if (currentCandleCount > -1) {
						/*
						 * Check the candle is during the trading range and fire
						 * the rules.
						 */
						if (!getCurrentCandle()
								.getPeriod()
								.getStart()
								.before(this.tradestrategy.getTradingday()
										.getOpen())) {
							/*
							 * Refresh the orders in the positionOrders as these
							 * may have been filled via another thread. This
							 * gets the Orders/OpenPosition and Contract
							 */
							reFreshPositionOrders();
							runStrategy(candleSeries, newCandle);
							strategyLastFired = new Date();
						}
					}
					/*
					 * First time in add a listener for new candle.
					 */
					if (!listeningCandles) {

						/*
						 * Start listening for new candles and candle changes.
						 */
						this.datasetContainer.getBaseCandleSeries()
								.addChangeListener(this);
						/*
						 * Tell the worker if listening. Note only for back
						 * testing that the strategy is running.
						 */
						this.fireStrategyStarted(this.getClass()
								.getSimpleName(), this.tradestrategy);
						listeningCandles = true;

						_log.info("Started: " + this.getClass().getName()
								+ " engine doInBackground Symbol: "
								+ this.symbol + " idTradestrategy: "
								+ this.idTradestrategy);
					} else {
						this.fireRuleComplete(this.tradestrategy);
					}
				}

			} while (!this.isDone() && !this.isCancelled());
		} catch (InterruptedException interExp) {
			// Do nothing.
		} catch (Exception ex) {
			_log.error(
					"Error StrategyWorker exception: " + getSymbol()
							+ " class: " + this.getClass().getName() + " Msg: "
							+ ex.getMessage(), ex);
			error(1, 100, "Error StrategyWorker exception: " + ex.getMessage());
		} finally {
			/*
			 * Ok we are complete clean up.
			 */
		}
		return null;
	}

	/**
	 * Method cancel.
	 * 
	 * @see org.trade.strategy.StrategyRule#cancel()
	 */
	public void cancel() {
		this.setIsCancelled(true);
		/*
		 * Unlock the doInBackground that may be waiting for a candle. This will
		 * cause a clean finish to the process.
		 */
		synchronized (lockStrategyWorker) {
			seriesChanged = true;
			lockStrategyWorker.notifyAll();
		}
	}

	/**
	 * Method runStrategy. This method is called every time the candleSeries is
	 * either updated or a candleItem is added.
	 * 
	 * 
	 * If market data is selected this will fire every time the last price falls
	 * outside the H/L of the current candle. Note also if market data is
	 * selected the current Bid/Ask/Last can be accessed via the
	 * candleSeries.getContract().
	 * 
	 * If market data is not selected this method fires every 5sec as real time
	 * bars update the current candle.
	 * 
	 * @param candleSeries
	 *            CandleSeries
	 * @param newBar
	 *            boolean when ever a new bar is added to the candleSeries.
	 * 
	 * @see org.trade.strategy.StrategyRule#runStrategy(CandleSeries, boolean)
	 */
	public abstract void runStrategy(CandleSeries candleSeries, boolean newBar);

	protected void done() {
		this.fireStrategyComplete(this.getClass().getSimpleName(),
				this.tradestrategy);
		removeAllMessageListener();
		this.datasetContainer.getBaseCandleSeries().removeChangeListener(this);
		_log.info("Rule engine done: " + getSymbol() + " class: "
				+ this.getClass().getSimpleName() + " idTradestrategy: "
				+ this.tradestrategy.getIdTradeStrategy());
	}

	/**
	 * Method seriesChanged. The series change event for the candle series this
	 * receives all changes to the candle data set. these changes happen in the
	 * Broker interface when new data is received by the market.
	 * 
	 * @param event
	 *            SeriesChangeEvent
	 * @see org.jfree.data.general.SeriesChangeListener#seriesChanged(SeriesChangeEvent)
	 */
	public void seriesChanged(SeriesChangeEvent event) {
		synchronized (lockStrategyWorker) {
			seriesChanged = true;
			lockStrategyWorker.notifyAll();
		}
	}

	/**
	 * Method closePosition. This method creates a market order to close the
	 * Trade. The order is persisted and transmitted via the broker interface to
	 * the market.
	 * 
	 * @param transmit
	 *            boolean
	 * @throws StrategyRuleException
	 */
	public TradeOrder closePosition(boolean transmit)
			throws StrategyRuleException {

		if (this.isThereOpenPosition()) {

			int openQuantity = Math.abs(this.getOpenTradePosition()
					.getOpenQuantity());

			if (openQuantity > 0) {
				String action = Action.BUY;
				if (Side.BOT.equals(this.getOpenTradePosition().getSide())) {
					action = Action.SELL;
				}

				return this.createOrder(action, OrderType.MKT, null, null,
						openQuantity, null, TriggerMethod.DEFAULT,
						OverrideConstraints.YES, TimeInForce.DAY, false,
						transmit, this.getOpenPositionOrder().getFAProfile(),
						this.getOpenPositionOrder().getFAGroup(), this
								.getOpenPositionOrder().getFAMethod(), this
								.getOpenPositionOrder().getFAPercent());
			}
		}
		return null;
	}

	/**
	 * Method createOrder.
	 * 
	 * This method creates an open position order for the Trade. The order is
	 * persisted and transmitted via the broker interface to the market.
	 * 
	 * @param action
	 *            String
	 * @param orderType
	 *            String
	 * @param limitPrice
	 *            Money
	 * @param auxPrice
	 *            Money
	 * @param quantity
	 *            int
	 * @param ocaGroupName
	 *            String
	 * @param roundPrice
	 *            boolean
	 * @param transmit
	 *            boolean
	 * @return TradeOrder
	 * @throws StrategyRuleException
	 */
	public TradeOrder createOrder(String action, String orderType,
			Money limitPrice, Money auxPrice, int quantity,
			String ocaGroupName, boolean roundPrice, boolean transmit)
			throws StrategyRuleException {
		return createOrder(action, orderType, limitPrice, auxPrice, quantity,
				ocaGroupName, TriggerMethod.DEFAULT, OverrideConstraints.YES,
				TimeInForce.DAY, roundPrice, transmit, null, null, null, null);
	}

	/**
	 * Method createOrder.
	 * 
	 * This method creates an open position order for the Trade. The order is
	 * persisted and transmitted via the broker interface to the market.
	 * 
	 * 
	 * @param action
	 *            String
	 * @param orderType
	 *            String
	 * @param limitPrice
	 *            Money
	 * @param auxPrice
	 *            Money
	 * @param quantity
	 *            int
	 * @param ocaGroupName
	 *            String
	 * @param triggerMethod
	 *            int
	 * @param overrideConstraints
	 *            int
	 * @param timeInForce
	 *            String
	 * @param roundPrice
	 *            boolean
	 * @param transmit
	 *            boolean
	 * @return TradeOrder
	 * @throws StrategyRuleException
	 */
	public TradeOrder createOrder(String action, String orderType,
			Money limitPrice, Money auxPrice, int quantity,
			String ocaGroupName, int triggerMethod, int overrideConstraints,
			String timeInForce, boolean roundPrice, boolean transmit,
			String FAProfile, String FAGroup, String FAMethod,
			BigDecimal FAPercent) throws StrategyRuleException {

		if (null == orderType)
			throw new StrategyRuleException(1, 200, "Order Type cannot be null");

		if (null == action)
			throw new StrategyRuleException(1, 201, "Action cannot be null");

		if (quantity == 0)
			throw new StrategyRuleException(1, 202, "Quantity cannot be zero");

		if (OrderType.LMT.equals(orderType) && null == limitPrice)
			throw new StrategyRuleException(1, 203,
					"Limit price cannot be null");

		if (OrderType.STPLMT.equals(orderType)
				&& (null == limitPrice || null == auxPrice))
			throw new StrategyRuleException(1, 204,
					"Limit/Aux price cannot be null");
		try {
			if (OrderType.MKT.equals(orderType)) {
				limitPrice = new Money(0);
				auxPrice = new Money(0);
			}

			if (roundPrice) {
				String side = (Action.BUY.equals(action) ? Side.BOT : Side.SLD);
				if (OrderType.LMT.equals(orderType)) {
					if (roundPrice) {
						limitPrice = addPennyAndRoundStop(
								limitPrice.doubleValue(), side, action, 0.01);
					}
				} else if (OrderType.STPLMT.equals(orderType)) {
					Money diffPrice = limitPrice.subtract(auxPrice);
					auxPrice = addPennyAndRoundStop(auxPrice.doubleValue(),
							side, action, 0.01);
					limitPrice = auxPrice.add(diffPrice);
				} else {
					auxPrice = addPennyAndRoundStop(auxPrice.doubleValue(),
							side, action, 0.01);
				}
			}
			TradeOrder tradeOrder = new TradeOrder(this.getTradestrategy(),
					action, this.getOrderCreateDate(), orderType, quantity,
					auxPrice.getBigDecimalValue(),
					limitPrice.getBigDecimalValue(), overrideConstraints,
					timeInForce, triggerMethod);
			tradeOrder.setOcaGroupName(ocaGroupName);
			tradeOrder.setTransmit(transmit);
			if (FAProfile != null) {
				tradeOrder.setFAProfile(FAProfile);
			} else {
				if (FAGroup != null) {
					tradeOrder.setFAGroup(FAGroup);
					tradeOrder.setFAMethod(FAMethod);
					tradeOrder.setFAPercent(FAPercent);
				} else {
					if (null != getTradestrategy().getPortfolio()
							.getIndividualAccount()) {
						tradeOrder.setAccountNumber(getTradestrategy()
								.getPortfolio().getIndividualAccount()
								.getAccountNumber());
					}
				}
			}
			tradeOrder = getBrokerManager().onPlaceOrder(
					getTradestrategy().getContract(), tradeOrder);
			this.getPositionOrders().addTradeOrder(tradeOrder);
			return tradeOrder;
		} catch (BrokerModelException ex) {
			throw new StrategyRuleException(1, 500,
					"Error submitting new tradeOrder to broker : "
							+ ex.getMessage());
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 300,
					"Error create tradeOrder : " + ex.getMessage());
		}
	}

	/**
	 * Method updateOrder.
	 * 
	 * This method creates an open position order for the Trade. The order is
	 * persisted and transmitted via the broker interface to the market.
	 * 
	 * @param orderKey
	 *            Integer
	 * @param action
	 *            String
	 * @param orderType
	 *            String
	 * @param limitPrice
	 *            Money
	 * @param auxPrice
	 *            Money
	 * @param quantity
	 *            int
	 * @param roundPrice
	 *            boolean
	 * @param transmit
	 *            boolean
	 * @return TradeOrder
	 * @throws StrategyRuleException
	 */
	public TradeOrder updateOrder(Integer orderKey, String action,
			String orderType, Money limitPrice, Money auxPrice, int quantity,
			boolean roundPrice, boolean transmit) throws StrategyRuleException {
		try {

			if (null == orderKey)
				throw new StrategyRuleException(1, 200,
						"Order Key cannot be null");

			TradeOrder tradeOrder = tradePersistentModel
					.findTradeOrderByKey(orderKey);

			if (null == action)
				throw new StrategyRuleException(1, 201, "Action cannot be null");

			if (roundPrice) {
				String side = (Action.BUY.equals(action) ? Side.BOT : Side.SLD);
				if (OrderType.LMT.equals(orderType)) {
					if (roundPrice) {
						limitPrice = addPennyAndRoundStop(
								limitPrice.doubleValue(), side, action, 0.01);
					}
				} else if (OrderType.STPLMT.equals(orderType)) {
					Money diffPrice = limitPrice.subtract(auxPrice);
					auxPrice = addPennyAndRoundStop(auxPrice.doubleValue(),
							side, action, 0.01);
					limitPrice = auxPrice.add(diffPrice);
				} else {
					auxPrice = addPennyAndRoundStop(auxPrice.doubleValue(),
							side, action, 0.01);
				}
			}
			tradeOrder.setLastUpdateDate(TradingCalendar.getDate((new Date())
					.getTime()));
			tradeOrder.setLimitPrice(limitPrice.getBigDecimalValue());
			tradeOrder.setAuxPrice(auxPrice.getBigDecimalValue());
			if (quantity > 0)
				tradeOrder.setQuantity(quantity);

			if (null != orderType)
				tradeOrder.setOrderType(orderType);

			tradeOrder.setTransmit(transmit);
			return getBrokerManager().onPlaceOrder(
					getTradestrategy().getContract(), tradeOrder);
		} catch (BrokerModelException ex) {
			throw new StrategyRuleException(1, 510,
					"Error submitting updated tradeOrder to broker: "
							+ ex.getMessage());
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 310,
					"Error update tradeOrder : " + ex.getMessage());
		}
	}

	/**
	 * Method createRiskOpenPosition. This method creates an open position order
	 * for the Trade. The order is persisted and transmitted via the broker
	 * interface to the market.
	 * 
	 * @param action
	 *            String
	 * @param entryPrice
	 *            Money
	 * @param stopPrice
	 *            Money
	 * @param transmit
	 *            boolean
	 * @return TradeOrder
	 * 
	 * @throws StrategyRuleException
	 */
	public TradeOrder createRiskOpenPosition(String action, Money entryPrice,
			Money stopPrice, boolean transmit, String FAProfile,
			String FAGroup, String FAMethod, BigDecimal FAPercent)
			throws StrategyRuleException {

		if (this.isThereOpenPosition())
			throw new StrategyRuleException(1, 205,
					"Cannot create position for TradePosition Id: "
							+ this.getOpenTradePosition().getIdTradePosition()
							+ " as position is already open.");

		if (null == action)
			throw new StrategyRuleException(1, 206, "Action cannot be null");

		try {

			Entrylimit entrylimit = getEntryLimit().getValue(entryPrice);
			String side = (Action.BUY.equals(action) ? Side.BOT : Side.SLD);

			/*
			 * Add/Subtract 1 cent to the entry round the price for 1, 0.5
			 * numbers
			 */
			entryPrice = addPennyAndRoundStop(entryPrice.doubleValue(), side,
					action, 0.01);
			double risk = getTradestrategy().getRiskAmount().doubleValue();

			double stop = entryPrice.doubleValue() - stopPrice.doubleValue();

			// Round to round value
			int quantity = (int) ((int) risk / Math.abs(stop));
			/*
			 * Check to see if we are in the limits of the amount of margin we
			 * can use. If percentOfMargin is null or zero ignore this calc.
			 */
			if (null != entrylimit.getPercentOfMargin()
					&& entrylimit.getPercentOfMargin().doubleValue() > 0) {
				if ((quantity * entryPrice.doubleValue()) > this
						.getIndividualAccount().getBuyingPower()
						.multiply(entrylimit.getPercentOfMargin())
						.doubleValue()) {
					quantity = (int) ((int) this.getIndividualAccount()
							.getBuyingPower().doubleValue()
							* entrylimit.getPercentOfMargin().doubleValue() / entryPrice
							.getBigDecimalValue().doubleValue());
				}
			}

			quantity = (int) ((Math.rint(quantity
					/ entrylimit.getShareRound().doubleValue())) * entrylimit
					.getShareRound().doubleValue());
			if (quantity == 0) {
				quantity = 10;
			}

			Money limitPrice = new Money(
					(Side.BOT.equals(side) ? (entryPrice.doubleValue() + entrylimit
							.getLimitAmount().doubleValue()) : (entryPrice
							.doubleValue() - entrylimit.getLimitAmount()
							.doubleValue())));
			TradeOrder tradeOrder = new TradeOrder(this.getTradestrategy(),
					action, OrderType.STPLMT, quantity,
					entryPrice.getBigDecimalValue(),
					limitPrice.getBigDecimalValue(), this.getOrderCreateDate());

			tradeOrder.setStopPrice(stopPrice.getBigDecimalValue());
			tradeOrder.setTransmit(transmit);
			if (FAProfile != null) {
				tradeOrder.setFAProfile(FAProfile);
			} else {
				if (FAGroup != null) {
					tradeOrder.setFAGroup(FAGroup);
					tradeOrder.setFAMethod(FAMethod);
					tradeOrder.setFAPercent(FAPercent);
				} else {
					if (null != getTradestrategy().getPortfolio()
							.getIndividualAccount()) {
						tradeOrder.setAccountNumber(getTradestrategy()
								.getPortfolio().getIndividualAccount()
								.getAccountNumber());
					}
				}
			}
			tradeOrder = getBrokerManager().onPlaceOrder(
					getTradestrategy().getContract(), tradeOrder);
			this.getPositionOrders().addTradeOrder(tradeOrder);
			return tradeOrder;
		} catch (BrokerModelException ex) {
			throw new StrategyRuleException(1, 520,
					"Error submitting new tradeOrder to broker: "
							+ ex.getMessage());
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 320,
					"Error create risk open tradeOrder : " + ex.getMessage());
		}
	}

	/**
	 * Method cancelOrder. This method cancels the open order position for the
	 * Trade. The order is persisted and transmitted via the broker interface to
	 * the market.
	 * 
	 * @param order
	 *            TradeOrder
	 * @throws StrategyRuleException
	 */
	public void cancelOrder(TradeOrder order) throws StrategyRuleException {
		try {
			if (null != order) {
				if (order.isActive()) {
					getBrokerManager().onCancelOrder(order);
				}
			}
		} catch (BrokerModelException ex) {
			throw new StrategyRuleException(1, 530,
					"Error cancelling tradeOrder to broker: " + ex.getMessage());
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 330,
					"Error create risk open tradeOrder : " + ex.getMessage());
		}
	}

	/**
	 * Method isPositionConvered. This method checks to see if the open order
	 * position for the Trade has a order to cover the position i.e. a
	 * target/stop that covers the total open quantity.
	 * 
	 * @return boolean
	 * @throws StrategyRuleException
	 */
	public boolean isPositionCovered() throws StrategyRuleException {

		try {
			int openQuantity = 0;
			if (this.isThereOpenPosition()) {
				// Find the open position orders
				for (TradeOrder order : this.getPositionOrders()
						.getTradeOrders()) {
					if (order.isActive()) {
						/*
						 * Note that this will give 2X the open amount. But when
						 * an OCA order is filled the cancel tends to happen
						 * before the other side is completely filled.
						 */
						openQuantity = openQuantity + order.getQuantity();
					}
				}
				if (openQuantity >= Math.abs(this.getOpenTradePosition()
						.getOpenQuantity())) {
					return true;
				}
			}
			return false;

		} catch (Exception ex) {
			throw new StrategyRuleException(1, 340,
					"Error StrategyWorker isPositionCovered exception: "
							+ ex.getMessage());
		}
	}

	/**
	 * Method createStopAndTargetOrder. This method creates orders to cover an
	 * open position order for the Trade. The order is persisted and transmitted
	 * via the broker interface to the market. This will create two order OCA
	 * one as a LMT target order and one as a STP stop order.
	 * 
	 * i.e. If we are in a position IBM 1000 shares at $120 then the following
	 * will create two orders. stopPrice = $118 quantity = 1000 numberRiskUnits
	 * = 3 percentQty = 100%
	 * 
	 * IBM LMT $125.99 IBM STP $117.99
	 * 
	 * Note all orders are rounded up/down (around whole/half numbers.) based on
	 * the EntryLimit table.
	 * 
	 * @param stopPrice
	 *            Money
	 * @param targetPrice
	 *            Money
	 * @param quantity
	 *            int
	 * @param stopTransmit
	 *            boolean
	 * @return TradeOrder
	 * @throws StrategyRuleException
	 */
	public TradeOrder createStopAndTargetOrder(Money stopPrice,
			Money targetPrice, int quantity, boolean stopTransmit)
			throws StrategyRuleException {

		if (quantity == 0)
			throw new StrategyRuleException(1, 207, "Quantity cannot be zero");

		if (!this.isThereOpenPosition()) {
			throw new StrategyRuleException(1, 208,
					"Error position is not open");
		}
		try {
			String action = Action.BUY;
			if (Side.BOT.equals(getOpenTradePosition().getSide())) {
				action = Action.SELL;
			}

			String ocaID = new String(Integer.toString((new BigDecimal(Math
					.random() * 1000000)).intValue()));

			TradeOrder orderTarget = new TradeOrder(this.getTradestrategy(),
					action, OrderType.LMT, quantity, null,
					targetPrice.getBigDecimalValue(), this.getOrderCreateDate());

			orderTarget.setOcaType(2);
			orderTarget.setTransmit(true);
			orderTarget.setOcaGroupName(ocaID);

			orderTarget = getBrokerManager().onPlaceOrder(
					getTradestrategy().getContract(), orderTarget);
			this.getPositionOrders().addTradeOrder(orderTarget);
			/*
			 * Note the last order submitted in TWS on OCA order is the only one
			 * that can be updated
			 */

			TradeOrder orderStop = new TradeOrder(this.getTradestrategy(),
					action, OrderType.STP, quantity,
					stopPrice.getBigDecimalValue(), null,
					this.getOrderCreateDate());
			orderStop.setOcaType(2);
			orderStop.setTransmit(stopTransmit);
			orderStop.setOcaGroupName(ocaID);
			if (null != getTradestrategy().getPortfolio()
					.getIndividualAccount()) {
				orderStop.setAccountNumber(getTradestrategy().getPortfolio()
						.getIndividualAccount().getAccountNumber());

			}
			orderStop = getBrokerManager().onPlaceOrder(
					getTradestrategy().getContract(), orderStop);
			this.getPositionOrders().addTradeOrder(orderStop);
			return orderTarget;

		} catch (BrokerModelException ex) {
			throw new StrategyRuleException(1, 540,
					"Error submitting new tradeOrder to broker: "
							+ ex.getMessage());
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 350,
					"Error create stop/target tradeOrder: " + ex.getMessage());
		}
	}

	/**
	 * Method createStopAndTargetOrder. This method creates orders to cover an
	 * open position order for the Trade. The order is persisted and transmitted
	 * via the broker interface to the market. This will create two order OCA
	 * one as a LMT target order and one as a STP stop order.
	 * 
	 * i.e. If we are in a position IBM 1000 shares at $120 then the following
	 * will create two orders. stopPrice = $118 quantity = 1000 numberRiskUnits
	 * = 3 percentQty = 100%
	 * 
	 * IBM LMT $125.99 IBM STP $117.99
	 * 
	 * Note all orders are rounded up/down (around whole/half numbers.) based on
	 * the EntryLimit table.
	 * 
	 * 
	 * @param openPosition
	 *            TradeOrder
	 * @param stopRiskUnits
	 *            int
	 * @param targetRiskUnits
	 *            int
	 * @param percentQty
	 *            int
	 * @param stopTransmit
	 *            boolean
	 * @return TradeOrder
	 * @throws StrategyRuleException
	 */
	public TradeOrder createStopAndTargetOrder(TradeOrder openPosition,
			int stopRiskUnits, int targetRiskUnits, Integer quantity,
			boolean stopTransmit) throws StrategyRuleException {

		if (!this.isThereOpenPosition()) {
			throw new StrategyRuleException(1, 209,
					"Error position is not open");
		}
		try {
			/*
			 * Risk amount is based of the average filled price and actual stop
			 * price not the rounded quantity. But if the stop price is not set
			 * use Risk Amount/Quantity.
			 */
			double riskAmount = 0;
			if (null == openPosition.getStopPrice()) {
				riskAmount = Math.abs(this.getTradestrategy().getRiskAmount()
						.doubleValue()
						/ openPosition.getFilledQuantity().doubleValue());
			} else {
				riskAmount = Math.abs(openPosition.getAverageFilledPrice()
						.doubleValue()
						- openPosition.getStopPrice().doubleValue());
			}

			String action = Action.BUY;
			int buySellMultipliter = 1;
			if (Side.BOT.equals(getOpenTradePosition().getSide())) {
				action = Action.SELL;
				buySellMultipliter = -1;
			}

			// Add a penny to the stop and target
			Money stopPrice = addPennyAndRoundStop(openPosition
					.getAverageFilledPrice().doubleValue()
					+ (riskAmount * stopRiskUnits * buySellMultipliter), this
					.getOpenTradePosition().getSide(), action, 0.01);

			Money targetPrice = addPennyAndRoundStop(openPosition
					.getAverageFilledPrice().doubleValue()
					+ (riskAmount * targetRiskUnits * buySellMultipliter * -1),
					this.getOpenTradePosition().getSide(), action, 0.01);

			String ocaID = new String(Integer.toString((new BigDecimal(Math
					.random() * 1000000)).intValue()));

			TradeOrder orderTarget = new TradeOrder(this.getTradestrategy(),
					action, OrderType.LMT, quantity, null,
					targetPrice.getBigDecimalValue(), this.getOrderCreateDate());

			orderTarget.setOcaType(2);
			orderTarget.setTransmit(true);
			orderTarget.setOcaGroupName(ocaID);
			orderTarget.setAccountNumber(openPosition.getAccountNumber());
			orderTarget.setFAGroup(openPosition.getFAGroup());
			orderTarget.setFAProfile(openPosition.getFAProfile());
			orderTarget.setFAMethod(openPosition.getFAMethod());
			orderTarget.setFAPercent(openPosition.getFAPercent());
			orderTarget = getBrokerManager().onPlaceOrder(
					getTradestrategy().getContract(), orderTarget);
			this.getPositionOrders().addTradeOrder(orderTarget);
			/*
			 * Note the last order submitted in TWS on OCA order is the only one
			 * that can be updated
			 */

			TradeOrder orderStop = new TradeOrder(this.getTradestrategy(),
					action, OrderType.STP, quantity,
					stopPrice.getBigDecimalValue(), null,
					this.getOrderCreateDate());
			orderStop.setOcaType(2);
			orderStop.setTransmit(stopTransmit);
			orderStop.setOcaGroupName(ocaID);
			orderStop.setAccountNumber(openPosition.getAccountNumber());
			orderStop.setFAGroup(openPosition.getFAGroup());
			orderStop.setFAProfile(openPosition.getFAProfile());
			orderStop.setFAMethod(openPosition.getFAMethod());
			orderStop.setFAPercent(openPosition.getFAPercent());
			orderStop = getBrokerManager().onPlaceOrder(
					getTradestrategy().getContract(), orderStop);
			this.getPositionOrders().addTradeOrder(orderStop);
			return orderTarget;
		} catch (BrokerModelException ex) {
			throw new StrategyRuleException(1, 550,
					"Error submitting new tradeOrder to broker: "
							+ ex.getMessage());
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 360,
					"Error create stop/target tradeOrder: " + ex.getMessage());
		}
	}

	/**
	 * Method getStopPriceForPositionRisk. This method will calculate the Stop
	 * price based on the number of risk units and the risk amount.
	 * 
	 * @param openPosition
	 *            TradeOrder
	 * @param numberRiskUnits
	 *            int
	 * @return Money
	 * @throws StrategyRuleException
	 */
	public Money getStopPriceForPositionRisk(TradeOrder openPosition,
			int numberRiskUnits) throws StrategyRuleException {
		try {
			double riskAmount = (this.getTradestrategy().getRiskAmount()
					.doubleValue() / this.getOpenTradePosition()
					.getOpenQuantity()) * numberRiskUnits;

			if (Side.BOT.equals(this.getOpenTradePosition().getSide())) {
				riskAmount = riskAmount * -1;
			}

			// Add a penny to the stop
			Money stopPrice = new Money(openPosition.getAverageFilledPrice()
					.doubleValue() + riskAmount);
			return stopPrice;

		} catch (Exception ex) {
			throw new StrategyRuleException(1, 370,
					"Error getting stop price for risk position: "
							+ ex.getMessage());
		}
	}

	/**
	 * Method cancelOrdersClosePosition. This method will close a position by
	 * canceling all unfilled orders and creating a market order to close the
	 * position.
	 * 
	 * @param transmit
	 *            boolean
	 * 
	 * @throws StrategyRuleException
	 */
	public TradeOrder cancelOrdersClosePosition(boolean transmit)
			throws StrategyRuleException {

		_log.info("Strategy  closeOpenPosition symbol: " + symbol);
		try {
			cancelAllOrders();
			if (this.isThereOpenPosition()) {
				return closePosition(transmit);
			}
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 380,
					"Error StrategyWorker exception: " + ex.getMessage());
		}
		return null;
	}

	/**
	 * Method moveStopOCAPrice. This method will the stop order for a trade to
	 * the new values..
	 * 
	 * @param stopPrice
	 *            Money
	 * @param stopTransmit
	 *            boolean
	 * @throws StrategyRuleException
	 */
	public void moveStopOCAPrice(Money stopPrice, boolean transmit)
			throws StrategyRuleException {

		_log.info("Strategy  moveStopOCAPrice symbol: " + symbol
				+ " Stop Price: " + stopPrice);
		try {
			if (this.isThereOpenPosition()) {
				// Cancel the tgt and stop orders i.e. OCA
				for (TradeOrder tradeOrder : this.getPositionOrders()
						.getTradeOrders()) {
					if (!tradeOrder.getIsOpenPosition()
							&& tradeOrder.isActive()) {
						if (OrderType.STP.equals(tradeOrder.getOrderType())
								&& null != tradeOrder.getOcaGroupName()) {
							tradeOrder.setLastUpdateDate(TradingCalendar
									.getDate((new Date()).getTime()));
							tradeOrder.setAuxPrice(stopPrice
									.getBigDecimalValue());
							tradeOrder.setTransmit(transmit);
							tradeOrder = getBrokerManager().onPlaceOrder(
									getTradestrategy().getContract(),
									tradeOrder);
						}
					}
				}
			}
		} catch (BrokerModelException ex) {
			throw new StrategyRuleException(1, 560,
					"Error updating tradeOrder to broker: " + ex.getMessage());
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 390,
					"Error StrategyWorker moveStopOCAPrice exception Symbol: "
							+ this.symbol + " Stop Price: " + stopPrice
							+ " Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method cancelAllOrders. This method will all orders for a trade position.
	 * 
	 * @throws StrategyRuleException
	 */
	public void cancelAllOrders() throws StrategyRuleException {
		_log.info("Strategy  cancelAllOrders symbol: " + symbol);
		for (TradeOrder order : this.getPositionOrders().getTradeOrders()) {
			cancelOrder(order);
		}
	}

	/**
	 * Method addPennyAndRoundStop. This method takes a price and adds/subtracts
	 * pennies to that prices and rounds the results based on whole/half number.
	 * 
	 * @param price
	 *            double
	 * @param side
	 *            String
	 * @param action
	 *            String
	 * @param dollars
	 *            double
	 * @return Money
	 * @throws StrategyRuleException
	 */
	public Money addPennyAndRoundStop(double price, String side, String action,
			double dollars) throws StrategyRuleException {
		double roundPrice = 0;
		if (Side.BOT.equals(side)) {
			roundPrice = roundPrice(price + dollars, action);
		} else {
			roundPrice = roundPrice(price - dollars, action);
		}
		return new Money(roundPrice);
	}

	/**
	 * Method getCurrentCandleCount.
	 * 
	 * @return int
	 */
	public int getCurrentCandleCount() {
		return currentCandleCount;
	}

	/**
	 * Method getCurrentCandle.
	 * 
	 * @return CandleItem
	 */
	public CandleItem getCurrentCandle() {
		CandleItem currentCandleItem = null;
		if (getCurrentCandleCount() > -1) {
			CandleSeries candleSeries = this.getTradestrategy()
					.getDatasetContainer().getBaseCandleSeries();
			currentCandleItem = (CandleItem) candleSeries
					.getDataItem(getCurrentCandleCount());
		}
		return currentCandleItem;
	}

	/**
	 * Method getCandle.
	 * 
	 * @param startPeriod
	 *            Date
	 * @return CandleItem
	 * @throws StrategyRuleException
	 */
	public CandleItem getCandle(Date startPeriod) throws StrategyRuleException {
		CandleItem candle = null;
		CandleSeries baseCandleSeries = getTradestrategy()
				.getDatasetContainer().getBaseCandleSeries();
		CandlePeriod period = new CandlePeriod(startPeriod,
				baseCandleSeries.getBarSize());
		int index = baseCandleSeries.indexOf(period);
		if (index > -1) {
			candle = (CandleItem) baseCandleSeries.getDataItem(index);
		} else {
			throw new StrategyRuleException(1, 210,
					"Error Candle not found for period: " + period
							+ " in baseCandleSeries barSize: "
							+ baseCandleSeries.getBarSize() + " series count: "
							+ baseCandleSeries.getItemCount()
							+ " StartPeriod: " + startPeriod);
		}
		return candle;
	}

	/**
	 * Method updateTradestrategyStatus.
	 * 
	 * @param status
	 *            String
	 * @throws StrategyRuleException
	 */
	public void updateTradestrategyStatus(String status)
			throws StrategyRuleException {
		try {
			this.getPositionOrders().setStatus(status);
			this.getPositionOrders().setLastUpdateDate(
					TradingCalendar.getDate((new Date()).getTime()));
			this.tradePersistentModel.persistAspect(this.getPositionOrders());
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 400,
					"Error updating tradestrategy status: " + ex.getMessage());
		}
	}

	/**
	 * Method getBrokerManager.
	 * 
	 * @return BrokerModel
	 */
	private BrokerModel getBrokerManager() {
		return this.brokerModel;
	}

	/**
	 * Method getEntryLimit.
	 * 
	 * @return DAOEntryLimit
	 */
	public DAOEntryLimit getEntryLimit() {
		return this.entryLimits;
	}

	/**
	 * Method getTradestrategy.
	 * 
	 * @return Tradestrategy
	 */
	public Tradestrategy getTradestrategy() {
		return this.tradestrategy;
	}

	/**
	 * Method getPositionOrders.
	 * 
	 * @return PositionOrders
	 */
	public PositionOrders getPositionOrders() {
		return this.positionOrders;
	}

	/**
	 * Method getStrategyLastFired.
	 * 
	 * @return Date
	 */
	public Date getStrategyLastFired() {
		return strategyLastFired;
	}

	/**
	 * Method reFreshPositionOrders.
	 * 
	 * @throws StrategyRuleException
	 */

	public void reFreshPositionOrders() throws StrategyRuleException {
		try {
			this.positionOrders = this.tradePersistentModel
					.findPositionOrdersByTradestrategyId(this.idTradestrategy);
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 410, "Error position orders: "
					+ ex.getMessage());
		}
	}

	/**
	 * Method getIndividualAccount. Return a refreshed trade account note this
	 * is updated when connected to TWS every time the account values change.
	 * 
	 * @return Account
	 * @throws StrategyRuleException
	 */
	public Account getIndividualAccount() throws StrategyRuleException {
		try {
			return this.tradePersistentModel
					.findAccountByNumber(getTradestrategy().getPortfolio()
							.getIndividualAccount().getAccountNumber());
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 420,
					"Error finding individual accounts: " + ex.getMessage());
		}
	}

	/**
	 * Method hasActiveOrders.
	 * 
	 * @return boolean
	 */

	public boolean hasActiveOrders() {
		for (TradeOrder tradeOrder : getPositionOrders().getTradeOrders()) {
			if (tradeOrder.isActive())
				return true;
		}
		return false;
	}

	/**
	 * Method isThereOpenPosition.
	 * 
	 * @return boolean
	 */
	public boolean isThereOpenPosition() {
		if (null != getOpenTradePosition()) {
			return true;
		}
		return false;
	}

	/**
	 * Method getOpenTradePosition.
	 * 
	 * @return TradePosition
	 */
	public TradePosition getOpenTradePosition() {
		return getPositionOrders().getOpenTradePosition();
	}

	/**
	 * Method getSymbol.
	 * 
	 * @return String
	 */
	public String getSymbol() {
		return this.symbol;
	}

	/**
	 * Method getOpenPositionOrder. The open position order is the first order
	 * that opened the trade position.
	 * <p>
	 * 
	 * @return TradeOrder
	 * 
	 */
	public TradeOrder getOpenPositionOrder() {
		for (TradeOrder tradeOrder : this.getPositionOrders().getTradeOrders()) {
			if (tradeOrder.getIsOpenPosition())
				return tradeOrder;
		}
		return null;
	}

	/**
	 * Method getStopPriceMinUnfilled.
	 * 
	 * @return Money
	 */
	public Money getStopPriceMinUnfilled() {
		double stopPrice = Double.MAX_VALUE;
		for (TradeOrder tradeOrder : this.getPositionOrders().getTradeOrders()) {
			if (tradeOrder.isActive()
					&& OrderType.STP.equals(tradeOrder.getOrderType())) {
				stopPrice = Math.min(stopPrice, tradeOrder.getAuxPrice()
						.doubleValue());
			}
		}
		if (stopPrice < Double.MAX_VALUE)
			return new Money(stopPrice);

		return null;
	}

	/**
	 * Method getStopPriceMinUnfilled.
	 * 
	 * @return Money
	 */
	public Money getTargetPriceMinUnfilled() {
		double stopPrice = Double.MAX_VALUE;
		for (TradeOrder tradeOrder : this.getPositionOrders().getTradeOrders()) {
			if (tradeOrder.isActive()
					&& OrderType.STP.equals(tradeOrder.getOrderType())) {
				stopPrice = Math.min(stopPrice, tradeOrder.getAuxPrice()
						.doubleValue());
			}
		}
		if (stopPrice < Double.MAX_VALUE)
			return new Money(stopPrice);

		return null;
	}

	/**
	 * Method getTradeOrder.
	 * 
	 * @param orderKey
	 *            Integer
	 * @return TradeOrder
	 */
	public TradeOrder getTradeOrder(Integer orderKey) {
		for (TradeOrder order : this.getPositionOrders().getTradeOrders()) {
			if (order.getOrderKey().equals(orderKey)) {
				return order;
			}
		}
		return null;
	}

	/**
	 * Method isDuringTradingday.
	 * 
	 * @param dateTime
	 *            Date
	 * @return boolean
	 */
	public boolean isDuringTradingday(Date dateTime) {
		if (TradingCalendar.isMarketHours(getTradestrategy().getTradingday()
				.getOpen(), getTradestrategy().getTradingday().getClose(),
				dateTime)
				&& TradingCalendar.sameDay(getTradestrategy().getTradingday()
						.getOpen(), dateTime)) {
			return true;
		}
		return false;
	}

	/**
	 * Method tradeOrderFilled.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 */
	public void tradeOrderFilled(TradeOrder tradeOrder) {
	}

	/**
	 * Method logCandle.
	 * 
	 * @param candle
	 *            Candle
	 */
	public static void logCandle(AbstractStrategyRule context, Candle candle) {
		_log.info(context.getClass().getSimpleName() + " Symbol: "
				+ candle.getContract().getSymbol() + " startPeriod: "
				+ candle.getStartPeriod() + " endPeriod: "
				+ candle.getEndPeriod() + " Open: "
				+ new Money(candle.getOpen()) + " High: "
				+ new Money(candle.getHigh()) + " Low: "
				+ new Money(candle.getLow()) + " Close: "
				+ new Money(candle.getClose()) + " Volume: "
				+ new Money(candle.getVolume()) + " Vwap: "
				+ new Money(candle.getVwap()) + " TradeCount: "
				+ new Money(candle.getTradeCount()) + " LastUpdate: "
				+ candle.getLastUpdateDate());
	}

	/**
	 * Method roundPrice.
	 * 
	 * @param price
	 *            double
	 * @param action
	 *            String
	 * @return double
	 * @throws StrategyRuleException
	 */
	private double roundPrice(double price, String action)
			throws StrategyRuleException {
		try {
			// Round at whole and half numbers add to this if you
			// need others.
			Entrylimit entrylimit = getEntryLimit().getValue(new Money(price));
			if (null == entrylimit) {
				throw new StrategyRuleException(1, 211,
						"No EntryLimits found for price: " + price);
			}

			double[] rounding = { 1, 0.5 };
			int buySellMultiplier = 1;

			if (action.equals(Action.SELL)) {
				buySellMultiplier = -1;
			}

			for (double element : rounding) {
				// Round the price to over under half numbers
				double wholePrice = price + (1 - element);
				double remainder = ((Math.rint(wholePrice) - wholePrice) * buySellMultiplier);
				if ((remainder < entrylimit.getPriceRound().doubleValue())
						&& (remainder >= 0)) {
					price = (Math.rint(wholePrice) + (0.01d * buySellMultiplier))
							- (1 - element);
					return price;
				}
			}
			return price;
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 420, "Error rounding price: "
					+ ex.getMessage());
		}
	}

	/**
	 * Method getOrderCreateDate. If realtimebars running use current time
	 * otherwise we are testing use candle lastUpdateDate.
	 * 
	 * @return Date
	 * @throws StrategyRuleException
	 */
	private Date getOrderCreateDate() {
		Date createDate = new Date();
		if (!brokerModel.isRealtimeBarsRunning(this.tradestrategy)) {
			if (null != this.getCurrentCandle())
				createDate = this.getCurrentCandle().getPeriod().getStart();
		}
		return createDate;
	}
}
