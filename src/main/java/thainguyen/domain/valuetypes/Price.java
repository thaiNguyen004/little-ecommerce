package thainguyen.domain.valuetypes;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Currency;

@Embeddable
@Data
public class Price {
    private BigDecimal value;
    private Currency currency;

    public Price() {
    }

    public Price(BigDecimal value, Currency currency) {
        this.value = value;
        this.currency = currency;
    }

    public static Price fromString(String priceStr) throws Exception {
        String[] split = priceStr.split(" ");
        Price priceConverted = new Price();
        priceConverted.setValue(new BigDecimal(split[0]));
        priceConverted.setCurrency(Currency.getInstance(split[1]));
        return priceConverted;
    }

    public String toString() {
        return value + " " + currency;
    }
}
