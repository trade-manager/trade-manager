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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.trade.core.dao.Aspect;

/**
 */
@Entity
@Table(name = "codeattribute")
public class CodeAttribute extends Aspect implements java.io.Serializable {

	private static final long serialVersionUID = 2273276207080568947L;

	@NotNull
	private String name;
	private String description;
	private String defaultValue;
	private String className;
	private String classEditorName;
	@NotNull
	private CodeType codeType;
	private Set<CodeValue> codeValues = new HashSet<>(0);

	public CodeAttribute() {
	}

	/**
	 * Constructor for CodeAttribute.
	 * 
	 * @param codeType
	 *            CodeType
	 * @param name
	 *            String
	 * @param description
	 *            String
	 * @param defaultValue
	 *            String
	 * @param className
	 *            String
	 * @param classEditorName
	 *            String
	 */
	public CodeAttribute(CodeType codeType, String name, String description,
			String defaultValue, String className, String classEditorName) {
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
		this.className = className;
		this.classEditorName = classEditorName;
		this.codeType = codeType;
	}

	/**
	 * Method getIdCodeAttribute.
	 * 
	 * @return Integer
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "idCodeAttribute", unique = true, nullable = false)
	public Integer getIdCodeAttribute() {
		return this.id;
	}

	/**
	 * Method setIdCodeAttribute.
	 * 
	 * @param idCodeAttribute
	 *            Integer
	 */
	public void setIdCodeAttribute(Integer idCodeAttribute) {
		this.id = idCodeAttribute;
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
	@Column(name = "description", nullable = true, length = 100)
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
	 * Method getDefaultValue.
	 * 
	 * @return String
	 */
	@Column(name = "defaultValue", nullable = true, length = 45)
	public String getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * Method setDefaultValue.
	 * 
	 * @param defaultValue
	 *            String
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Method getClassName.
	 * 
	 * @return String
	 */
	@Column(name = "className", nullable = false, length = 100)
	public String getClassName() {
		return this.className;
	}

	/**
	 * Method setClassName.
	 * 
	 * @param className
	 *            String
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Method getEditorClassName.
	 * 
	 * @return String
	 */
	@Column(name = "classEditorName", nullable = true, length = 100)
	public String getEditorClassName() {
		return this.classEditorName;
	}

	/**
	 * Method setEditorClassName.
	 * 
	 * @param classEditorName
	 *            String
	 */
	public void setEditorClassName(String classEditorName) {
		this.classEditorName = classEditorName;
	}

	/**
	 * Method getCodeType.
	 * 
	 * @return CodeType
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idCodeType", nullable = false)
	public CodeType getCodeType() {
		return this.codeType;
	}

	/**
	 * Method setCodeType.
	 * 
	 * @param codeType
	 *            CodeType
	 */
	public void setCodeType(CodeType codeType) {
		this.codeType = codeType;
	}

	/**
	 * Method getCodeValue.
	 * 
	 * @return Set<CodeValue>
	 */
	@OneToMany(mappedBy = "codeAttribute", fetch = FetchType.LAZY)
	public Set<CodeValue> getCodeValue() {
		return this.codeValues;
	}

	/**
	 * Method setCodeValue.
	 * 
	 * @param codeValues
	 *            Set<CodeValue>
	 */
	public void setCodeValue(Set<CodeValue> codeValues) {
		this.codeValues = codeValues;
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
