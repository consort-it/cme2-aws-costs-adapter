package de.consortit.application.cost;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Cost {

    private String resourceGroup;

    private Integer month;

    private Integer year;

    private boolean estimated;

    private Price awsCosts;
}
