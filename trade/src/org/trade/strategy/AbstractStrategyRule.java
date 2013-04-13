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
import org.trade.core.util.CoreUtils;
import org.trade.core.util.TradingCalendar;
import org.trade.core.util.Worker;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.DAOEntryLimit;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.dictionary.valuetype.OverrideConstraints;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.TimeInForce;
import org.trade.dictionary.valuetype.TriggerMethod;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Entrylimit;
import org.trade.persistent.dao.Trade;
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
	private Trade trade = null;
	private Integer idTradestrategy = null;
	private String symbol = null;
	private Money targetPrice = null;
	private boolean seriesChanged = false;
	private final Object lockStrategyWorker = new Object();
	private boolean listeningCandles = false;
	private int currentCandleCount = -1;

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
	protected void fireStrategyStarted(Tradestrategy tradestrategy) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == StrategyChangeListener.class) {
				((StrategyChangeListener) listeners[i + 1])
						.strategyStarted(tradestrategy);
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
	 * Notifies all registered listeners that the strategyRule position has been
	 * covered.
	 * 
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see #addChangeListener(StrategyChangeListener)
	 */
	protected void firePositionCovered(Tradestrategy tradestrategy) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == StrategyChangeListener.class) {
				((StrategyChangeListener) listeners[i + 1])
						.positionCovered(tradestrategy);
			}
		}
	}

	/*
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

			/*
			 * Process the current candle if there is one on startup.
			 */

			currentCandleCount = this.datasetContainer.getBaseCandleSeries()
					.getItemCount() - 1;

			seriesChanged = true;

			_log.info(this.getClass().getName()
					+ " engine doInBackground Symbol: " + this.symbol
					+ " idTradestrategy: " + this.idTradestrategy);
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
					 * Refresh the orders in the tradestrategy as these may have
					 * been filled via another thread
					 */

					if (null == this.getTrade()) {
						this.trade = this.tradePersistentModel
								.findOpenTradeByTradestrategyId(this.tradestrategy
										.getIdTradeStrategy());
					} else {
						this.trade = this.tradePersistentModel
								.findTradeById(this.trade.getIdTrade());
					}
					CandleSeries candleSeries = this.tradestrategy
							.getDatasetContainer().getBaseCandleSeries();

					if ((candleSeries.getItemCount() - 1) > currentCandleCount) {

						/*
						 * We have a new candle. Note currentCandleCount starts
						 * at -1
						 */
						currentCandleCount = (candleSeries.getItemCount() - 1);
						/*
						 * Only manage trades when the market is open and the
						 * candle is for the Tradestrategies trading day.
						 */
						if (this.isDuringTradingday(getCurrentCandle()
								.getPeriod().getStart()))
							runStrategy(candleSeries, true);

					} else if (currentCandleCount == (candleSeries
							.getItemCount() - 1)) {
						/*
						 * We have an updated candle. If this strategy listens
						 * for updates fire the rule.
						 */
						if (currentCandleCount > -1) {
							// Fire rules
							if (this.isDuringTradingday(getCurrentCandle()
									.getPeriod().getStart()))
								runStrategy(candleSeries, false);
						}
					} else if (currentCandleCount < (candleSeries
							.getItemCount() - 1)) {
						/*
						 * The currentCandleCount is less than the candle
						 * series. Then another thread must have cleared the
						 * candle series so shut down the strategy.
						 */
						_log.info("Cancelled due to candleSeries clear Symbol: "
								+ getSymbol()
								+ " class: "
								+ this.getClass().getName());
						this.cancel();
						break;
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
						if (null != this.trade) {
							if (isPositionCovered()) {
								this.firePositionCovered(this.tradestrategy);
							}
						}
						this.fireStrategyStarted(this.tradestrategy);
						listeningCandles = true;
					} else {
						if (null != this.trade) {
							if (isPositionCovered()) {
								this.firePositionCovered(this.tradestrategy);
							}
						}
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
			error(1, 50, "Error StrategyWorker exception: " + ex.getMessage());
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
	 * Method runStrategy. This method is called every time the candledataset is
	 * either updated or a candle is added.
	 * 
	 * @param candleSeries
	 *            CandleSeries
	 * @param newBar
	 *            boolean
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
	 * @throws ValueTypeException
	 * @throws BrokerModelException
	 * @throws PersistentModelException
	 */
	public void closePosition(boolean transmit) throws ValueTypeException,
			BrokerModelException, PersistentModelException {

		if (this.isPositionOpen()) {

			int cumQuantityOpen = Math.abs(this.getTrade().getOpenQuantity());

			if (cumQuantityOpen > 0) {
				Date createDate = new Date();

				String action = Action.BUY;
				if (Side.BOT.equals(this.getTrade().getSide())) {
					action = Action.SELL;
				}
				TradeOrder tradeOrder = new TradeOrder(this.getTrade(), action,
						OrderType.MKT, cumQuantityOpen, null, null, createDate);

				tradeOrder.setIsOpenPosition(false);
				tradeOrder.setTransmit(transmit);
				tradeOrder.setStatus(OrderStatus.UNSUBMIT);
				/*
				 * If the portfolio has an individual account use the account
				 * number. If the portfolio has an allocation method and that
				 * allocation method is an integer then assume its a profile
				 * otherwise it must be a group. Note this only applied for TWS
				 * Broker. TODO figure out a better implementation.
				 */
				if (null != getTradestrategy().getPortfolio()
						.getIndividualAccount()) {
					tradeOrder.setAccountNumber(getTradestrategy()
							.getPortfolio().getIndividualAccount()
							.getAccountNumber());
				} else {
					if (null != this.getTradestrategy().getPortfolio()
							.getAllocationMethod()) {
						if (CoreUtils.isNumeric(this.getTradestrategy()
								.getPortfolio().getAllocationMethod())) {
							tradeOrder.setFAProfile(this.getTradestrategy()
									.getPortfolio().getName());
						} else {
							tradeOrder.setFAGroup(this.getTradestrategy()
									.getPortfolio().getName());
						}
					}
				}
				tradeOrder = getBrokerManager().onPlaceOrder(
						getTradestrategy().getContract(), tradeOrder);
			}
		}
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
	 * @throws ValueTypeException
	 * @throws BrokerModelException
	 * @throws PersistentModelException
	 * @throws StrategyRuleException
	 */
	public TradeOrder createOrder(String action, String orderType,
			Money limitPrice, Money auxPrice, int quantity,
			String ocaGroupName, boolean roundPrice, boolean transmit)
			throws ValueTypeException, BrokerModelException,
			PersistentModelException, StrategyRuleException {
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
	 * @throws ValueTypeException
	 * @throws BrokerModelException
	 * @throws PersistentModelException
	 * @throws StrategyRuleException
	 */
	public TradeOrder createOrder(String action, String orderType,
			Money limitPrice, Money auxPrice, int quantity,
			String ocaGroupName, int triggerMethod, int overrideConstraints,
			String timeInForce, boolean roundPrice, boolean transmit,
			String FAProfile, String FAGroup, String FAMethod,
			BigDecimal FAPercent) throws ValueTypeException,
			BrokerModelException, PersistentModelException {
		/*
		 * If no trade exists create the trade and set the side based on the
		 * action.
		 */
		if (!isPositionOpen()) {
			/*
			 * Set the side based on the action for the first order.
			 */
			Trade openTrade = tradePersistentModel
					.findOpenTradeByContractId(getTradestrategy().getContract()
							.getIdContract());

			if (null == openTrade) {
				this.trade = new Trade(getTradestrategy(),
						(Action.BUY.equals(action) ? Side.BOT : Side.SLD));
				getTradestrategy().addTrade(this.trade);
			} else {

				/*
				 * TODO Need to update schema to hang Trade of Contract not
				 * Tradestrategy then link the trade to Tradingday with the
				 * relationship closed on
				 */

				_log.warn("A position is already open Trade Id: "
						+ openTrade.getIdTrade() + " isOpen: "
						+ openTrade.getIsOpen() + " totalQty: "
						+ openTrade.getTotalQuantity()
						+ " this will be used to trade.");

				openTrade.setTradestrategy(getTradestrategy());
				this.trade = this.tradePersistentModel.persistTrade(openTrade);
				this.trade = this.tradePersistentModel.findTradeById(this.trade
						.getIdTrade());
				getTradestrategy().addTrade(this.trade);
			}
		}

		if (roundPrice) {
			if (OrderType.LMT.equals(orderType)) {
				if (roundPrice) {
					limitPrice = addPennyAndRoundStop(limitPrice.doubleValue(),
							this.getTrade().getSide(), action, 0.01);
				}
			} else if (OrderType.STPLMT.equals(orderType)) {
				Money diffPrice = limitPrice.subtract(auxPrice);
				auxPrice = addPennyAndRoundStop(auxPrice.doubleValue(), this
						.getTrade().getSide(), action, 0.01);
				limitPrice = diffPrice.isNegative() ? auxPrice
						.subtract(diffPrice) : auxPrice.add(diffPrice);
			} else {
				auxPrice = addPennyAndRoundStop(auxPrice.doubleValue(), this
						.getTrade().getSide(), action, 0.01);
			}
		}
		TradeOrder tradeOrder = new TradeOrder(this.getTrade(), action,
				new Date(), orderType, quantity, auxPrice.getBigDecimalValue(),
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
		this.getTrade().addTradeOrder(tradeOrder);
		return tradeOrder;
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
	 * @throws ValueTypeException
	 * @throws BrokerModelException
	 * @throws PersistentModelException
	 */
	public TradeOrder updateOrder(Integer orderKey, String action,
			String orderType, Money limitPrice, Money auxPrice, int quantity,
			boolean roundPrice, boolean transmit) throws ValueTypeException,
			BrokerModelException, PersistentModelException {

		TradeOrder tradeOrder = tradePersistentModel
				.findTradeOrderByKey(orderKey);
		if (roundPrice) {
			if (OrderType.LMT.equals(orderType)) {
				if (roundPrice) {
					limitPrice = addPennyAndRoundStop(limitPrice.doubleValue(),
							this.getTrade().getSide(), action, 0.01);
				}
			} else if (OrderType.STPLMT.equals(orderType)) {
				Money diffPrice = limitPrice.subtract(auxPrice);
				auxPrice = addPennyAndRoundStop(auxPrice.doubleValue(), this
						.getTrade().getSide(), action, 0.01);
				limitPrice = diffPrice.isNegative() ? auxPrice
						.subtract(diffPrice) : auxPrice.add(diffPrice);
			} else {
				auxPrice = addPennyAndRoundStop(auxPrice.doubleValue(), this
						.getTrade().getSide(), action, 0.01);
			}
		}
		tradeOrder.setLimitPrice(limitPrice.getBigDecimalValue());
		tradeOrder.setAuxPrice(auxPrice.getBigDecimalValue());
		tradeOrder.setTransmit(transmit);
		tradeOrder = getBrokerManager().onPlaceOrder(
				getTradestrategy().getContract(), tradeOrder);
		return tradeOrder;
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
	 * @throws ValueTypeException
	 * @throws BrokerModelException
	 * @throws PersistentModelException
	 * @throws StrategyRuleException
	 */
	public TradeOrder createRiskOpenPosition(String action, Money entryPrice,
			Money stopPrice, boolean transmit, String FAProfile,
			String FAGroup, String FAMethod, BigDecimal FAPercent)
			throws ValueTypeException, BrokerModelException,
			PersistentModelException {

		if (!isPositionOpen()) {
			/*
			 * Set the side based on the action for the first order.
			 */
			Trade openTrade = tradePersistentModel
					.findOpenTradeByContractId(getTradestrategy().getContract()
							.getIdContract());

			if (null == openTrade) {
				this.trade = new Trade(getTradestrategy(),
						(Action.BUY.equals(action) ? Side.BOT : Side.SLD));
				getTradestrategy().addTrade(this.trade);
			} else {

				/*
				 * TODO Need to update schema to hang Trade of Contract not
				 * Tradestrategy then link the trade to Tradingday with the
				 * relationship closed on
				 */

				_log.warn("A position is already open Trade Id: "
						+ openTrade.getIdTrade() + " isOpen: "
						+ openTrade.getIsOpen() + " totalQty: "
						+ openTrade.getTotalQuantity()
						+ " this will be used to trade.");

				openTrade.setTradestrategy(getTradestrategy());
				this.trade = this.tradePersistentModel.persistTrade(openTrade);
				this.trade = this.tradePersistentModel.findTradeById(this.trade
						.getIdTrade());
				getTradestrategy().addTrade(this.trade);
			}
		}

		if (!this.getTrade().getTradeOrders().isEmpty())
			throw new BrokerModelException(1, 51,
					"Cannot create open position for Trade Id: "
							+ this.getTrade().getIdTrade()
							+ " as trade alreads has orders.");

		if (this.getTrade().getIsOpen())
			throw new BrokerModelException(1, 52,
					"Cannot create open position for Trade Id: "
							+ this.getTrade().getIdTrade()
							+ " as trade is already open.");

		Date createDate = new Date();
		Entrylimit entrylimit = getEntryLimit().getValue(entryPrice);

		/*
		 * Add/Subtract 1 cent to the entry round the price for 1, 0.5 numbers
		 */
		entryPrice = addPennyAndRoundStop(entryPrice.doubleValue(), this
				.getTrade().getSide(), action, 0.01);
		double risk = getTradestrategy().getRiskAmount().doubleValue();

		double stop = entryPrice.doubleValue() - stopPrice.doubleValue();

		// Round to round value
		int quantity = (int) ((int) risk / Math.abs(stop));
		/*
		 * Check to see if we are in the limits of the amount of margin we can
		 * use. If percentOfMargin is null or zero ignore this calc.
		 */
		if (null != entrylimit.getPercentOfMargin()
				&& entrylimit.getPercentOfMargin().doubleValue() > 0) {
			if ((quantity * entryPrice.doubleValue()) > this
					.getIndividualAccount().getBuyingPower()
					.multiply(entrylimit.getPercentOfMargin()).doubleValue()) {
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
				(Side.BOT.equals(this.getTrade().getSide()) ? (entryPrice
						.doubleValue() + entrylimit.getLimitAmount()
						.doubleValue())
						: (entryPrice.doubleValue() - entrylimit
								.getLimitAmount().doubleValue())));
		TradeOrder tradeOrder = new TradeOrder(this.getTrade(), action,
				OrderType.STPLMT, quantity, entryPrice.getBigDecimalValue(),
				limitPrice.getBigDecimalValue(), createDate);

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
		this.getTrade().addTradeOrder(tradeOrder);
		return tradeOrder;
	}

	/**
	 * Method cancelOrder. This method cancels the open order position for the
	 * Trade. The order is persisted and transmitted via the broker interface to
	 * the market.
	 * 
	 * @param order
	 *            TradeOrder
	 * @throws BrokerModelException
	 */
	public void cancelOrder(TradeOrder order) throws BrokerModelException {
		if (null != order) {
			if (!order.getIsFilled()
					&& !OrderStatus.CANCELLED.equals(order.getStatus())
					&& !OrderStatus.INACTIVE.equals(order.getStatus())
					&& !OrderStatus.UNSUBMIT.equals(order.getStatus())) {
				getBrokerManager().onCancelOrder(order);
			}
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
			if (this.isPositionOpen()) {
				// Find the open position orders
				for (TradeOrder order : this.getTrade().getTradeOrders()) {
					if (!order.getIsOpenPosition() && !order.getIsFilled()
							&& !OrderStatus.CANCELLED.equals(order.getStatus())
							&& !OrderStatus.INACTIVE.equals(order.getStatus())) {
						/*
						 * Note that this will give 2X the open amount. But when
						 * an OCA order is filled the cancel tends to happen
						 * before the other side is completely filled.
						 */
						openQuantity = openQuantity + order.getQuantity();
					}
				}
				if (openQuantity >= Math.abs(this.getTrade().getOpenQuantity())) {
					return true;
				}
			}
			return false;

		} catch (Exception ex) {
			throw new StrategyRuleException(1, 60,
					"Error StrategyWorker exception: " + ex.getMessage());
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
	 * @return Money
	 * @throws ValueTypeException
	 * @throws BrokerModelException
	 * @throws PersistentModelException
	 * @throws StrategyRuleException
	 */
	public Money createStopAndTargetOrder(Money stopPrice, Money targetPrice,
			int quantity, boolean stopTransmit) throws ValueTypeException,
			BrokerModelException, PersistentModelException,
			StrategyRuleException {

		Date createDate = new Date();

		if (!this.isPositionOpen()) {
			throw new StrategyRuleException(1, 80, "Error trade is not open");
		}

		String action = Action.BUY;
		if (Side.BOT.equals(trade.getSide())) {
			action = Action.SELL;
		}

		String ocaID = new String(Integer.toString((new BigDecimal(Math
				.random() * 1000000)).intValue()));

		TradeOrder orderTarget = new TradeOrder(this.getTrade(), action,
				OrderType.LMT, quantity, null,
				targetPrice.getBigDecimalValue(), createDate);

		orderTarget.setOcaType(2);
		orderTarget.setTransmit(true);
		orderTarget.setOcaGroupName(ocaID);
		if (null != getTradestrategy().getPortfolio().getIndividualAccount()) {
			orderTarget.setAccountNumber(getTradestrategy().getPortfolio()
					.getIndividualAccount().getAccountNumber());

		} else {
			// TODO for AccountType.CORPORATE accounts provide the group/proflie
		}
		orderTarget = getBrokerManager().onPlaceOrder(
				getTradestrategy().getContract(), orderTarget);
		this.getTrade().addTradeOrder(orderTarget);

		/*
		 * Note the last order submitted in TWS on OCA order is the only one
		 * that can be updated
		 */

		TradeOrder orderStop = new TradeOrder(this.getTrade(), action,
				OrderType.STP, quantity, stopPrice.getBigDecimalValue(), null,
				createDate);
		orderStop.setOcaType(2);
		orderStop.setTransmit(stopTransmit);
		orderStop.setOcaGroupName(ocaID);
		if (null != getTradestrategy().getPortfolio().getIndividualAccount()) {
			orderStop.setAccountNumber(getTradestrategy().getPortfolio()
					.getIndividualAccount().getAccountNumber());

		} else {
			// TODO for AccountType.CORPORATE accounts provide the group/proflie
		}
		orderStop = getBrokerManager().onPlaceOrder(
				getTradestrategy().getContract(), orderStop);
		this.getTrade().addTradeOrder(orderStop);
		return targetPrice;
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
	 * @return Money
	 * @throws ValueTypeException
	 * @throws BrokerModelException
	 * @throws PersistentModelException
	 * @throws StrategyRuleException
	 */
	public Money createStopAndTargetOrder(TradeOrder openPosition,
			int stopRiskUnits, int targetRiskUnits, int percentQty,
			boolean stopTransmit) throws ValueTypeException,
			BrokerModelException, PersistentModelException,
			StrategyRuleException {

		Date createDate = new Date();

		if (!this.isPositionOpen()) {
			throw new StrategyRuleException(1, 80, "Error trade is not open");
		}

		/*
		 * Risk amount is based of the average filled price and actual stop
		 * price not the rounded quantity. But if the stop price is not set use
		 * Risk Amount/Quantity.
		 */
		double riskAmount = 0;
		if (null == openPosition.getStopPrice()) {
			riskAmount = Math.abs(this.getTradestrategy().getRiskAmount()
					.doubleValue()
					/ openPosition.getFilledQuantity().doubleValue());
		} else {
			riskAmount = Math.abs(openPosition.getAverageFilledPrice()
					.doubleValue() - openPosition.getStopPrice().doubleValue());
		}

		String action = Action.BUY;
		int buySellMultipliter = 1;
		if (Side.BOT.equals(trade.getSide())) {
			action = Action.SELL;
			buySellMultipliter = -1;
		}

		// Add a penny to the stop and target
		Money stopPrice = addPennyAndRoundStop(openPosition
				.getAverageFilledPrice().doubleValue()
				+ (riskAmount * stopRiskUnits * buySellMultipliter), this
				.getTrade().getSide(), action, 0.01);

		Money targetPrice = addPennyAndRoundStop(openPosition
				.getAverageFilledPrice().doubleValue()
				+ (riskAmount * targetRiskUnits * buySellMultipliter * -1),
				this.getTrade().getSide(), action, 0.01);

		int quantity = Math.abs(this.getTrade().getOpenQuantity() * percentQty) / 100;

		String ocaID = new String(Integer.toString((new BigDecimal(Math
				.random() * 1000000)).intValue()));

		TradeOrder orderTarget = new TradeOrder(this.getTrade(), action,
				OrderType.LMT, quantity, null,
				targetPrice.getBigDecimalValue(), createDate);

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
		this.getTrade().addTradeOrder(orderTarget);
		/*
		 * Note the last order submitted in TWS on OCA order is the only one
		 * that can be updated
		 */

		TradeOrder orderStop = new TradeOrder(this.getTrade(), action,
				OrderType.STP, quantity, stopPrice.getBigDecimalValue(), null,
				createDate);
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
		this.getTrade().addTradeOrder(orderStop);
		return targetPrice;
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
	 * @throws ValueTypeException
	 */
	public Money getStopPriceForPositionRisk(TradeOrder openPosition,
			int numberRiskUnits) throws ValueTypeException {

		double riskAmount = (this.getTradestrategy().getRiskAmount()
				.doubleValue() / this.getTrade().getOpenQuantity())
				* numberRiskUnits;

		if (Side.BOT.equals(this.getTrade().getSide())) {
			riskAmount = riskAmount * -1;
		}

		// Add a penny to the stop
		Money stopPrice = new Money(openPosition.getAverageFilledPrice()
				.doubleValue() + riskAmount);
		return stopPrice;
	}

	/**
	 * Method closeAllOpenPositions. This method will close all open order
	 * positions for the Trade.
	 * 
	 * @throws StrategyRuleException
	 */
	public void closeAllOpenPositions() throws StrategyRuleException {

		_log.info("Strategy  closeAllOpenPositions symbol: " + symbol);
		try {
			cancelAllOrders();
			if (this.isPositionOpen()) {
				closePosition(true);
			}
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 70,
					"Error StrategyWorker exception: " + ex.getMessage());
		}
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
	public void moveStopOCAPrice(Money stopPrice, boolean stopTransmit)
			throws StrategyRuleException {

		_log.info("Strategy  moveStopOCAPrice symbol: " + symbol
				+ " Stop Price: " + stopPrice);
		try {
			if (this.isPositionOpen()) {
				// Cancel the tgt and stop orders i.e. OCA
				for (TradeOrder tradeOrder : this.getTrade().getTradeOrders()) {
					if (!tradeOrder.getIsOpenPosition()
							&& !tradeOrder.getIsFilled()
							&& !OrderStatus.CANCELLED.equals(tradeOrder
									.getStatus())
							&& !OrderStatus.INACTIVE.equals(tradeOrder
									.getStatus())) {
						if (OrderType.STP.equals(tradeOrder.getOrderType())) {
							if (null != stopPrice) {
								tradeOrder.setAuxPrice(stopPrice
										.getBigDecimalValue());
							}
							tradeOrder.setStatus(OrderStatus.UNSUBMIT);
							tradeOrder.setTransmit(stopTransmit);
							tradeOrder = getBrokerManager().onPlaceOrder(
									getTradestrategy().getContract(),
									tradeOrder);
						}
					}
				}
			}
		} catch (Exception ex) {
			_log.error(
					"Error StrategyWorker moveStopOCAPrice exception Symbol: "
							+ this.symbol + " Stop Price: " + stopPrice
							+ " Msg: " + ex.getMessage(), ex);
			throw new StrategyRuleException(1, 80,
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

		_log.info("Strategy  cancelAllPositions symbol: " + symbol);
		try {
			if (this.isPositionOpen()) {
				for (TradeOrder order : trade.getTradeOrders()) {
					cancelOrder(order);
				}
			}
		} catch (Exception ex) {
			throw new StrategyRuleException(1, 90,
					"Error StrategyWorker exception: " + ex.getMessage());
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
	 * @throws ValueTypeException
	 */
	public Money addPennyAndRoundStop(double price, String side, String action,
			double dollars) throws ValueTypeException {
		double roundPrice = 0;
		if (Side.BOT.equals(side)) {
			roundPrice = roundPrice(price + dollars, action);
		} else {
			roundPrice = roundPrice(price - dollars, action);
		}
		return new Money(roundPrice);
	}

	/**
	 * Method isPositionOpen.
	 * 
	 * @return boolean
	 */
	public boolean isPositionOpen() {
		if (null != getTrade()) {
			return getTrade().getIsOpen();
		}
		return false;
	}

	/**
	 * Method isPositionCancelled.
	 * 
	 * @return boolean
	 */
	public boolean isPositionCancelled() {
		if (null != getTrade()) {
			if (!getTrade().getIsOpen()) {
				if (OrderStatus.CANCELLED.equals(getTrade()
						.getOpenPositionOrder().getStatus())) {
					return true;
				}
			}
		}
		return false;
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
			throw new StrategyRuleException(1, 100,
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
	 * @throws PersistentModelException
	 */
	public void updateTradestrategyStatus(String status)
			throws PersistentModelException {
		Tradestrategy transientInstance = this.tradePersistentModel
				.findTradestrategyById(getTradestrategy());
		transientInstance.setStatus(status);
		this.tradestrategy = this.tradePersistentModel
				.persistTradestrategy(transientInstance);
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
	 * Method getIndividualAccount. Return a refreshed trade account note this
	 * is updated when connected to TWS every time the account values change.
	 * 
	 * @return Account
	 * @throws PersistentModelException
	 */
	public Account getIndividualAccount() throws PersistentModelException {
		return this.tradePersistentModel.findAccountByNumber(getTradestrategy()
				.getPortfolio().getIndividualAccount().getAccountNumber());
	}

	/**
	 * Method getTrade.
	 * 
	 * @return Trade
	 */
	public Trade getTrade() {
		return this.trade;
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
	 * The open position is initially created in createSubmitOpenPosition this
	 * is called from the Strategy that has rules to enter a position.
	 * Strategies that manage positions also need access to the openPosition
	 * order so here we populated this from the trade.
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * State isOpen = false and ProfitLoss is null when created not filled.
	 * State isOpen = true when position is open and filled. State isOpen =
	 * false and ProfitLoss not null when closed.
	 * 
	 * @return openPostion The TradeOrder that open this trade position. null
	 * return means no open position.
	 */

	/**
	 * Method getOpenPositionOrder.
	 * 
	 * @return TradeOrder
	 */
	public TradeOrder getOpenPositionOrder() {
		if (null != getTrade()) {
			return getTrade().getOpenPositionOrder();
		}
		return null;
	}

	/**
	 * Method isThereOpenPositionOrder.
	 * 
	 * @return boolean
	 */
	public boolean isThereOpenPositionOrder() {
		if (null != getOpenPositionOrder())
			return true;
		return false;
	}

	/**
	 * Method isThereOpenPositionOrder.
	 * 
	 * @return boolean
	 * @throws PersistentModelException
	 */
	public boolean isThereOpenPositionByContract()
			throws PersistentModelException {
		Trade openTrade = tradePersistentModel
				.findOpenTradeByContractId(getTradestrategy().getContract()
						.getIdContract());
		if (null != openTrade)
			return true;
		return false;
	}

	/**
	 * Method getTargetPrice.
	 * 
	 * @return Money
	 */
	public Money getTargetPrice() {
		return this.targetPrice;
	}

	/**
	 * Method setTargetPrice.
	 * 
	 * @param targetPrice
	 *            Money
	 * @return Money
	 */
	public Money setTargetPrice(Money targetPrice) {
		return this.targetPrice = targetPrice;
	}

	/**
	 * Method setTargetPrice.
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
	 * Method roundPrice.
	 * 
	 * @param price
	 *            double
	 * @param action
	 *            String
	 * @return double
	 * @throws ValueTypeException
	 */
	private double roundPrice(double price, String action)
			throws ValueTypeException {

		// Round at whole and half numbers add to this if you
		// need others.
		Entrylimit entrylimit = getEntryLimit().getValue(new Money(price));
		if (null == entrylimit) {
			throw new ValueTypeException("No EntryLimits found for price: "
					+ price);
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
	}

	/**
	 * Method tradeOrderFilled.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 */
	public void tradeOrderFilled(TradeOrder tradeOrder) {

	}
}
