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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 */
@Entity
@DiscriminatorValue("StrategyParameters")
public class StrategyParameters extends CodeType {

	private static final long serialVersionUID = 2273276207080568947L;

	public StrategyParameters(String name, String description) {
		super(name, CodeType.StrategyParameters, description);
	}

	public StrategyParameters() {
		super(CodeType.StrategyParameters);
	}

	/**
	 * Constructor for CodeType.
	 * 
	 * @param name
	 *            String
	 * @param type
	 *            String
	 * @param description
	 *            String
	 */
	public StrategyParameters(String name, String type, String description) {
		super(name, type, description);
	}
}
