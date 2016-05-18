package org.trade.persistent.dao.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.trade.core.util.TradingCalendar;

@Converter(autoApply = true)
public class ZonedDateConverter implements AttributeConverter<LocalDate, Date> {

	public Date convertToDatabaseColumn(LocalDate date) {
		if (date == null) {
			return null;
		}
		final Instant instant = date.atStartOfDay().atZone(TradingCalendar.MKT_TIMEZONE).toInstant();
		return (Date) Date.from(instant);
	}

	public LocalDate convertToEntityAttribute(Date value) {
		if (value == null) {
			return null;
		}
		final Instant instant = Instant.ofEpochMilli(value.getTime());
		return ZonedDateTime.ofInstant(instant, TradingCalendar.MKT_TIMEZONE).toLocalDate();
	}
}