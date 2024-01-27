package thainguyen.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import thainguyen.domain.valuetypes.Price;

@Converter(autoApply = true)
public class PriceConverter implements AttributeConverter<Price, String> {
    @Override
    public String convertToDatabaseColumn(Price price) {
        if (price == null) return null;
        return price.toString();
    }

    @Override
    public Price convertToEntityAttribute(String priceStr) {
        try {
            return Price.fromString(priceStr);
        } catch (Exception e) {
            return null;
        }
    }
}
