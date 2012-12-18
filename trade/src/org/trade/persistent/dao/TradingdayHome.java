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
package org.trade.persistent.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.trade.core.dao.EntityManagerHelper;
import org.trade.core.util.TradingCalendar;
import org.trade.strategy.data.IndicatorSeries;

/**
 */
@Stateless
public class TradingdayHome {

	private EntityManager entityManager = null;

	public TradingdayHome() {

	}

	/**
	 * Method persist. This method saves all the trade-strategies for all the
	 * tradingdays from the Trading Tab.
	 * 
	 * @param detachedInstance
	 *            Tradingday as set of tradingdays with associated
	 *            tradestrategies.
	 * @throws Exception
	 */
	public void persist(Tradingday detachedInstance) throws Exception {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			/*
			 * Check the incoming tradingday to see if it exists if it does
			 * merge with the persisted one if not persist.
			 */
			Tradingday tradingday = null;
			if (null == detachedInstance.getIdTradingDay()) {
				tradingday = this
						.findTradingdayByOpenCloseDate(
								detachedInstance.getOpen(),
								detachedInstance.getClose());
				if (null == tradingday) {
					entityManager.persist(detachedInstance);
				}
			} else {
				tradingday = entityManager.merge(detachedInstance);
				detachedInstance.setVersion(tradingday.getVersion());
			}
			entityManager.getTransaction().commit();

			for (Tradestrategy tradestrategy : detachedInstance
					.getTradestrategies()) {
				// If it has trades do nothing
				if (tradestrategy.getTrades().isEmpty()
						&& tradestrategy.isDirty()) {
					entityManager.getTransaction().begin();

					/*
					 * If the tradingday existed use the persisted version.
					 */
					if (null != tradingday) {
						tradestrategy.setTradingday(tradingday);
					}
					/*
					 * The strategy will always exist as these cannot be created
					 * via this tab, as they are a drop down list. So find the
					 * persisted one and set this.
					 */
					Strategy strategy = this.findStrategyByName(tradestrategy
							.getStrategy().getName());
					if (null != strategy) {
						tradestrategy.setStrategy(strategy);
					}
					/*
					 * Check to see if the contract exists if it does merge and
					 * set the new persisted one. If no persist the contract.
					 */
					Contract contract = this.findContractByUniqueKey(
							tradestrategy.getContract().getSecType(),
							tradestrategy.getContract().getSymbol(),
							tradestrategy.getContract().getExchange(),
							tradestrategy.getContract().getCurrency(),
							tradestrategy.getContract().getExpiry());
					if (null != contract) {
						tradestrategy.setContract(contract);
					}
					/*
					 * Persist or merge the tradestrategy.
					 */
					if (null == tradestrategy.getIdTradeStrategy()) {
						entityManager.persist(tradestrategy);
						entityManager.getTransaction().commit();
					} else {
						Tradestrategy instance = entityManager
								.merge(tradestrategy);
						entityManager.getTransaction().commit();
						tradestrategy.setVersion(instance.getVersion());
					}
					tradestrategy.setDirty(false);
					for (IndicatorSeries indicatorSeries : tradestrategy
							.getStrategy().getIndicatorSeries()) {
						indicatorSeries.getCodeValues().size();
					}
				}
			}
			entityManager.getTransaction().begin();
			List<Tradestrategy> tradestrategies = findTradestrategyByDate(detachedInstance
					.getOpen());

			for (Tradestrategy tradestrategy : tradestrategies) {
				boolean exists = false;
				for (Tradestrategy newTradestrategy : detachedInstance
						.getTradestrategies()) {
					if (newTradestrategy.equals(tradestrategy)) {
						exists = true;
						if (!tradestrategy.getIdTradeStrategy().equals(
								newTradestrategy.getIdTradeStrategy())
								|| (null == newTradestrategy
										.getIdTradeStrategy())) {
							throw new Exception("The following Contract:"
									+ newTradestrategy.getContract()
											.getSymbol()
									+ " Strategy:"
									+ newTradestrategy.getStrategy().getName()
									+ " Tradingday: "
									+ newTradestrategy.getTradingday()
											.getOpen() + " already exists.");
						}
					}
				}
				if (!exists) {
					if (tradestrategy.getTrades().isEmpty()) {
						entityManager.remove(tradestrategy);
					} else {
						throw new Exception("The following Contract:"
								+ tradestrategy.getContract().getSymbol()
								+ " Strategy:"
								+ tradestrategy.getStrategy().getName()
								+ " already exists with trades.");
					}
				}
			}
			entityManager.getTransaction().commit();
			detachedInstance.setDirty(false);

		} catch (RuntimeException re) {
			EntityManagerHelper.logError(
					"Error saving Tradingdays: " + re.getMessage(), re);
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findTradingdayById.
	 * 
	 * @param id
	 *            Integer
	 * @return Tradingday
	 */
	public Tradingday findTradingdayById(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Tradingday instance = entityManager.find(Tradingday.class, id);
			if (null != instance) {
				for (Tradestrategy tradestrategy : instance
						.getTradestrategies()) {
					tradestrategy.getStrategy().getIndicatorSeries().size();
					for (Trade trade : tradestrategy.getTrades()) {
						trade.getTradeOrders().size();
					}
				}
			}
			entityManager.getTransaction().commit();
			return instance;
		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findTradingdaysByDateRange.
	 * 
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @return Tradingdays
	 */
	public Tradingdays findTradingdaysByDateRange(Date startDate, Date endDate) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Tradingdays tradingdays = new Tradingdays();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tradingday> query = builder
					.createQuery(Tradingday.class);
			Root<Tradingday> from = query.from(Tradingday.class);
			query.select(from);
			query.orderBy(builder.desc(from.get("open")));
			List<Predicate> predicates = new ArrayList<Predicate>();

			if (null != startDate) {
				Predicate predicate = builder.greaterThanOrEqualTo(
						from.get("open").as(Date.class), startDate);
				predicates.add(predicate);
			}
			if (null != endDate) {
				Predicate predicate = builder.lessThanOrEqualTo(from
						.get("open").as(Date.class), endDate);
				predicates.add(predicate);
			}

			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Tradingday> typedQuery = entityManager
					.createQuery(query);
			List<Tradingday> items = typedQuery.getResultList();
			for (Tradingday tradingday : items) {
				tradingdays.add(tradingday);
				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {
					tradestrategy.getTrades().size();
					for (IndicatorSeries indicatorSeries : tradestrategy
							.getStrategy().getIndicatorSeries()) {
						indicatorSeries.getCodeValues().size();
					}
				}
			}
			entityManager.getTransaction().commit();
			return tradingdays;

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findByOpen.
	 * 
	 * @param open
	 *            Date
	 * @return Tradingday
	 */
	public Tradingday findByOpenCloseDate(Date openDate, Date closeDate) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tradingday> query = builder
					.createQuery(Tradingday.class);
			Root<Tradingday> from = query.from(Tradingday.class);
			query.select(from);
			if (null != openDate)
				query.where(builder.equal(from.get("open"), openDate));
			if (null != closeDate)
				query.where(builder.equal(from.get("close"), closeDate));
			List<Tradingday> items = entityManager.createQuery(query)
					.getResultList();
			for (Tradingday tradingday : items) {
				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {
					tradestrategy.getTrades().size();
					tradestrategy.getStrategy().getIndicatorSeries().size();
				}
			}
			entityManager.getTransaction().commit();
			if (items.size() > 0) {
				return items.get(0);
			}
			return null;

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findStrategyByName.
	 * 
	 * @param name
	 *            String
	 * @return Strategy
	 */
	private Strategy findStrategyByName(String name) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Strategy> query = builder.createQuery(Strategy.class);
			Root<Strategy> from = query.from(Strategy.class);
			query.select(from);
			query.where(builder.equal(from.get("name"), name));
			List<Strategy> items = entityManager.createQuery(query)
					.getResultList();
			if (items.size() > 0) {
				return items.get(0);
			}
			return null;

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		}
	}

	/**
	 * Method findTradingdayByOpenDate.
	 * 
	 * @param open
	 *            Date
	 * @return Tradingday
	 */
	private Tradingday findTradingdayByOpenCloseDate(Date openDate,
			Date closeDate) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tradingday> query = builder
					.createQuery(Tradingday.class);
			Root<Tradingday> from = query.from(Tradingday.class);
			query.select(from);

			if (null != openDate)
				query.where(builder.equal(from.get("open"), openDate));
			if (null != closeDate)
				query.where(builder.equal(from.get("close"), closeDate));
			List<Tradingday> items = entityManager.createQuery(query)
					.getResultList();

			if (items.size() > 0) {
				return items.get(0);
			}
			return null;

		} catch (RuntimeException re) {
			throw re;
		}
	}

	/**
	 * Method findTradestrategyByDate.
	 * 
	 * @param open
	 *            Date
	 * @return List<Tradestrategy>
	 */
	private List<Tradestrategy> findTradestrategyByDate(Date open) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tradestrategy> query = builder
					.createQuery(Tradestrategy.class);
			Root<Tradestrategy> from = query.from(Tradestrategy.class);
			query.select(from);
			List<Predicate> predicates = new ArrayList<Predicate>();

			if (null != open) {
				Join<Tradestrategy, Tradingday> tradingday = from
						.join("tradingday");
				Predicate predicate = builder.equal(tradingday.get("open"),
						open);
				predicates.add(predicate);
			}
			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Tradestrategy> typedQuery = entityManager
					.createQuery(query);
			List<Tradestrategy> items = typedQuery.getResultList();
			return items;

		} catch (RuntimeException re) {
			throw re;
		}
	}

	/**
	 * Method findContractByUniqueKey.
	 * 
	 * @param SECType
	 *            String
	 * @param symbol
	 *            String
	 * @param exchange
	 *            String
	 * @param currency
	 *            String
	 * @param expiryDate
	 *            Date
	 * @return Contract
	 */
	private Contract findContractByUniqueKey(String SECType, String symbol,
			String exchange, String currency, Date expiryDate) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Contract> query = builder.createQuery(Contract.class);
			Root<Contract> from = query.from(Contract.class);
			query.select(from);
			List<Predicate> predicates = new ArrayList<Predicate>();

			if (null != SECType) {
				Predicate predicate = builder.equal(from.get("secType"),
						SECType);
				predicates.add(predicate);
			}
			if (null != symbol) {
				Predicate predicate = builder.equal(from.get("symbol"), symbol);
				predicates.add(predicate);
			}
			if (null != exchange) {
				Predicate predicate = builder.equal(from.get("exchange"),
						exchange);
				predicates.add(predicate);
			}
			if (null != currency) {
				Predicate predicate = builder.equal(from.get("currency"),
						currency);
				predicates.add(predicate);
			}
			if (null != expiryDate) {

				Integer yearExpiry = TradingCalendar.getYear(expiryDate);
				Expression<Integer> year = builder.function("year",
						Integer.class, from.get("expiry"));
				Predicate predicateYear = builder.equal(year, yearExpiry);
				predicates.add(predicateYear);

				Integer monthExpiry = TradingCalendar.getMonth(expiryDate);
				Expression<Integer> month = builder.function("month",
						Integer.class, from.get("expiry"));
				Predicate predicateMonth = builder.equal(month, new Integer(
						1 + monthExpiry.intValue()));
				predicates.add(predicateMonth);
			}
			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Contract> typedQuery = entityManager.createQuery(query);
			List<Contract> items = typedQuery.getResultList();
			if (items.size() > 0) {
				return items.get(0);
			}
			return null;

		} catch (RuntimeException re) {
			throw re;
		}
	}
}