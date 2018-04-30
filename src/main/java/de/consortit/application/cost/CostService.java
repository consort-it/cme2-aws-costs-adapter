package de.consortit.application.cost;

import com.amazonaws.services.costexplorer.model.GetCostAndUsageResult;
import com.amazonaws.services.costexplorer.model.GetTagsResult;
import com.amazonaws.services.costexplorer.model.MetricValue;
import com.amazonaws.services.devicefarm.model.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.consortit.application.AwsCostsAdapterException;
import de.consortit.application.awsclient.AwsCostClient;
import de.consortit.application.restmodel.Errors;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static de.consortit.application.cost.CostPayload.getAwsCostPayload;
import static de.consortit.application.cost.CostType.UNBLENDED_COST;
import static java.util.stream.Collectors.toList;
import static spark.Spark.halt;

@Slf4j
public class CostService {

    public static String getAwsCosts(Request request, Response response) throws JsonProcessingException {

        String month = request.queryParams("month");
        String year = request.queryParams("year");
        checkValidQueryParams(month, year);

        GetCostAndUsageResult result = getAwsCostResult(request);

        return createAwsCostsResponse(response, result);
    }

    public static String getResourceGroups(Response response) throws JsonProcessingException {

        final ObjectMapper objectMapper = new ObjectMapper();
        response.type("application/json");

        GetTagsResult result = AwsCostClient.getGetTagsResult();

        return objectMapper.writeValueAsString(result.getTags());
    }

    private static GetCostAndUsageResult getAwsCostResult(Request request) {

        final CostPayload costPayload = getAwsCostPayload(request, UNBLENDED_COST);
        return AwsCostClient.getGetCostAndUsageResult(costPayload);
    }

    private static String createAwsCostsResponse(Response response, GetCostAndUsageResult result) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        response.type("application/json");

        return objectMapper.writeValueAsString(buildCostResponse(result));
    }

    static List<Cost> buildCostResponse(GetCostAndUsageResult result) {
        log.info("Create response payload with cost information.");

        return result.getResultsByTime().get(0).getGroups()
                .stream()
                .map(group -> Cost.builder()
                        .resourceGroup(group.getKeys().get(0))
                        .month(getMonthFromAWSDateInterval(result.getResultsByTime().get(0).getTimePeriod().getStart()))
                        .year(getYearFromAWSDateInterval(result.getResultsByTime().get(0).getTimePeriod().getStart()))
                        .estimated(result.getResultsByTime().get(0).getEstimated())
                        .awsCosts(getAwsCosts(group.getMetrics()))
                        .build()
                )
                .collect(toList());
    }

    private static Price getAwsCosts(Map<String, MetricValue> metrics) {

        final ObjectMapper objectMapper = new ObjectMapper();

        if (metrics.containsKey(UNBLENDED_COST.getName())) {
            return new Price(
                    new BigDecimal(metrics.get(UNBLENDED_COST.getName()).getAmount()),
                    CurrencyCode.fromValue(metrics.get(UNBLENDED_COST.getName()).getUnit())
            );
        } else {
            try {
                halt(400, objectMapper.writeValueAsString(
                        new AwsCostsAdapterException(Errors.ERR_BAD_REQUEST, "Create Response Payload."))
                );
            } catch (JsonProcessingException e) {
                log.error("Failed to create Error response.");
                throw new FailedToCreateErrorResponse(e.getMessage());
            }
        }
        return null;
    }

    private static Integer getMonthFromAWSDateInterval(String date) {

        LocalDate localDate = LocalDate.parse(date);
        return localDate.getMonth().getValue();
    }

    private static Integer getYearFromAWSDateInterval(String date) {

        LocalDate localDate = LocalDate.parse(date);
        return localDate.getYear();
    }

    static void checkValidQueryParams(String month, String year) throws JsonProcessingException {
        checkExistRequiredParams(month);
        checkExistRequiredParams(year);

        checkValidIntegerValue(month);
        checkValidIntegerValue(year);

        checkIsValidMonthValue(month);
        checkIsValidDate(month, year);
    }

    private static void checkExistRequiredParams(String value) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        if (value == null) {
            log.warn("Invalid request: Missing query parameter");
            halt(400, objectMapper.writeValueAsString(new AwsCostsAdapterException(Errors.ERR_BAD_REQUEST, "Invalid request: Missing query parameter", "Check required parameters.")));
        }
    }

    private static void checkValidIntegerValue(String value) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid request: Query parameter are no valid Integer values!");
            halt(400, objectMapper.writeValueAsString(new AwsCostsAdapterException(Errors.ERR_BAD_REQUEST, "Invalid request: Query parameter are no valid Integer values!", "Parse Query Params.")));
        }
    }

    private static void checkIsValidMonthValue(String value) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Integer month = Integer.parseInt(value);

        if ((month + 1) < 1 || (month + 1) > 12) {
            log.warn("Invalid request: Parameter 'month' is out of range! Expected 0-11, but was " + value);
            halt(400, objectMapper.writeValueAsString(new AwsCostsAdapterException(Errors.ERR_BAD_REQUEST, "Invalid request: Parameter 'month' is out of range! Expected 0-11, but was " + value, "Parse requested month.")));
        }
    }

    private static void checkIsValidDate(String month, String year) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Integer monthValue = Integer.parseInt(month);
        Integer yearValue = Integer.parseInt(year);

        LocalDate givenDate = LocalDate.of(yearValue, monthValue + 1, 1);
        LocalDate currentDate = LocalDate.now();

        if (currentDate.isBefore(givenDate)) {
            log.warn("Invalid request: Latest cost information is available up to " + currentDate.getMonth().getValue() + "-" + currentDate.getYear());
            halt(400, objectMapper.writeValueAsString(new AwsCostsAdapterException(Errors.ERR_BAD_REQUEST, "Latest cost information is available up to " + currentDate.getMonth().getValue() + "-" + currentDate.getYear(), "Parse requested date.")));
        }

        LocalDate dateWithOffset = currentDate.minusMonths(12);
        if (givenDate.isBefore(dateWithOffset)) {
            log.warn("Invalid request: Given information is older than 12 months.");
            halt(400, objectMapper.writeValueAsString(new AwsCostsAdapterException(Errors.ERR_BAD_REQUEST, "Invalid request: Given information is older than 12 months.", "Parse requested month.")));
        }
    }
}
