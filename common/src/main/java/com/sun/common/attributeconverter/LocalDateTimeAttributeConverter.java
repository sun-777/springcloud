package com.sun.common.attributeconverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @description: java.time.LocalDateTime与 java.sql.Timestamp转换器
 * @author: Sun Xiaodong
 */
@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, Timestamp> {
    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime localDateTime) {
        return null == localDateTime ? null : Timestamp.valueOf(localDateTime);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp dbTimestamp) {
        return null == dbTimestamp ? null : dbTimestamp.toLocalDateTime();
    }
}
