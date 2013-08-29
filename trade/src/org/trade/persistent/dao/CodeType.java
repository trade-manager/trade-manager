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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.trade.core.dao.Aspect;

/**
 */
@Entity
@Table(name = "codetype")
public class CodeType extends Aspect implements java.io.Serializable {

	private static final long serialVersionUID = 2273276207080568947L;

	private String name;
	private String description;
	private List<CodeAttribute> codeAttributes = new ArrayList<CodeAttribute>(0);

	public CodeType() {
	}

	/**
	 * Constructor for CodeType.
	 * 
	 * @param name
	 *            String
	 * @param description
	 *            String
	 */
	public CodeType(String name, String description) {

		this.name = name;
		this.description = description;
	}

	/**
	 * Method getIdCodeType.
	 * 
	 * @return Integer
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "idCodeType", unique = true, nullable = false)
	public Integer getIdCodeType() {
		return this.id;
	}

	/**
	 * Method setIdCodeType.
	 * 
	 * @param idCodeType
	 *            Integer
	 */
	public void setIdCodeType(Integer idCodeType) {
		this.id = idCodeType;
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
	 * Method getCodeAttribute.
	 * 
	 * @return List<CodeAttribute>
	 */
	@OneToMany(mappedBy = "codeType", fetch = FetchType.LAZY, orphanRemoval = true, cascade = { CascadeType.ALL })
	public List<CodeAttribute> getCodeAttribute() {
		return this.codeAttributes;
	}

	/**
	 * Method setCodeAttribute.
	 * 
	 * @param codeAttributes
	 *            List<CodeAttribute>
	 */
	public void setCodeAttribute(List<CodeAttribute> codeAttributes) {
		this.codeAttributes = codeAttributes;
	}

	/**
	 * Method isDirty.
	 * 
	 * @return boolean
	 */
	@Transient
	public boolean isDirty() {
		for (CodeAttribute item : this.getCodeAttribute()) {
			if (item.isDirty())
				return true;
		}
		return super.isDirty();
	}
}
