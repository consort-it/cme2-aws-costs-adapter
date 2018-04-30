package de.consortit.application.cost;

import com.amazonaws.services.devicefarm.model.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class Price {

    private BigDecimal amount;

    private CurrencyCode currency;
}
