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
import java.util.List;

import org.trade.core.dao.Aspect;

/**
 */
public class TradelogReport extends Aspect implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3388042483785305102L;

	private List<TradelogDetail> tradelogDetail = new ArrayList<TradelogDetail>(
			0);
	private List<TradelogSummary> tradelogSummary = new ArrayList<TradelogSummary>(
			0);

	public TradelogReport() {
	}

	/**
	 * Constructor for TradelogReport.
	 * 
	 * @param idTradelogDetail
	 *            Integer
	 */
	public TradelogReport(Integer idTradelogDetail) {
		this.id = idTradelogDetail;
	}

	/**
	 * Constructor for TradelogReport.
	 * 
	 * @param idTradelogDetail
	 *            Integer
	 * @param tradelogDetail
	 *            List<TradelogDetail>
	 */
	public TradelogReport(Integer idTradelogDetail,
			List<TradelogDetail> tradelogDetail) {
		this.id = idTradelogDetail;
		this.tradelogDetail = tradelogDetail;

	}

	/**
	 * Method getIdTradingdays.
	 * 
	 * @return Integer
	 */
	public Integer getIdTradingdays() {
		return this.id;
	}

	/**
	 * Method setIdTradingdays.
	 * 
	 * @param idTradelogDetail
	 *            Integer
	 */
	public void setIdTradingdays(Integer idTradelogDetail) {
		this.id = idTradelogDetail;
	}

	/**
	 * Method add.
	 * 
	 * @param tradelogSummary
	 *            TradelogSummary
	 */
	public void add(TradelogSummary tradelogSummary) {
		this.tradelogSummary.add(tradelogSummary);
	}

	/**
	 * Method remove.
	 * 
	 * @param tradelogSummary
	 *            TradelogSummary
	 */
	public void remove(TradelogSummary tradelogSummary) {
		this.tradelogSummary.remove(tradelogSummary);
	}

	/**
	 * Method getTradelogSummary.
	 * 
	 * @return List<TradelogSummary>
	 */
	public List<TradelogSummary> getTradelogSummary() {
		return this.tradelogSummary;
	}

	/**
	 * Method setTradelogSummary.
	 * 
	 * @param tradelogSummary
	 *            List<TradelogSummary>
	 */
	public void setTradelogSummary(List<TradelogSummary> tradelogSummary) {
		this.tradelogSummary = tradelogSummary;
	}

	/**
	 * Method add.
	 * 
	 * @param tradelogDetail
	 *            TradelogDetail
	 */
	public void add(TradelogDetail tradelogDetail) {
		this.tradelogDetail.add(tradelogDetail);
	}

	/**
	 * Method remove.
	 * 
	 * @param tradelogDetail
	 *            TradelogDetail
	 */
	public void remove(TradelogDetail tradelogDetail) {
		this.tradelogDetail.remove(tradelogDetail);
	}

	/**
	 * Method getTradelogDetail.
	 * 
	 * @return List<TradelogDetail>
	 */
	public List<TradelogDetail> getTradelogDetail() {
		return this.tradelogDetail;
	}

	/**
	 * Method setTradelogDetail.
	 * 
	 * @param tradelogDetail
	 *            List<TradelogDetail>
	 */
	public void setTradelogDetail(List<TradelogDetail> tradelogDetail) {
		this.tradelogDetail = tradelogDetail;
	}

	public void clear() {
		tradelogDetail.clear();
		tradelogSummary.clear();
	}
}
