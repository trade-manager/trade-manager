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

import static javax.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Min;

import org.trade.core.dao.Aspect;

/**
 */
@Entity
@Table(name = "entrylimit")
public class Entrylimit extends Aspect implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8612117968275040016L;

	@Min(0)
	private BigDecimal startPrice;
	@Min(0)
	private BigDecimal endPrice;
	private BigDecimal limitAmount;
	private BigDecimal percentOfPrice;
	private BigDecimal percentOfMargin;
	private BigDecimal priceRound;
	private Integer shareRound;
	private BigDecimal pivotRange;

	public Entrylimit() {
	}

	/**
	 * Constructor for Entrylimit.
	 * 
	 * @param startPrice
	 *            BigDecimal
	 * @param endPrice
	 *            BigDecimal
	 * @param limitAmount
	 *            BigDecimal
	 */
	public Entrylimit(BigDecimal startPrice, BigDecimal endPrice,
			BigDecimal limitAmount) {
		this.startPrice = startPrice;
		this.endPrice = endPrice;
		this.limitAmount = limitAmount;
	}

	/**
	 * Constructor for Entrylimit.
	 * 
	 * @param startPrice
	 *            BigDecimal
	 * @param endPrice
	 *            BigDecimal
	 * @param limitAmount
	 *            BigDecimal
	 * @param percent
	 *            BigDecimal
	 * @param priceRound
	 *            BigDecimal
	 * @param shareRound
	 *            Integer
	 * @param pivotRange
	 *            BigDecimal
	 */
	public Entrylimit(BigDecimal startPrice, BigDecimal endPrice,
			BigDecimal limitAmount, BigDecimal percentOfPrice,
			BigDecimal priceRound, BigDecimal percentOfMargin,
			Integer shareRound, BigDecimal pivotRange) {
		this.startPrice = startPrice;
		this.endPrice = endPrice;
		this.limitAmount = limitAmount;
		this.percentOfPrice = percentOfPrice;
		this.percentOfMargin = percentOfMargin;
		this.pivotRange = pivotRange;
		this.priceRound = priceRound;
	}

	/**
	 * Method getIdEntryLimit.
	 * 
	 * @return Integer
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "idEntryLimit", unique = true, nullable = false)
	public Integer getIdEntryLimit() {
		return this.id;
	}

	/**
	 * Method setIdEntryLimit.
	 * 
	 * @param idEntryLimit
	 *            Integer
	 */
	public void setIdEntryLimit(Integer idEntryLimit) {
		this.id = idEntryLimit;
	}

	/**
	 * Method getStartPrice.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "startPrice", nullable = false, precision = 10)
	public BigDecimal getStartPrice() {
		return this.startPrice;
	}

	/**
	 * Method setStartPrice.
	 * 
	 * @param startPrice
	 *            BigDecimal
	 */
	public void setStartPrice(BigDecimal startPrice) {
		this.startPrice = startPrice;
	}

	/**
	 * Method getEndPrice.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "endPrice", nullable = false, precision = 10)
	public BigDecimal getEndPrice() {
		return this.endPrice;
	}

	/**
	 * Method setEndPrice.
	 * 
	 * @param endPrice
	 *            BigDecimal
	 */
	public void setEndPrice(BigDecimal endPrice) {
		this.endPrice = endPrice;
	}

	/**
	 * Method getLimitAmount.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "limitAmount", nullable = false, precision = 10)
	public BigDecimal getLimitAmount() {
		return this.limitAmount;
	}

	/**
	 * Method setLimitAmount.
	 * 
	 * @param limitAmount
	 *            BigDecimal
	 */
	public void setLimitAmount(BigDecimal limitAmount) {
		this.limitAmount = limitAmount;
	}

	/**
	 * Method getPercentOfPrice.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "percentOfPrice", precision = 10)
	public BigDecimal getPercentOfPrice() {
		return this.percentOfPrice;
	}

	/**
	 * Method setPercentOfPrice.
	 * 
	 * @param percentOfPrice
	 *            BigDecimal
	 */
	public void setPercentOfPrice(BigDecimal percentOfPrice) {
		this.percentOfPrice = percentOfPrice;
	}

	/**
	 * Method getPivotRange.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "pivotRange", precision = 10)
	public BigDecimal getPivotRange() {
		return this.pivotRange;
	}

	/**
	 * Method setPivotRange.
	 * 
	 * @param pivotRange
	 *            BigDecimal
	 */
	public void setPivotRange(BigDecimal pivotRange) {
		this.pivotRange = pivotRange;
	}

	/**
	 * Method getPercentOfMargin.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "percentOfMargin", precision = 10)
	public BigDecimal getPercentOfMargin() {
		return this.percentOfMargin;
	}

	/**
	 * Method setPercentOfMargin.
	 * 
	 * @param percentOfMargin
	 *            BigDecimal
	 */
	public void setPercentOfMargin(BigDecimal percentOfMargin) {
		this.percentOfMargin = percentOfMargin;
	}

	/**
	 * Method getPriceRound.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "priceRound", precision = 10)
	public BigDecimal getPriceRound() {
		return this.priceRound;
	}

	/**
	 * Method setPriceRound.
	 * 
	 * @param priceRound
	 *            BigDecimal
	 */
	public void setPriceRound(BigDecimal priceRound) {
		this.priceRound = priceRound;
	}

	/**
	 * Method getShareRound.
	 * 
	 * @return Integer
	 */
	@Column(name = "shareRound")
	public Integer getShareRound() {
		return this.shareRound;
	}

	/**
	 * Method setShareRound.
	 * 
	 * @param shareRound
	 *            Integer
	 */
	public void setShareRound(Integer shareRound) {
		this.shareRound = shareRound;
	}

	/**
	 * Method getVersion.
	 * 
	 * @return Integer
	 */
	@Version
	@Column(name = "version")
	public Integer getVersion() {
		return this.version;
	}

	/**
	 * Method setVersion.
	 * 
	 * @param version
	 *            Integer
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}
}
