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
import org.trade.core.validator.PercentValidator;
import org.trade.core.validator.Validator;

/**
 */
public class Percent extends ValueType implements Comparator<Percent>, Comparable<Percent> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6356086072126179279L;

	public final static String PERCENT_POSITIVE_7_2 = "($)#(,)###(,)###(.##)";

	public final static String PERCENT_NONNEGATIVE_8_2 = "($)##(,)###(,)###(.##)";

	public final static String PERCENT_POSITIVE_10_2 = "($)#(,)###(,)###(,)###(.##)";

	public final static String PERCENT_NONNEGATIVE_11_2 = "($)##(,)###(,)###(,)###(.##)";

	public final static Percent ZERO = new Percent(0L, 0);

	protected static Boolean m_ascending = new Boolean(true);

	static {
		// Register the appropriate converters
		JavaTypeTranslator.registerDynamicTypeConverter(new ObjectToPercent());
		JavaTypeTranslator.registerDynamicTypeConverter(new PercentToObject());
	}

	//
	// Private Attributes
	//

	private BigDecimal m_value = null;

	private String m_format = PERCENT_NONNEGATIVE_11_2;

	private String m_invalidValue = null; // This will be null if there were

	// no conversion errors

	private final static int SCALE = 6;

	private final static String MULTIPLIER = "100";

	//
	// Public Methods
	//

	/**
	 * Default Constructor. Create an object and initialize it to empty.
	 */
	public Percent() {
	}

	/**
	 * Default Constructor. Create an object and initialize it to empty.
	 * 
	 * @param PercentString
	 *            String
	 */
	public Percent(String PercentString) {
		if ((null != PercentString) && (PercentString.length() != 0)) {
			// This is necessary because Java will parse strings with multiple
			// dashes
			if (PercentString.indexOf("-") != PercentString.lastIndexOf("-")) {
				m_invalidValue = PercentString;
			} else {
				try {
					setBigDecimal(new BigDecimal(PercentString));
				} catch (NumberFormatException e) {
					m_invalidValue = PercentString;
				}
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * 
	 * @param d
	 *            Integer
	 */
	public Percent(Integer d) {
		setBigDecimal(d);
	}

	/**
	 * Constructor
	 * 
	 * 
	 * @param d
	 *            double
	 */
	public Percent(double d) {
		setBigDecimal(new BigDecimal(d));
	}

	/**
	 * Constructor
	 * 
	 * 
	 * @param d
	 *            Double
	 */
	public Percent(Double d) {
		setBigDecimal(new BigDecimal(d.doubleValue()));
	}

	/**
	 * Constructor
	 * 
	 * 
	 * @param bd
	 *            BigDecimal
	 */
	public Percent(BigDecimal bd) {
		setBigDecimal(bd);
	}

	/**
	 * Constructor for Percent.
	 * 
	 * @param Percent
	 *            Percent
	 */
	public Percent(Percent Percent) {
		m_value = Percent.m_value;
		m_format = Percent.m_format;
		m_invalidValue = Percent.m_invalidValue;
	}

	/**
	 * Constructor
	 * 
	 * 
	 * 
	 * @param nonDecimalAmount
	 *            long
	 * @param decimalAmount
	 *            int
	 */
	public Percent(long nonDecimalAmount, int decimalAmount) {
		// Set up the default constraints for basic Percent values
		BigDecimal val = new BigDecimal((nonDecimalAmount * 100) + decimalAmount);
		setBigDecimal(val.movePointLeft(SCALE));
	}

	/**
	 * Provides the format used for determining if this object is valid. The
	 * format should be one of the format constants on this class. The default
	 * format is NORMAL_11_2.
	 * 
	 * @param format
	 *            String
	 */
	public void setFormat(String format) {
		m_format = format;
	}

	/**
	 * Method getFormat.
	 * 
	 * @return String
	 */
	public String getFormat() {
		return m_format;
	}

	/**
	 * This maximum length includes the decimal point and digits to both sides.
	 * 
	 * @return int
	 */
	public int getMaxLength() {
		int maxLength = 14;

		if (getFormat().equals(PERCENT_NONNEGATIVE_8_2)) {
			maxLength = 11;
		} else if (getFormat().equals(PERCENT_POSITIVE_10_2)) {
			maxLength = 13;
		} else if (getFormat().equals(PERCENT_POSITIVE_7_2)) {
			maxLength = 10;
		}

		return maxLength;
	}

	/**
	 * This indicates whether zero is an acceptable value for this instance.
	 * Currently this is determined by the format returned by getFormat().
	 * 
	 * @return boolean
	 */
	public boolean canBeZero() {
		boolean zero = true;

		if (getFormat().equals(PERCENT_POSITIVE_7_2)) {
			zero = false;
		} else if (getFormat().equals(PERCENT_POSITIVE_10_2)) {
			zero = false;
		}

		return zero;
	}

	/**
	 * This indicates whether zero is an acceptable value for this instance.
	 * Currently this is determined by the format returned by getFormat().
	 * 
	 * @return boolean
	 */
	public boolean canBeNegative() {
		boolean negative = false;

		// Currently all formats prohibit negative numbers.

		return negative;
	}

	/**
	 * Method isNegative.
	 * 
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
	 * 
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
	 * 
	 * @return The value before the decimal point in the Percent value.
	 */
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
	 * 
	 * @return Object
	 */
	public Object getSQLObject() {
		return (getBigDecimalValue());
	}

	/**
	 * 
	 * @return The value after the decimal point in the Percent value.
	 */
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
	 * 
	 * @return A BigDecimal representing the monetary value.
	 */
	public BigDecimal getBigDecimalValue() {
		assertDefined();

		return m_value;
	}

	/**
	 * Method toString.
	 * 
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
	 * 
	 * @param value
	 *            Object
	 * @throws ValueTypeException
	 */
	public void setValue(Object value) throws ValueTypeException {
		if (value instanceof Percent) {
			setBigDecimal(((Percent) value).m_value);
		} else {
			try {
				setBigDecimal(((Percent) JavaTypeTranslator.convert(Percent.class, value)).getBigDecimalValue());
			} catch (Exception ex) {
				throw new ValueTypeException(ex);
			}
		}
	}

	/**
	 * Adds two Percent objects
	 * 
	 * @param Percent
	 *            the Percent object to be added
	 * 
	 * 
	 * @return Percent the result
	 */
	public Percent add(Percent Percent) {
		assertDefined();

		if (null == m_value) {
			if (null == Percent.getBigDecimalValue()) {
				return new Percent();
			} else {
				return new Percent(Percent.getBigDecimalValue());
			}
		}

		BigDecimal value = m_value.add(Percent.getBigDecimalValue());
		return new Percent(value);
	}

	/**
	 * Subtracts two Percent objects
	 * 
	 * @param Percent
	 *            the Percent object to be subtracted
	 * 
	 * 
	 * @return Percent the result
	 */
	public Percent subtract(Percent Percent) {
		assertDefined();

		if (null == m_value) {
			return (Percent);
		}

		BigDecimal value = m_value.subtract(Percent.getBigDecimalValue());
		return new Percent(value);
	}

	/**
	 * Compares two Percent objects.
	 * 
	 * @param Percent
	 *            the Percent object to compare with.
	 * 
	 * 
	 * @return boolean result.
	 */
	public boolean isLessThen(Percent Percent) {
		assertDefined();

		BigDecimal thisValue = notNull(this);
		BigDecimal parameter = notNull(Percent);

		return (thisValue.compareTo(parameter) < 0);
	}

	/**
	 * Compares two Percent objects.
	 * 
	 * @param Percent
	 *            the Percent object to compare with.
	 * 
	 * 
	 * @return boolean result.
	 */
	public boolean isLessThenOrEqualTo(Percent Percent) {
		assertDefined();

		BigDecimal thisValue = notNull(this);
		BigDecimal parameter = notNull(Percent);

		return (thisValue.compareTo(parameter) <= 0);
	}

	/**
	 * Compares two Percent objects.
	 * 
	 * @param Percent
	 *            the Percent object to compare with.
	 * 
	 * 
	 * @return boolean result.
	 */
	public boolean isGreaterThen(Percent Percent) {
		assertDefined();

		BigDecimal thisValue = notNull(this);
		BigDecimal parameter = notNull(Percent);

		return (thisValue.compareTo(parameter) > 0);
	}

	/**
	 * Compares two Percent objects.
	 * 
	 * @param Percent
	 *            the Percent object to compare with.
	 * 
	 * 
	 * @return boolean result.
	 */
	public boolean isGreaterThenOrEqualTo(Percent Percent) {
		assertDefined();

		BigDecimal thisValue = notNull(this);
		BigDecimal parameter = notNull(Percent);

		return (thisValue.compareTo(parameter) >= 0);
	}

	/**
	 * Method isValid.
	 * 
	 * @param validator
	 *            Validator
	 * @param receiver
	 *            ExceptionMessageListener
	 * @return boolean
	 */
	public boolean isValid(Validator validator, ExceptionMessageListener receiver) {
		return validator.isValid(m_value, m_invalidValue, null, receiver);
	}

	/**
	 * Method getDefaultValidator.
	 * 
	 * @param messageFactory
	 *            IMessageFactory
	 * @param isMandatory
	 *            boolean
	 * @return Validator
	 */
	public Validator getDefaultValidator(IMessageFactory messageFactory, boolean isMandatory) {
		// This allow non-negative 11.2
		return new PercentValidator(messageFactory, false, true, 7, 5, isMandatory);
	}

	/**
	 * @deprecated Use the new validator method instead.
	 * @return boolean
	 */
	public boolean isValid() {
		boolean valid = false;

		String error = getError();
		if (null == error) {
			valid = true;
		}

		return valid;
	}

	/**
	 * @deprecated Use the new validator method instead.
	 * @return String
	 */
	public String getError() {
		String error = null;

		if (!isEmpty()) {
			long nonDecimalLength = Long.toString(getNonDecimalAmount()).length();

			// Note that the decimal length will be 1 for 00-09.
			long decimalLength = Long.toString(getDecimalAmount()).length();

			// Allow only 2 decimal places.
			if (decimalLength > 2) {
				error = "only two decimal places are allowed";
			}

			// Add three to account for the decimal portion and decimal point.
			if ((nonDecimalLength + 3) > getMaxLength()) {
				error = "length of digits and decimal point should not exceed " + getMaxLength();
			}

			// Disallow zero for certain formats
			if (!canBeZero() && (getBigDecimalValue().doubleValue() == 0)) {
				error = "amount cannot be zero";
			}

			// Disallow negative numbers
			if (!canBeNegative() && (getBigDecimalValue().doubleValue() < 0)) {
				error = "amount cannot be negative";
			}
		}

		return error;
	}

	/**
	 * Will throw a <code>NullPointerException</code> if this valuetype is
	 * empty.
	 * 
	 * 
	 * @return A double representing the monetary value.
	 */

	public double doubleValue() {
		assertDefined();
		return m_value.doubleValue();
	}

	/**
	 * Overrides Cloneable
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return Object
	 * @exception *
	 * 				@see
	 */

	public Object clone() {
		try {
			Percent other = (Percent) super.clone();
			return other;
		} catch (CloneNotSupportedException e) {
			// will never happen
			return null;
		}
	}

	/**
	 * Method compareTo.
	 * 
	 * @param other
	 *            Percent
	 * @return int
	 */
	public int compareTo(final Percent other) {
		return CoreUtils.nullSafeComparator(this.getBigDecimalValue(), other.getBigDecimalValue());
	}

	/**
	 * Method compare.
	 * 
	 * @param o1
	 *            Percent
	 * @param o2
	 *            Percent
	 * @return int
	 */
	public int compare(Percent o1, Percent o2) {
		int returnVal = CoreUtils.nullSafeComparator(o1.getBigDecimalValue(), o2.getBigDecimalValue());
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

		if (objectToCompare instanceof Percent) {
			if (CoreUtils.nullSafeComparator(((Percent) objectToCompare).getBigDecimalValue(),
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
	 * 
	 * @param value
	 *            Integer
	 */
	private void setBigDecimal(Integer value) {
		if (null == value) {
			m_value = new BigDecimal(0.0);
		} else {
			// m_value = value;
			m_value = (new BigDecimal(value)).setScale(SCALE, BigDecimal.ROUND_HALF_EVEN);
		}

		// Clear any invalid values
		m_invalidValue = null;
	}

	/**
	 * Method setBigDecimal.
	 * 
	 * @param value
	 *            BigDecimal
	 */
	private void setBigDecimal(BigDecimal value) {
		if (null == value) {
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
	 * 
	 * @param value
	 *            Percent
	 * @return BigDecimal
	 */
	private BigDecimal notNull(Percent value) {
		if (null == value) {
			return (new BigDecimal(0.0D));
		} else {
			return (value.getBigDecimalValue());
		}
	}

	private void assertDefined() {
		if (null != m_invalidValue) {
			throw new NumberFormatException(
					"Attempting to use a Percent that was not properly initialized.  Invalid value is: "
							+ m_invalidValue);
		}
	}
}
