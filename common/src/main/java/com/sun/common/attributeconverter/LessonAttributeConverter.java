package com.sun.common.attributeconverter;

import com.sun.common.enumeration.Lesson;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @description:
 * @author: Sun Xiaodong
 */
@Converter(autoApply = true)
public class LessonAttributeConverter implements AttributeConverter<Lesson, Short> {
    @Override
    public Short convertToDatabaseColumn(Lesson lesson) {
        return null == lesson ? null : lesson.code();
    }

    @Override
    public Lesson convertToEntityAttribute(Short dbLesson) {
        return Lesson.values()[0].codeOf(dbLesson).orElse(null);
    }
}
