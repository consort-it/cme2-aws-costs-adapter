package de.consortit.application.cost;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CostType {

    UNBLENDED_COST("UnblendedCost");

    private String name;
}
