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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.trade.core.dao.Aspect;
import org.trade.core.util.TradingCalendar;

/**
 */
@Entity
@Table(name = "portfolio")
public class Portfolio extends Aspect implements Serializable, Cloneable {

	private static final long serialVersionUID = 2273276207080568947L;

	private String name;
	private String alias;
	private String allocationMethod;
	private String description;
	private Boolean isDefault = new Boolean(false);
	private Date lastUpdateDate;
	private List<Tradestrategy> tradestrategies = new ArrayList<Tradestrategy>(
			0);
	private List<PortfolioAccount> portfolioAccounts = new ArrayList<PortfolioAccount>(
			0);

	public Portfolio() {
	}

	/**
	 * Constructor for Portfolio.
	 * 
	 * @param name
	 *            String
	 * @param description
	 *            String
	 */
	public Portfolio(String name, String description) {
		this.name = name;
		this.description = description;
		this.lastUpdateDate = TradingCalendar.getDate((new java.util.Date())
				.getTime());
	}

	/**
	 * Method getIdPortfolio.
	 * 
	 * @return Integer
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "idPortfolio", unique = true, nullable = false)
	public Integer getIdPortfolio() {
		return this.id;
	}

	/**
	 * Method setIdPortfolio.
	 * 
	 * @param idPortfolio
	 *            Integer
	 */
	public void setIdPortfolio(Integer idPortfolio) {
		this.id = idPortfolio;
	}

	/**
	 * Method getName.
	 * 
	 * @return String
	 */
	@Column(name = "name", nullable = false, length = 45)
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
	 * Method getAlias.
	 * 
	 * @return String
	 */
	@Column(name = "alias", unique = true, nullable = true, length = 45)
	public String getAlias() {
		return this.alias;
	}

	/**
	 * Method setAlias.
	 * 
	 * @param alias
	 *            String
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Method getAllocationMethod.
	 * 
	 * @return String
	 */
	@Column(name = "allocationMethod", nullable = false, length = 20)
	public String getAllocationMethod() {
		return this.allocationMethod;
	}

	/**
	 * Method setAllocationMethod.
	 * 
	 * @param allocationMethod
	 *            String
	 */
	public void setAllocationMethod(String allocationMethod) {
		this.allocationMethod = allocationMethod;
	}

	/**
	 * Method getDescription.
	 * 
	 * @return String
	 */
	@Column(name = "description", nullable = false, length = 240)
	public String getDescription() {
		return this.description;
	}

	/**
	 * Method setDescription.
	 * 
	 * @param description
	 *            String
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Method getIsDefault.
	 * 
	 * @return Boolean
	 */
	@Column(name = "isDefault", nullable = false)
	public Boolean getIsDefault() {
		return this.isDefault;
	}

	/**
	 * Method setIsDefault.
	 * 
	 * @param isDefault
	 *            Boolean
	 */
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * Method getLastUpdateDate.
	 * 
	 * @return Date
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastUpdateDate", nullable = false, length = 19)
	public Date getLastUpdateDate() {
		return this.lastUpdateDate;
	}

	/**
	 * Method setLastUpdateDate.
	 * 
	 * @param lastUpdateDate
	 *            Date
	 */
	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
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

	/**
	 * Method getTradestrategies.
	 * 
	 * @return List<Tradestrategy>
	 */
	@OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
	public List<Tradestrategy> getTradestrategies() {
		return this.tradestrategies;
	}

	/**
	 * Method setTradestrategies.
	 * 
	 * @param tradestrategies
	 *            List<Tradestrategy>
	 */
	public void setTradestrategies(List<Tradestrategy> tradestrategies) {
		this.tradestrategies = tradestrategies;
	}

	/**
	 * Method getPortfolioAccounts.
	 * 
	 * @return List<PortfolioAccounts>
	 */
	@OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY, orphanRemoval = true, cascade = { CascadeType.ALL })
	public List<PortfolioAccount> getPortfolioAccounts() {
		return this.portfolioAccounts;
	}

	/**
	 * Method setPortfolioAccounts.
	 * 
	 * @param codeAttributes
	 *            List<CodeAttribute>
	 */
	public void setPortfolioAccounts(List<PortfolioAccount> portfolioAccounts) {
		this.portfolioAccounts = portfolioAccounts;
	}

	/**
	 * Method getIndividualAccount.
	 * 
	 * @return account
	 */
	@Transient
	public Account getIndividualAccount() {
		if (this.getPortfolioAccounts().size() == 1) {
			return this.getPortfolioAccounts().get(0).getAccount();
		}
		return null;
	}

	/**
	 * Method isDirty.
	 * 
	 * @return boolean
	 */
	@Transient
	public boolean isDirty() {
		for (PortfolioAccount item : this.getPortfolioAccounts()) {
			if (item.isDirty())
				return true;
		}
		return super.isDirty();
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {

		Portfolio portfolio = (Portfolio) super.clone();
		List<Tradestrategy> tradestrategies = new ArrayList<Tradestrategy>(0);
		portfolio.setTradestrategies(tradestrategies);
		return portfolio;
	}
}
