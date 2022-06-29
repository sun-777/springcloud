package com.sun.common.attributeconverter;

import com.sun.common.enumeration.Gender;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @description:
 * @author: Sun Xiaodong
 */
@Converter(autoApply = true)
public class GenderAttributeConverter implements AttributeConverter<Gender, Short> {
    @Override
    public Short convertToDatabaseColumn(Gender gender) {
        return null == gender ? null : gender.code();
    }

    @Override
    public Gender convertToEntityAttribute(Short dbGender) {
        return Gender.values()[0].codeOf(dbGender).orElse(null);
    }
}
