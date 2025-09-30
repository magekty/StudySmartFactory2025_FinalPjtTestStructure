// src/main/java/com/globalmed/mes/mes_api/common/jpa/OffsetDateTimeUtcConverter.java
package com.globalmed.mes.mes_api.common.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Converter(autoApply = false)
public class OffsetDateTimeUtcConverter implements AttributeConverter<OffsetDateTime, Timestamp> {
    @Override
    public Timestamp convertToDatabaseColumn(OffsetDateTime attribute) {
        if (attribute == null) return null;
        return Timestamp.from(attribute.withOffsetSameInstant(ZoneOffset.UTC).toInstant());
    }
    @Override
    public OffsetDateTime convertToEntityAttribute(Timestamp dbData) {
        if (dbData == null) return null;
        return dbData.toInstant().atOffset(ZoneOffset.UTC);
    }
}