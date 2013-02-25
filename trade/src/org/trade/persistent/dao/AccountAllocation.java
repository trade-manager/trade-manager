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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.trade.core.dao.Aspect;

/**
 */
@Entity
@Table(name = "accountallocation")
public class AccountAllocation extends Aspect implements java.io.Serializable {

	private static final long serialVersionUID = 2273276207080568947L;

	private String accountNumber;
	private BigDecimal amount;
	private String posEff;
	private FinancialAccount financialAccount;

	public AccountAllocation() {
	}

	/**
	 * Constructor for AccountAllocation.
	 * 
	 * @param accountNumber
	 *            String
	 * @param amount
	 *            BigDecimal
	 * @param posEff
	 *            String
	 */
	public AccountAllocation(FinancialAccount financialAccount,
			String accountNumber, BigDecimal amount, String posEff) {
		this.financialAccount = financialAccount;
		this.accountNumber = accountNumber;
		this.amount = amount;
		this.posEff = posEff;
	}

	/**
	 * Method getIdAccountAllocation.
	 * 
	 * @return Integer
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "idAccountAllocation", unique = true, nullable = false)
	public Integer getIdAccountAllocation() {
		return this.id;
	}

	/**
	 * Method setIdAccountAllocation.
	 * 
	 * @param idAccountAllocation
	 *            Integer
	 */
	public void setIdAccountAllocation(Integer idAccountAllocation) {
		this.id = idAccountAllocation;
	}

	/**
	 * Method getAccountNumber.
	 * 
	 * @return String
	 */
	@Column(name = "accountNumber", unique = true, nullable = false, length = 20)
	public String getAccountNumber() {
		return this.accountNumber;
	}

	/**
	 * Method setAccountNumber.
	 * 
	 * @param accountNumber
	 *            String
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	/**
	 * Method getAmount.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "amount", precision = 10)
	public BigDecimal getAmount() {
		return this.amount;
	}

	/**
	 * Method setAmount.
	 * 
	 * @param amount
	 *            BigDecimal
	 */
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	/**
	 * Method getPosEff.
	 * 
	 * @return String
	 */
	@Column(name = "posEff", length = 10)
	public String getPosEff() {
		return this.posEff;
	}

	/**
	 * Method setPosEff.
	 * 
	 * @param posEff
	 *            String
	 */
	public void setPosEff(String posEff) {
		this.posEff = posEff;
	}

	/**
	 * Method getFinancialAccount.
	 * 
	 * @return FinancialAccount
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idFinancialAccount", insertable = true, updatable = true, nullable = false)
	public FinancialAccount getFinancialAccount() {
		return this.financialAccount;
	}

	/**
	 * Method setFinancialAccount.
	 * 
	 * @param financialAccount
	 *            FinancialAccount
	 */
	public void setFinancialAccount(FinancialAccount financialAccount) {
		this.financialAccount = financialAccount;
	}

}
