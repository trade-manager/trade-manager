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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.trade.core.dao.Aspect;
import org.trade.core.properties.ConfigProperties;

/**
 */
@Entity
@SqlResultSetMappings({ @SqlResultSetMapping(name = "TradelogDetailMapping", entities = @EntityResult(entityClass = TradelogDetail.class)) })
public class TradelogDetail extends Aspect implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -832064631322873796L;

	private Integer id;
	private Integer idTradestrategy;
	private String open;
	private String marketBias;
	private String marketBar;
	private String name;
	private String symbol;
	private String longShort;
	private String tier;
	private String status;
	private String side;
	private String action;
	private BigDecimal stopPrice;
	private String orderStatus;
	private Date filledDate;
	private Integer quantity;
	private BigDecimal averageFilledPrice;
	private BigDecimal commission;
	private BigDecimal profitLoss;

	public TradelogDetail() {
	}

	/**
	 * Constructor for TradelogDetail.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 * @param open
	 *            String
	 * @param marketBias
	 *            String
	 * @param marketBar
	 *            String
	 * @param name
	 *            String
	 * @param symbol
	 *            String
	 * @param longShort
	 *            String
	 * @param tier
	 *            String
	 * @param status
	 *            String
	 * @param side
	 *            String
	 * @param action
	 *            String
	 * @param stopPrice
	 *            BigDecimal
	 * @param orderStatus
	 *            String
	 * @param filledDate
	 *            Date
	 * @param quantity
	 *            Integer
	 * @param averageFilledPrice
	 *            BigDecimal
	 * @param commission
	 *            BigDecimal
	 * @param profitLoss
	 *            BigDecimal
	 */
	public TradelogDetail(Integer idTradestrategy, String open,
			String marketBias, String marketBar, String name, String symbol,
			String longShort, String tier, String status, String side,
			String action, BigDecimal stopPrice, String orderStatus,
			Date filledDate, Integer quantity, BigDecimal averageFilledPrice,
			BigDecimal commission, BigDecimal profitLoss) {
		this.idTradestrategy = idTradestrategy;
		this.open = open;
		this.marketBias = marketBias;
		this.marketBar = marketBar;
		this.name = name;
		this.symbol = symbol;
		this.longShort = longShort;
		this.tier = tier;
		this.status = status;
		this.side = side;
		this.action = action;
		this.stopPrice = stopPrice;
		this.orderStatus = orderStatus;
		this.filledDate = filledDate;
		this.quantity = quantity;
		this.averageFilledPrice = averageFilledPrice;
		this.commission = commission;
		this.profitLoss = profitLoss;

	}

	/**
	 * Method getIdTradelogDetail.
	 * 
	 * @return Integer
	 */
	@Id
	@Column(name = "idTradelogDetail")
	public Integer getIdTradelogDetail() {
		return this.id;
	}

	/**
	 * Method setIdTradelogDetail.
	 * 
	 * @param idTradelogDetail
	 *            Integer
	 */
	public void setIdTradelogDetail(Integer idTradelogDetail) {
		this.id = idTradelogDetail;
	}

	/**
	 * Method getIdTradestrategy.
	 * 
	 * @return Integer
	 */
	@Column(name = "idTradestrategy")
	public Integer getIdTradestrategy() {
		return this.idTradestrategy;
	}

	/**
	 * Method setIdTradestrategy.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 */
	public void setIdTradestrategy(Integer idTradestrategy) {
		this.idTradestrategy = idTradestrategy;
	}

	/**
	 * Method getOpen.
	 * 
	 * @return String
	 */
	@Column(name = "open", length = 19)
	public String getOpen() {
		return this.open;
	}

	/**
	 * Method setOpen.
	 * 
	 * @param open
	 *            String
	 */
	public void setOpen(String open) {
		this.open = open;
	}

	/**
	 * Method getMarketBias.
	 * 
	 * @return String
	 */
	@Column(name = "marketBias", length = 10)
	public String getMarketBias() {
		return this.marketBias;
	}

	/**
	 * Method setMarketBias.
	 * 
	 * @param marketBias
	 *            String
	 */
	public void setMarketBias(String marketBias) {
		this.marketBias = marketBias;
	}

	/**
	 * Method getMarketBar.
	 * 
	 * @return String
	 */
	@Column(name = "marketBar", length = 10)
	public String getMarketBar() {
		return this.marketBar;
	}

	/**
	 * Method setMarketBar.
	 * 
	 * @param marketBar
	 *            String
	 */
	public void setMarketBar(String marketBar) {
		this.marketBar = marketBar;
	}

	/**
	 * Method getName.
	 * 
	 * @return String
	 */
	@Column(name = "name", length = 20)
	public String getName() {
		return this.name;
	}

	/**
	 * Method setName.
	 * 
	 * @param name
	 *            String
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Method getSymbol.
	 * 
	 * @return String
	 */
	@Column(name = "symbol", length = 10)
	public String getSymbol() {
		return this.symbol;
	}

	/**
	 * Method setSymbol.
	 * 
	 * @param symbol
	 *            String
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * Method getLongShort.
	 * 
	 * @return String
	 */
	@Column(name = "longShort", length = 6)
	public String getLongShort() {
		return this.longShort;
	}

	/**
	 * Method setLongShort.
	 * 
	 * @param longShort
	 *            String
	 */
	public void setLongShort(String longShort) {
		this.longShort = longShort;
	}

	/**
	 * Method getTier.
	 * 
	 * @return String
	 */
	@Column(name = "tier", length = 1)
	public String getTier() {
		return this.tier;
	}

	/**
	 * Method setTier.
	 * 
	 * @param tier
	 *            String
	 */
	public void setTier(String tier) {
		this.tier = tier;
	}

	/**
	 * Method getStatus.
	 * 
	 * @return String
	 */
	@Column(name = "status", length = 10)
	public String getStatus() {
		return this.status;
	}

	/**
	 * Method setStatus.
	 * 
	 * @param status
	 *            String
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Method getSide.
	 * 
	 * @return String
	 */
	@Column(name = "side", nullable = false, length = 3)
	public String getSide() {
		return this.side;
	}

	/**
	 * Method setSide.
	 * 
	 * @param side
	 *            String
	 */
	public void setSide(String side) {
		this.side = side;
	}

	/**
	 * Method getAction.
	 * 
	 * @return String
	 */
	@Column(name = "action", length = 6)
	public String getAction() {
		return this.action;
	}

	/**
	 * Method setAction.
	 * 
	 * @param action
	 *            String
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * Method getStopPrice.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "stopPrice", precision = 10)
	public BigDecimal getStopPrice() {
		return this.stopPrice;
	}

	/**
	 * Method setStopPrice.
	 * 
	 * @param stopPrice
	 *            BigDecimal
	 */
	public void setStopPrice(BigDecimal stopPrice) {
		this.stopPrice = stopPrice;
	}

	/**
	 * Method getOrderStatus.
	 * 
	 * @return String
	 */
	@Column(name = "orderStatus", length = 45)
	public String getOrderStatus() {
		return this.orderStatus;
	}

	/**
	 * Method setOrderStatus.
	 * 
	 * @param orderStatus
	 *            String
	 */
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	/**
	 * Method getFilledDate.
	 * 
	 * @return Date
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "filledDate", length = 19)
	public Date getFilledDate() {
		return this.filledDate;
	}

	/**
	 * Method setFilledDate.
	 * 
	 * @param filledDate
	 *            Date
	 */
	public void setFilledDate(Date filledDate) {
		this.filledDate = filledDate;
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
	 * Method getAverageFilledPrice.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "averageFilledPrice", precision = 10)
	public BigDecimal getAverageFilledPrice() {
		return this.averageFilledPrice;
	}

	/**
	 * Method setAverageFilledPrice.
	 * 
	 * @param averageFilledPrice
	 *            BigDecimal
	 */
	public void setAverageFilledPrice(BigDecimal averageFilledPrice) {
		this.averageFilledPrice = averageFilledPrice;
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
	 * Method getProfitLoss.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "profitLoss", precision = 10)
	public BigDecimal getProfitLoss() {
		return this.profitLoss;
	}

	/**
	 * Method setProfitLoss.
	 * 
	 * @param profitLoss
	 *            BigDecimal
	 */
	public void setProfitLoss(BigDecimal profitLoss) {
		this.profitLoss = profitLoss;
	}

	/**
	 * Method getSQLString.
	 * 
	 * @return String
	 */
	public static String getSQLString() {
		String sql = null;
		try {
			sql = ConfigProperties.readFileAsString(
					"org/trade/persistent/dao/sql/TradelogDetail.sql", Thread
							.currentThread().getContextClassLoader());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sql;
	}
}
