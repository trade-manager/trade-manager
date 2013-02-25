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
import org.trade.core.dao.Aspect;

/**
 */
@Entity
@Table(name = "financialaccount")
public class FinancialAccount extends Aspect implements java.io.Serializable {

	private static final long serialVersionUID = 2273276207080568947L;

	private String groupName;
	private String profileName;
	private String description;
	private String method;
	private Integer type;
	private BigDecimal percent;

	public FinancialAccount() {
	}

	/**
	 * Constructor for FinancialAccount.
	 * 
	 * @param groupName
	 *            String
	 * @param description
	 *            String
	 * @param method
	 *            String
	 */
	public FinancialAccount(String groupName, String description, String method) {
		this.groupName = groupName;
		this.description = description;
		this.method = method;
	}

	/**
	 * Constructor for FinancialAccount.
	 * 
	 * @param profileName
	 *            String
	 * @param description
	 *            String
	 */
	public FinancialAccount(String profileName, String description, Integer type) {
		this.profileName = profileName;
		this.description = description;
		this.type = type;
	}

	/**
	 * Method getIdFinancialAccount.
	 * 
	 * @return Integer
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "idFinancialAccount", unique = true, nullable = false)
	public Integer getIdFinancialAccount() {
		return this.id;
	}

	/**
	 * Method setIdFinancialAccount.
	 * 
	 * @param idFinancialAccount
	 *            Integer
	 */
	public void setIdFinancialAccount(Integer idFinancialAccount) {
		this.id = idFinancialAccount;
	}

	/**
	 * Method getGroupName.
	 * 
	 * @return String
	 */
	@Column(name = "groupName", nullable = false, length = 45)
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * Method setGroupName.
	 * 
	 * @param groupName
	 *            String
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * Method getProfileName.
	 * 
	 * @return String
	 */
	@Column(name = "profileName", nullable = false, length = 45)
	public String getProfileName() {
		return this.profileName;
	}

	/**
	 * Method setProfileName.
	 * 
	 * @param profileName
	 *            String
	 */
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	/**
	 * Method getDescription.
	 * 
	 * @return String
	 */
	@Column(name = "description", nullable = false, length = 100)
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
	 * Method getMethod.
	 * 
	 * @return String
	 */
	@Column(name = "method", length = 20)
	public String getMethod() {
		return this.method;
	}

	/**
	 * Method setMethod.
	 * 
	 * @param method
	 *            String
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Method getMethod.
	 * 
	 * @return Integer
	 */
	@Column(name = "type")
	public Integer getType() {
		return this.type;
	}

	/**
	 * Method setType.
	 * 
	 * @param type
	 *            Integer
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * Method getPercent.
	 * 
	 * @return BigDecimal
	 */
	@Column(name = "percent", precision = 10)
	public BigDecimal getPercent() {
		return this.percent;
	}

	/**
	 * Method setPercent.
	 * 
	 * @param percent
	 *            BigDecimal
	 */
	public void setPercent(BigDecimal percent) {
		this.percent = percent;
	}
}
