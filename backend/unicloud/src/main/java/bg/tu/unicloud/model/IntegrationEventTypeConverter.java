package bg.tu.unicloud.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class IntegrationEventTypeConverter implements AttributeConverter<IntegrationEventType, String> {

    @Override
    public String convertToDatabaseColumn(IntegrationEventType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public IntegrationEventType convertToEntityAttribute(String value) {
        return value == null ? null : IntegrationEventType.fromValue(value);
    }
}
