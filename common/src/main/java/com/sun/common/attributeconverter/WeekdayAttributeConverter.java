package com.sun.common.attributeconverter;

import com.sun.common.enumeration.Weekday;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @description:
 * @author: Sun Xiaodong
 */
@Converter(autoApply = true)
public class WeekdayAttributeConverter implements AttributeConverter<Weekday, Short> {
    @Override
    public Short convertToDatabaseColumn(Weekday weekday) {
        return null == weekday ? null : weekday.code();
    }

    @Override
    public Weekday convertToEntityAttribute(Short dbWeekday) {
        return Weekday.values()[0].codeOf(dbWeekday).orElse(null);
    }
}
