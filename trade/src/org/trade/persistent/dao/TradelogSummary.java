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

import java.io.IOException;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;

import org.trade.core.dao.Aspect;
import org.trade.core.properties.ConfigProperties;

/**
 */
@Entity
@SqlResultSetMappings({
		@SqlResultSetMapping(name = "TradelogSummaryMapping", entities = @EntityResult(entityClass = TradelogSummary.class) ) })
public class TradelogSummary extends Aspect implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -832064631322873796L;
	private String period;
	private BigDecimal battingAverage;
	private BigDecimal simpleSharpeRatio;
	private BigDecimal grossProfitLoss;
	private Integer quantity;
	private BigDecimal commission;
	private BigDecimal netProfitLoss;
	private BigDecimal profitAmount;
	private BigDecimal lossAmount;
	private Integer winCount;
	private Integer lossCount;
	private Integer positionCount;
	private Integer tradestrategyCount;

	public TradelogSummary() {
	}

	/**
	 * Constructor for TradelogSummary.
	 * 
	 * @param idTradelogSummary
	 *            Integer
	 * @param period
	 *            String
	 * @param battingAverage
	 *            BigDecimal
	 * @param simpleSharpeRatio
	 *            BigDecimal
	 * @param grossProfitLoss
	 *            BigDecimal
	 * @param quantity
	 *            Integer
	 * @param commission
	 *            BigDecimal
	 * @param netProfitLoss
	 *            BigDecimal
	 * @param profitAmount
	 *            BigDecimal
	 * @param lossAmount
	 *            BigDecimal
	 * @param winCount
	 *            Integer
	 * @param lossCount
	 *            Integer
	 * @param positionCount
	 *            Integer
	 * @param tradestrategyCount
	 *            Integer
	 */
	public TradelogSummary(Integer idTradelogSummary, String period, BigDecimal battingAverage,
			BigDecimal simpleSharpeRatio, BigDecimal grossProfitLoss, Integer quantity, BigDecimal commission,
			BigDecimal netProfitLoss, BigDecimal profitAmount, BigDecimal lossAmount, Integer winCount,
			Integer lossCount, Integer positionCount, Integer tradestrategyCount) {
		this.id = idTradelogSummary;
		this.period = period;
		this.battingAverage = battingAverage;
		this.simpleSharpeRatio = simpleSharpeRatio;
		this.grossProfitLoss = grossProfitLoss;
		this.quantity = quantity;
		this.commission = commission;
		this.netProfitLoss = netProfitLoss;
		this.profitAmount = profitAmount;
		this.lossAmount = lossAmount;
		this.winCount = winCount;
		this.lossCount = lossCount;
		this.positionCount = positionCount;
		this.tradestrategyCount = tradestrategyCount;
	}

	/**
	 * Method getIdTradelogSummary.
	 * 
	 * @return Integer
	 */
	@Id
	@Column(name = "idTradelogSummary")
	public Integer getIdTradelogSummary() {
		return this.id;
	}

	/**
	 * Method setIdTradelogSummary.
	 * 
	 * @param idTradelogSummary
	 *            Integer
	 */
	public void setIdTradelogSummary(Integer idTradelogSummary) {
		this.id = idTradelogSummary;
	}

	/**
	 * Method getPeriod.
	 * 
	 * @return String
	 */
	@Column(name = "period", length = 19)
	public String getPeriod() {
		return this.period;
	}

	/**
	 * Method setPeriod.
	 * 
	 * @param period
	 *            String
	 */
	public void setPeriod(String period) {
		this.period = period;
	}

	/**
	 * Method getBattingAverage.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "battingAverage", precision = 10)
	public BigDecimal getBattingAverage() {
		return this.battingAverage;
	}

	/**
	 * Method setBattingAverage.
	 * 
	 * @param battingAverage
	 *            BigDecimal
	 */
	public void setBattingAverage(BigDecimal battingAverage) {
		this.battingAverage = battingAverage;
	}

	/**
	 * Method getSimpleSharpeRatio.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "simpleSharpeRatio", precision = 10)
	public BigDecimal getSimpleSharpeRatio() {
		return this.simpleSharpeRatio;
	}

	/**
	 * Method setSimpleSharpeRatio.
	 * 
	 * @param simpleSharpeRatio
	 *            BigDecimal
	 */
	public void setSimpleSharpeRatio(BigDecimal simpleSharpeRatio) {
		this.simpleSharpeRatio = simpleSharpeRatio;
	}

	/**
	 * Method getQuantity.
	 * 
	 * @return Integer
	 */
	@Column(name = "quantity")
	public Integer getQuantity() {
		return this.quantity;
	}

	/**
	 * Method setQuantity.
	 * 
	 * @param quantity
	 *            Integer
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	/**
	 * Method getGrossProfitLoss.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "grossProfitLoss", precision = 10)
	public BigDecimal getGrossProfitLoss() {
		return this.grossProfitLoss;
	}

	/**
	 * Method setGrossProfitLoss.
	 * 
	 * @param grossProfitLoss
	 *            BigDecimal
	 */
	public void setGrossProfitLoss(BigDecimal grossProfitLoss) {
		this.grossProfitLoss = grossProfitLoss;
	}

	/**
	 * Method getCommission.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "commission", precision = 10)
	public BigDecimal getCommission() {
		return this.commission;
	}

	/**
	 * Method setCommission.
	 * 
	 * @param commission
	 *            BigDecimal
	 */
	public void setCommission(BigDecimal commission) {
		this.commission = commission;
	}

	/**
	 * Method getNetProfitLoss.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "netProfitLoss", precision = 10)
	public BigDecimal getNetProfitLoss() {
		return this.netProfitLoss;
	}

	/**
	 * Method setNetProfitLoss.
	 * 
	 * @param netProfitLoss
	 *            BigDecimal
	 */
	public void setNetProfitLoss(BigDecimal netProfitLoss) {
		this.netProfitLoss = netProfitLoss;
	}

	/**
	 * Method getProfitAmount.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "profitAmount", precision = 10)
	public BigDecimal getProfitAmount() {
		return this.profitAmount;
	}

	/**
	 * Method setProfitAmount.
	 * 
	 * @param profitAmount
	 *            BigDecimal
	 */
	public void setProfitAmount(BigDecimal profitAmount) {
		this.profitAmount = profitAmount;
	}

	/**
	 * Method getLossAmount.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "lossAmount", precision = 10)
	public BigDecimal getLossAmount() {
		return this.lossAmount;
	}

	/**
	 * Method setLossAmount.
	 * 
	 * @param lossAmount
	 *            BigDecimal
	 */
	public void setLossAmount(BigDecimal lossAmount) {
		this.lossAmount = lossAmount;
	}

	/**
	 * Method getWinCount.
	 * 
	 * @return Integer
	 */
	@Column(name = "winCount")
	public Integer getWinCount() {
		return this.winCount;
	}

	/**
	 * Method setWinCount.
	 * 
	 * @param winCount
	 *            Integer
	 */
	public void setWinCount(Integer winCount) {
		this.winCount = winCount;
	}

	/**
	 * Method getLossCount.
	 * 
	 * @return Integer
	 */
	@Column(name = "lossCount")
	public Integer getLossCount() {
		return this.lossCount;
	}

	/**
	 * Method setLossCount.
	 * 
	 * @param lossCount
	 *            Integer
	 */
	public void setLossCount(Integer lossCount) {
		this.lossCount = lossCount;
	}

	/**
	 * Method getPositionCount.
	 * 
	 * @return Integer
	 */
	@Column(name = "positionCount")
	public Integer getPositionCount() {
		return this.positionCount;
	}

	/**
	 * Method setPositionCount.
	 * 
	 * @param positionCount
	 *            Integer
	 */
	public void setPositionCount(Integer positionCount) {
		this.positionCount = positionCount;
	}

	/**
	 * Method getTradestrategyCount.
	 * 
	 * @return Integer
	 */
	@Column(name = "tradestrategyCount")
	public Integer getTradestrategyCount() {
		return this.tradestrategyCount;
	}

	/**
	 * Method setTradestrategyCount.
	 * 
	 * @param tradestrategyCount
	 *            Integer
	 */
	public void setTradestrategyCount(Integer tradestrategyCount) {
		this.tradestrategyCount = tradestrategyCount;
	}

	/**
	 * Method getSQLString.
	 * 
	 * @return String
	 */
	public static String getSQLString() {
		String sql = null;
		try {
			sql = ConfigProperties.readFileAsString("org/trade/persistent/dao/sql/TradelogSummary.sql",
					Thread.currentThread().getContextClassLoader());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sql;
	}
}
