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
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.candle.CandleItem;

/**
 */
@Stateless
public class CandleHome {

	private EntityManager entityManager = null;

	public CandleHome() {

	}

	/**
	 * Method persistCandleSeries.
	 * 
	 * @param candleSeries
	 *            CandleSeries
	 * @throws Exception
	 */
	public synchronized void persistCandleSeries(CandleSeries candleSeries)
			throws Exception {

		try {
			if (candleSeries.isEmpty())
				return;
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Tradingday tradingday = null;
			Contract contract = findContractById(candleSeries.getContract()
					.getIdContract());
			for (int i = 0; i < candleSeries.getItemCount(); i++) {

				CandleItem candleItem = (CandleItem) candleSeries
						.getDataItem(i);

				if (!candleItem.getCandle().getTradingday().equals(tradingday)) {

					if (null == candleItem.getCandle().getTradingday()
							.getIdTradingDay()) {
						tradingday = findTradingdayByDate(candleItem
								.getCandle().getTradingday().getOpen(),
								candleItem.getCandle().getTradingday()
										.getClose());
					} else {
						tradingday = findTradingdayById(candleItem.getCandle()
								.getTradingday().getIdTradingDay());
					}

					if (null == tradingday) {
						entityManager.persist(candleItem.getCandle()
								.getTradingday());
						entityManager.getTransaction().commit();
						entityManager.getTransaction().begin();
						tradingday = candleItem.getCandle().getTradingday();
					} else {
						Integer idTradingday = tradingday.getIdTradingDay();
						Integer idContract = contract.getIdContract();
						Integer barSize = candleSeries.getBarSize();
						String hqlDelete = "delete Candle where idContract = :idContract and idTradingday = :idTradingday and barSize = :barSize";
						entityManager.createQuery(hqlDelete)
								.setParameter("idContract", idContract)
								.setParameter("idTradingday", idTradingday)
								.setParameter("barSize", barSize)
								.executeUpdate();
						entityManager.getTransaction().commit();
						entityManager.getTransaction().begin();
					}
				}

				Candle transientInstance = candleItem.getCandle();
				transientInstance.setTradingday(tradingday);
				transientInstance.setContract(contract);
				entityManager.persist(transientInstance);

				// Commit every 50 rows
				if ((Math.floor(i / 50d) == (i / 50d)) && (i > 0)) {
					entityManager.getTransaction().commit();
					entityManager.getTransaction().begin();
				}
			}
			entityManager.getTransaction().commit();
		} catch (RuntimeException re) {
			EntityManagerHelper.logError("Error persistCandleSeries failed :"
					+ re.getMessage(), re);
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findByContractAndDateRange.
	 * 
	 * @param idContract
	 *            Integer
	 * @param startPeriod
	 *            Date
	 * @param endPeriod
	 *            Date
	 * @return List<Candle>
	 */
	public List<Candle> findByContractAndDateRange(Integer idContract,
			Date startPeriod, Date endPeriod, Integer barSize) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Candle> query = builder.createQuery(Candle.class);
			Root<Candle> from = query.from(Candle.class);
			query.select(from);
			List<Predicate> predicates = new ArrayList<Predicate>();

			if (null != idContract) {
				Join<Candle, Contract> contract = from.join("contract");
				Predicate predicate = builder.equal(contract.get("idContract"),
						idContract);
				predicates.add(predicate);
			}
			if (null != startPeriod) {
				Expression<Date> start = from.get("startPeriod");
				Predicate predicate = builder.greaterThanOrEqualTo(start,
						startPeriod);
				predicates.add(predicate);
			}
			if (null != endPeriod) {
				Expression<Date> end = from.get("endPeriod");
				Predicate predicate = builder.lessThanOrEqualTo(end, endPeriod);
				predicates.add(predicate);
			}
			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Candle> typedQuery = entityManager.createQuery(query);
			List<Candle> items = typedQuery.getResultList();
			entityManager.getTransaction().commit();
			return items;

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findCandlesByContractDateRangeBarSize.
	 * 
	 * @param idContract
	 *            Integer
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @param barSize
	 *            Integer
	 * @return List<Candle>
	 */
	public List<Candle> findCandlesByContractDateRangeBarSize(
			Integer idContract, Date startOpenDate, Date endOpenDate,
			Integer barSize) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Candle> query = builder.createQuery(Candle.class);
			Root<Candle> from = query.from(Candle.class);
			query.select(from);
			query.orderBy(builder.asc(from.get("startPeriod")));
			List<Predicate> predicates = new ArrayList<Predicate>();
			if (null != idContract) {
				Join<Candle, Contract> contract = from.join("contract");
				Predicate predicateContract = builder.equal(
						contract.get("idContract"), idContract);
				predicates.add(predicateContract);
			}
			if (null != startOpenDate) {
				Join<Candle, Tradingday> tradingdayStartDate = from
						.join("tradingday");
				Predicate predicateStartDate = builder.greaterThanOrEqualTo(
						tradingdayStartDate.get("open").as(Date.class),
						startOpenDate);
				predicates.add(predicateStartDate);
			}
			if (null != endOpenDate) {
				Join<Candle, Tradingday> tradingdayEndDate = from
						.join("tradingday");
				Predicate predicateEndDate = builder.lessThanOrEqualTo(
						tradingdayEndDate.get("open").as(Date.class),
						endOpenDate);
				predicates.add(predicateEndDate);
			}
			if (null != barSize) {
				Expression<Integer> expBarSize = from.get("barSize");
				Predicate predicate = builder.greaterThanOrEqualTo(expBarSize,
						barSize);
				predicates.add(predicate);
			}

			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Candle> typedQuery = entityManager.createQuery(query);
			List<Candle> items = typedQuery.getResultList();
			entityManager.getTransaction().commit();
			return items;

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findById.
	 * 
	 * @param idCandle
	 *            Integer
	 * @return Candle
	 */
	public Candle findById(Integer idCandle) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Candle instance = entityManager.find(Candle.class, idCandle);
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
	 * Method findByUniqueKey.
	 * 
	 * @param idTradingday
	 *            Integer
	 * @param idContract
	 *            Integer
	 * @param startPeriod
	 *            Date
	 * @param endPeriod
	 *            Date
	 * @return Candle
	 */
	public Candle findByUniqueKey(Integer idTradingday, Integer idContract,
			Date startPeriod, Date endPeriod, Integer barSize) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Candle> query = builder.createQuery(Candle.class);
			Root<Candle> from = query.from(Candle.class);
			query.select(from);
			List<Predicate> predicates = new ArrayList<Predicate>();

			if (null != idTradingday) {
				Join<Candle, Tradingday> tradingday = from.join("tradingday");
				Predicate predicate = builder.equal(
						tradingday.get("idTradingDay"), idTradingday);
				predicates.add(predicate);
			}
			if (null != idContract) {
				Join<Candle, Contract> contract = from.join("contract");
				Predicate predicate = builder.equal(contract.get("idContract"),
						idContract);
				predicates.add(predicate);
			}
			if (null != startPeriod) {
				Predicate predicate = builder.equal(from.get("startPeriod"),
						startPeriod);
				predicates.add(predicate);
			}
			if (null != endPeriod) {
				Predicate predicate = builder.equal(from.get("endPeriod"),
						endPeriod);
				predicates.add(predicate);
			}
			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Candle> typedQuery = entityManager.createQuery(query);
			List<Candle> items = typedQuery.getResultList();
			entityManager.getTransaction().commit();
			if (items.size() > 0) {
				return items.get(0);
			}
			return null;
		} catch (RuntimeException re) {
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findCandleCount.
	 * 
	 * @param idTradingday
	 *            Integer
	 * @param idContract
	 *            Integer
	 * @return Long
	 */
	public Long findCandleCount(Integer idTradingday, Integer idContract) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Object> query = builder.createQuery();
			Root<Candle> from = query.from(Candle.class);
			Expression<Long> id = from.get("idCandle");
			Expression<Long> minExpression = builder.count(id);

			List<Predicate> predicates = new ArrayList<Predicate>();

			if (null != idTradingday) {
				Join<Candle, Tradingday> tradingday = from.join("tradingday");
				Predicate predicate = builder.equal(
						tradingday.get("idTradingDay"), idTradingday);
				predicates.add(predicate);
			}
			if (null != idContract) {
				Join<Candle, Contract> contract = from.join("contract");
				Predicate predicate = builder.equal(contract.get("idContract"),
						idContract);
				predicates.add(predicate);
			}
			query.where(predicates.toArray(new Predicate[] {}));
			CriteriaQuery<Object> select = query.select(minExpression);
			TypedQuery<Object> typedQuery = entityManager.createQuery(select);
			Object item = typedQuery.getSingleResult();
			entityManager.getTransaction().commit();
			if (null == item)
				item = new Long(0);

			return (Long) item;

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findContractById.
	 * 
	 * @param id
	 *            Integer
	 * @return Contract
	 */
	private Contract findContractById(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			Contract instance = entityManager.find(Contract.class, id);
			return instance;
		} catch (RuntimeException re) {
			throw re;
		}
	}

	/**
	 * Method findContractById.
	 * 
	 * @param id
	 *            Integer
	 * @return Contract
	 */
	private Tradingday findTradingdayById(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			Tradingday instance = entityManager.find(Tradingday.class, id);
			return instance;
		} catch (RuntimeException re) {
			throw re;
		}
	}

	/**
	 * Method findTradingdayByDate.
	 * 
	 * @param open
	 *            Date
	 * @return Tradingday
	 */
	private Tradingday findTradingdayByDate(Date open, Date close) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tradingday> query = builder
					.createQuery(Tradingday.class);
			Root<Tradingday> from = query.from(Tradingday.class);
			query.select(from);
			if (null != open)
				query.where(builder.equal(from.get("open"), open));
			if (null != close)
				query.where(builder.equal(from.get("close"), close));
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
}
