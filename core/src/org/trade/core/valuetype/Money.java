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
package org.trade.core.valuetype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

import org.trade.core.conversion.JavaTypeTranslator;
import org.trade.core.message.IMessageFactory;
import org.trade.core.util.CoreUtils;
import org.trade.core.validator.ExceptionMessageListener;
import org.trade.core.validator.DecimalValidator;
import org.trade.core.validator.Validator;

/**
 */
public class Money extends ValueType implements Comparator<Money>,
		Comparable<Money> {
	private static final long serialVersionUID = 4937298768811778585L;

	public final static String MONEY_POSITIVE_7_2 = "($)#(,)###(,)###(.##)";
	public final static String MONEY_NONNEGATIVE_8_2 = "($)##(,)###(,)###(.##)";
	public final static String MONEY_POSITIVE_10_2 = "($)#(,)###(,)###(,)###(.##)";
	public final static String MONEY_NONNEGATIVE_11_2 = "($)##(,)###(,)###(,)###(.##)";

	public final static Money ZERO = new Money(0L, 0);

	protected static Boolean m_ascending = new Boolean(true);

	static {
		// Register the appropriate converters
		JavaTypeTranslator.registerDynamicTypeConverter(new ObjectToMoney());
		JavaTypeTranslator.registerDynamicTypeConverter(new MoneyToObject());
	}

	private BigDecimal m_value = null;

	private String m_format = MONEY_NONNEGATIVE_11_2;

	private String m_invalidValue = null; // This will be null if there were

	// no conversion errors

	private final static int SCALE = 2;

	private final static String MULTIPLIER = "100";

	//
	// Public Methods
	//

	/**
	 * Default Constructor. Create an object and initialize it to empty.
	 */
	public Money() {
	}

	/**
	 * Default Constructor. Create an object and initialize it to empty.
	 * @param moneyString String
	 */
	public Money(String moneyString) {
		if ((null != moneyString) && (moneyString.length() != 0)) {
			// This is necessary because Java will parse strings with multiple
			// dashes
			if (moneyString.indexOf("-") != moneyString.lastIndexOf("-")) {
				m_invalidValue = moneyString;
			} else {
				try {
					setBigDecimal(new BigDecimal(moneyString));
				} catch (NumberFormatException e) {
					m_invalidValue = moneyString;
				}
			}
		}
	}

	/**
	 * Constructor
	 * 
	
	 * @param d double
	 */
	public Money(double d) {
		setBigDecimal(new BigDecimal(d));
	}

	/**
	 * Constructor
	 * 
	
	 * @param d Double
	 */
	public Money(Double d) {
		setBigDecimal(new BigDecimal(d.doubleValue()));
	}

	/**
	 * Constructor
	 * 
	
	 * @param bd BigDecimal
	 */
	public Money(BigDecimal bd) {
		setBigDecimal(bd);
	}

	/**
	 * Constructor for Money.
	 * @param money Money
	 */
	public Money(Money money) {
		m_value = money.m_value;
		m_format = money.m_format;
		m_invalidValue = money.m_invalidValue;
	}

	/**
	 * Constructor
	 * 
	
	
	 * @param nonDecimalAmount long
	 * @param decimalAmount int
	 */
	public Money(long nonDecimalAmount, int decimalAmount) {
		// Set up the default constraints for IP's basic Money values
		BigDecimal val = new BigDecimal((nonDecimalAmount * 100)
				+ decimalAmount);
		setBigDecimal(val.movePointLeft(SCALE));
	}

	/**
	 * Provides the format used for determining if this object is valid. The
	 * format should be one of the format constants on this class. The default
	 * format is NORMAL_11_2.
	 * @param format String
	 */
	public void setFormat(String format) {
		m_format = format;
	}

	/**
	 * Method getFormat.
	 * @return String
	 */
	public String getFormat() {
		return m_format;
	}

	/**
	 * This maximum length includes the decimal point and digits to both sides.
	 * @return int
	 */
	public int getMaxLength() {
		int maxLength = 14;

		if (getFormat().equals(MONEY_NONNEGATIVE_8_2)) {
			maxLength = 11;
		} else if (getFormat().equals(MONEY_POSITIVE_10_2)) {
			maxLength = 13;
		} else if (getFormat().equals(MONEY_POSITIVE_7_2)) {
			maxLength = 10;
		}

		return maxLength;
	}

	/**
	 * This indicates whether zero is an acceptable value for this instance.
	 * Currently this is determined by the format returned by getFormat().
	 * @return boolean
	 */
	public boolean canBeZero() {
		boolean zero = true;

		if (getFormat().equals(MONEY_POSITIVE_7_2)) {
			zero = false;
		} else if (getFormat().equals(MONEY_POSITIVE_10_2)) {
			zero = false;
		}

		return zero;
	}

	/**
	 * This indicates whether zero is an acceptable value for this instance.
	 * Currently this is determined by the format returned by getFormat().
	 * @return boolean
	 */
	public boolean canBeNegative() {
		boolean negative = false;

		// Currently all formats prohibit negative numbers.

		return negative;
	}

	/**
	 * Method isNegative.
	 * @return boolean
	 */
	public boolean isNegative() {
		assertDefined();

		boolean negative = false;

		if (m_value.compareTo(new BigDecimal(0)) < 0) {
			negative = true;
		}

		return negative;
	}

	/**
	 * Method isEmpty.
	 * @return boolean
	 */
	public boolean isEmpty() {
		boolean empty = false;

		if ((null == m_value) || (null != m_invalidValue)) {
			empty = true;
		}

		return empty;
	}

	/**
	
	 * @return The value before the decimal point in the money value. */
	public long getNonDecimalAmount() {
		assertDefined();

		long nonDecimalAmount = 0;

		if (null != m_value) {
			nonDecimalAmount = m_value.longValue();
		}

		return nonDecimalAmount;
	}

	/**
	 * See description of superclass method. Overrode functionality to return
	 * the BigDecimal this object is using intrnally.
	 * @return Object
	 */
	public Object getSQLObject() {
		return (getBigDecimalValue());
	}

	/**
	
	 * @return The value after the decimal point in the money value. */
	public int getDecimalAmount() {
		assertDefined();

		int decimalAmount = 0;

		if (null != m_value) {
			BigInteger tot = (m_value.movePointRight(SCALE)).toBigInteger();
			BigInteger sub = m_value.toBigInteger();
			sub = sub.multiply(new BigInteger(MULTIPLIER));

			BigInteger res = tot.subtract(sub);

			decimalAmount = res.intValue();
		}

		return decimalAmount;
	}

	/**
	 * Will throw a <code>NullPointerException</code> if this valuetype is
	 * empty.
	 * 
	
	 * @return A BigDecimal representing the monetary value. */
	public BigDecimal getBigDecimalValue() {
		assertDefined();

		return m_value;
	}

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		if (null != m_value) {
			return (m_value.toString());
		} else if (null != m_invalidValue) {
			return m_invalidValue;
		} else {
			return "";
		}
	}

	/**
	 * Method setValue.
	 * @param value Object
	 * @throws ValueTypeException
	 */
	public void setValue(Object value) throws ValueTypeException {
		if (value instanceof Money) {
			setBigDecimal(((Money) value).m_value);
		} else {
			try {
				setBigDecimal(((Money) JavaTypeTranslator.convert(Money.class,
						value)).getBigDecimalValue());
			} catch (Exception ex) {
				throw new ValueTypeException(ex);
			}
		}
	}

	/**
	 * Adds two Money objects
	 * 
	 * @param money
	 *            the Money object to be added
	 * 
	
	 * @return Money the result */
	public Money add(Money money) {
		assertDefined();

		if (null == m_value) {
			if (null == money.getBigDecimalValue()) {
				return new Money();
			} else {
				return new Money(money.getBigDecimalValue());
			}
		}

		BigDecimal value = m_value.add(money.getBigDecimalValue());
		return new Money(value);
	}

	/**
	 * Subtracts two Money objects
	 * 
	 * @param money
	 *            the Money object to be subtracted
	 * 
	
	 * @return Money the result */
	public Money subtract(Money money) {
		assertDefined();

		if (null == m_value) {
			return (money);
		}

		BigDecimal value = m_value.subtract(money.getBigDecimalValue());
		return new Money(value);
	}

	/**
	 * Compares two Money objects.
	 * 
	 * @param money
	 *            the Money object to compare with.
	 * 
	
	 * @return boolean result. */
	public boolean isLessThan(Money money) {
		assertDefined();

		BigDecimal thisValue = notNull(this);
		BigDecimal parameter = notNull(money);

		return (thisValue.compareTo(parameter) < 0);
	}

	/**
	 * Compares two Money objects.
	 * 
	 * @param money
	 *            the Money object to compare with.
	 * 
	
	 * @return boolean result. */
	public boolean isLessThanOrEqualTo(Money money) {
		assertDefined();

		BigDecimal thisValue = notNull(this);
		BigDecimal parameter = notNull(money);

		return (thisValue.compareTo(parameter) <= 0);
	}

	/**
	 * Compares two Money objects.
	 * 
	 * @param money
	 *            the Money object to compare with.
	 * 
	
	 * @return boolean result. */
	public boolean isGreaterThan(Money money) {
		assertDefined();

		BigDecimal thisValue = notNull(this);
		BigDecimal parameter = notNull(money);

		return (thisValue.compareTo(parameter) > 0);
	}

	/**
	 * Compares two Money objects.
	 * 
	 * @param money
	 *            the Money object to compare with.
	 * 
	
	 * @return boolean result. */
	public boolean isGreaterThanOrEqualTo(Money money) {
		assertDefined();

		BigDecimal thisValue = notNull(this);
		BigDecimal parameter = notNull(money);

		return (thisValue.compareTo(parameter) >= 0);
	}

	/**
	 * Method isValid.
	 * @param validator Validator
	 * @param receiver ExceptionMessageListener
	 * @return boolean
	 */
	public boolean isValid(Validator validator,
			ExceptionMessageListener receiver) {
		return validator.isValid(m_value, m_invalidValue, null, receiver);
	}

	/**
	 * Method getDefaultValidator.
	 * @param messageFactory IMessageFactory
	 * @param isMandatory boolean
	 * @return Validator
	 */
	public Validator getDefaultValidator(IMessageFactory messageFactory,
			boolean isMandatory) {
		// This allow non-negative 11.2
		return new DecimalValidator(messageFactory, false, true, 11, 2,
				isMandatory);
	}

	/**
	 * Will throw a <code>NullPointerException</code> if this valuetype is
	 * empty.
	 * 
	
	 * @return A double representing the monetary value. */

	public double doubleValue() {
		assertDefined();
		return m_value.doubleValue();
	}

	/**
	 * Overrides Cloneable
	 * 
	 * 
	
	 * 
	
	
	 * @return Object
	 * @exception * @see */

	public Object clone() {
		try {
			Money other = (Money) super.clone();
			return other;
		} catch (CloneNotSupportedException e) {
			// will never happen
			return null;
		}
	}

	/**
	 * Method compareTo.
	 * @param other Money
	 * @return int
	 */
	public int compareTo(final Money other) {
		return CoreUtils.nullSafeComparator(
				this.getBigDecimalValue(), other.getBigDecimalValue());
	}

	/**
	 * Method compare.
	 * @param o1 Money
	 * @param o2 Money
	 * @return int
	 */
	public int compare(Money o1, Money o2) {

		int returnVal = CoreUtils.nullSafeComparator(
				o1.getBigDecimalValue(), o2.getBigDecimalValue());
		if (m_ascending.equals(Boolean.FALSE)) {
			returnVal = returnVal * -1;
		}
		return returnVal;
	}
	
	/**
	 * Method equals.
	 * 
	 * @param objectToCompare
	 *            Object
	 * @return boolean
	 */
	public boolean equals(Object objectToCompare) {

		if (super.equals(objectToCompare))
			return true;

		if (objectToCompare instanceof Money) {
			if (CoreUtils.nullSafeComparator(
					((Money) objectToCompare).getBigDecimalValue(),
					this.getBigDecimalValue()) == 0)
				return true;
		}
		return false;
	}

	//
	// Private Methods
	//

	/**
	 * Method setBigDecimal.
	 * @param value BigDecimal
	 */
	private void setBigDecimal(BigDecimal value) {
		if (value == null) {
			m_value = new BigDecimal(0.0);
		} else {
			// m_value = value;
			m_value = value.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN);
		}

		// Clear any invalid values
		m_invalidValue = null;
	}

	/**
	 * Method notNull.
	 * @param value Money
	 * @return BigDecimal
	 */
	private BigDecimal notNull(Money value) {
		if (null == value) {
			return (new BigDecimal(0.0D));
		} else {
			return (value.getBigDecimalValue());
		}
	}

	private void assertDefined() {
		if (null != m_invalidValue) {
			throw new NumberFormatException(
					"Attempting to use a Money that was not properly initialized.  Invalid value is: "
							+ m_invalidValue);
		}
	}
}
