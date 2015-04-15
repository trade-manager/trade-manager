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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.trade.core.dao.Aspect;
import org.trade.strategy.data.IndicatorSeries;

/**
 */
@Entity
@Table(name = "codevalue")
public class CodeValue extends Aspect implements java.io.Serializable {

	private static final long serialVersionUID = 2273276207080568947L;

	private String codeValue;
	@NotNull
	private CodeAttribute codeAttribute;
	private IndicatorSeries indicatorSeries;
	private Tradestrategy tradestrategy;

	public CodeValue() {
	}

	/**
	 * Constructor for CodeValue.
	 * 
	 * @param codeAttribute
	 *            CodeAttribute
	 * @param codeValue
	 *            String
	 */
	public CodeValue(CodeAttribute codeAttribute, String codeValue) {
		this.codeValue = codeValue;
		this.codeAttribute = codeAttribute;
	}

	/**
	 * Constructor for CodeValue.
	 * 
	 * @param codeAttribute
	 *            CodeAttribute
	 * @param codeValue
	 *            String
	 * @param indicatorSeries
	 *            IndicatorSeries
	 */
	public CodeValue(CodeAttribute codeAttribute, String codeValue,
			IndicatorSeries indicatorSeries) {
		this.codeValue = codeValue;
		this.codeAttribute = codeAttribute;
		this.indicatorSeries = indicatorSeries;
	}

	/**
	 * Constructor for CodeValue.
	 * 
	 * @param codeAttribute
	 *            CodeAttribute
	 * @param codeValue
	 *            String
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public CodeValue(CodeAttribute codeAttribute, String codeValue,
			Tradestrategy tradestrategy) {
		this.codeValue = codeValue;
		this.codeAttribute = codeAttribute;
		this.tradestrategy = tradestrategy;
	}

	/**
	 * Method getIdCodeValue.
	 * 
	 * @return Integer
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "idCodeValue", unique = true, nullable = false)
	public Integer getIdCodeValue() {
		return this.id;
	}

	/**
	 * Method setIdCodeValue.
	 * 
	 * @param idCodeValue
	 *            Integer
	 */
	public void setIdCodeValue(Integer idCodeValue) {
		this.id = idCodeValue;
	}

	/**
	 * Method getCodeValue.
	 * 
	 * @return String
	 */
	@Column(name = "codeValue", nullable = false, length = 45)
	public String getCodeValue() {
		return this.codeValue;
	}

	/**
	 * Method setCodeValue.
	 * 
	 * @param codeValue
	 *            String
	 */
	public void setCodeValue(String codeValue) {
		this.codeValue = codeValue;
	}

	/**
	 * Method getCodeAttribute.
	 * 
	 * @return CodeAttribute
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idCodeAttribute", nullable = false)
	public CodeAttribute getCodeAttribute() {
		return this.codeAttribute;
	}

	/**
	 * Method setCodeAttribute.
	 * 
	 * @param codeAttribute
	 *            CodeAttribute
	 */
	public void setCodeAttribute(CodeAttribute codeAttribute) {
		this.codeAttribute = codeAttribute;
	}

	/**
	 * Method getIndicatorSeries.
	 * 
	 * @return IndicatorSeries
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idIndicatorSeries", nullable = true)
	public IndicatorSeries getIndicatorSeries() {
		return this.indicatorSeries;
	}

	/**
	 * Method setIndicatorSeries.
	 * 
	 * @param indicatorSeries
	 *            IndicatorSeries
	 */
	public void setIndicatorSeries(IndicatorSeries indicatorSeries) {
		this.indicatorSeries = indicatorSeries;
	}

	/**
	 * Method getTradestrategy.
	 * 
	 * @return Tradestrategy
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idTradeStrategy", nullable = true)
	public Tradestrategy getTradestrategy() {
		return this.tradestrategy;
	}

	/**
	 * Method setTradestrategy.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void setTradestrategy(Tradestrategy tradestrategy) {
		this.tradestrategy = tradestrategy;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((codeAttribute == null) ? 0 : codeAttribute.hashCode());
		result = prime * result
				+ ((indicatorSeries == null) ? 0 : indicatorSeries.hashCode());
		result = prime * result
				+ ((tradestrategy == null) ? 0 : tradestrategy.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof CodeValue)) {
			return false;
		}
		CodeValue other = (CodeValue) obj;
		if (codeAttribute == null) {
			if (other.codeAttribute != null) {
				return false;
			}
		} else if (!codeAttribute.equals(other.codeAttribute)) {
			return false;
		}
		if (codeValue == null) {
			if (other.codeValue != null) {
				return false;
			}
		} else if (!codeValue.equals(other.codeValue)) {
			return false;
		}
		if (indicatorSeries == null) {
			if (other.indicatorSeries != null) {
				return false;
			}
		} else if (!indicatorSeries.equals(other.indicatorSeries)) {
			return false;
		}
		if (tradestrategy == null) {
			if (other.tradestrategy != null) {
				return false;
			}
		} else if (!tradestrategy.equals(other.tradestrategy)) {
			return false;
		}
		return true;
	}
}
