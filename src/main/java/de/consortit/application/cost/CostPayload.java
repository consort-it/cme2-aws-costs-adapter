package de.consortit.application.cost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import spark.Request;

import java.time.LocalDate;

@Slf4j
@Setter
@Getter
@Builder
@AllArgsConstructor
public class CostPayload {

    static final String TAG_VALUE = "code";

    private String startDate;

    private String endDate;

    private String tagKey;

    private String costType;

    static CostPayload getAwsCostPayload(final Request request, CostType costType) {

        Integer month = Integer.parseInt(request.queryParams("month"));
        Integer year = Integer.parseInt(request.queryParams("year"));

        return CostPayload.builder()
                .startDate(getStartDate(month, year))
                .endDate(getEndDate(month, year))
                .tagKey(getTagValue(request))
                .costType(costType.getName())
                .build();
    }

    static String getTagValue(Request request) {

        String tagValue = TAG_VALUE;
        if (request.queryParams("tag") != null) {
            tagValue = request.queryParams("tag");
        }
        return tagValue;
    }

    private static String getStartDate(Integer month, Integer year) {
        return LocalDate.of(year, month + 1, 1).toString();
    }

    private static String getEndDate(Integer month, Integer year) {
        LocalDate date = LocalDate.of(year, month + 1, 1);
        return date.plusMonths(1).toString();
    }
}
