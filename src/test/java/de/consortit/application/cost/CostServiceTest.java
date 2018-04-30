package de.consortit.application.cost;

import com.amazonaws.services.costexplorer.model.*;
import de.consortit.application.RequestStub;
import de.consortit.application.ResponseStub;
import de.consortit.application.awsclient.AwsCostClient;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import spark.HaltException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.consortit.application.cost.CostService.*;
import static de.consortit.application.cost.CostType.UNBLENDED_COST;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AwsCostClient.class)
public class CostServiceTest {

    private RequestStub requestStub;

    private ResponseStub responseStub;

    private CostPayload costPayload;

    private GetCostAndUsageResult getCostAndUsageResult;

    private Map<String, MetricValue> metricsMap;

    private MetricValue metricValue;

    @Before
    public void before() {
        mockStatic(AwsCostClient.class);

        metricsMap = new HashMap<>();
        metricValue = new MetricValue();
        metricValue.setAmount("2.99");
        metricValue.setUnit("USD");
    }

    @Test
    @SneakyThrows
    public void should_build_cost_response() {

        // GIVEN
        givenCostPayload();
        givenCostAndUsageResult();

        // WHEN
        List<Cost> costs = buildCostResponse(getCostAndUsageResult);

        // THEN
        thenVerifyPayload(costs);
    }

    @Test
    @SneakyThrows
    public void should_get_aws_costs() {

        // GIVEN
        givenRequest();
        givenResponse();
        givenCostPayload();
        givenCostAndUsageResult();

        // WHEN
        when(AwsCostClient.getGetCostAndUsageResult(any())).thenReturn(getCostAndUsageResult);
        String response = getAwsCosts(requestStub, responseStub);

        // THEN
        thenVerifyCostsResponse(response);
    }

    @Test
    @SneakyThrows
    public void should_check_is_valid_data() {

        checkValidQueryParams("0", "2018");
        checkValidQueryParams("11", "2017");
    }

    @Test(expected = HaltException.class)
    @SneakyThrows
    public void should_fail_valid_check_with_missing_parameter() {

        checkValidQueryParams(null, "2018");
        checkValidQueryParams("11", null);
    }

    @Test(expected = HaltException.class)
    @SneakyThrows
    public void should_fail_valid_check_with_invalid_month_value() {

        checkValidQueryParams("12", "2018");
    }

    @Test(expected = HaltException.class)
    @SneakyThrows
    public void should_fail_valid_check_with_date_in_future() {

        checkValidQueryParams("11", "2118");
    }

    private void thenVerifyPayload(List<Cost> costList) {
        assertThat(costList.size()).isEqualTo(1);
        assertThat(costList.get(0).getResourceGroup()).isEqualTo("AWS Instances");
        assertThat(costList.get(0).isEstimated()).isTrue();
        assertThat(costList.get(0).getMonth()).isEqualTo(1);
        assertThat(costList.get(0).getYear()).isEqualTo(2018);
        assertThat(costList.get(0).getAwsCosts()).isNotNull();
    }

    private void thenVerifyCostsResponse(String response) {

        Arrays.asList(Cost.class.getDeclaredFields()).forEach(
                field -> assertThat(response.contains(field.getName()))
        );
    }

    private void givenCostPayload() {
        costPayload = CostPayload.builder()
                .startDate("2018-01-01")
                .endDate("2018-02-01")
                .tagKey("data")
                .build();
    }

    private void givenRequest() {
        requestStub = new RequestStub();
    }

    private void givenResponse() {
        responseStub = new ResponseStub();
    }

    private void givenCostAndUsageResult() {

        GetCostAndUsageResult result = new GetCostAndUsageResult();

        ResultByTime resultByTime = new ResultByTime();
        resultByTime.setEstimated(true);
        setTimeInterval(resultByTime);
        setGroup(resultByTime);

        result.setResultsByTime(Arrays.asList(resultByTime));
        setGroupDefinition(result);

        getCostAndUsageResult = result;
    }

    private void setGroupDefinition(GetCostAndUsageResult result) {
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setType("DIMENSION");
        groupDefinition.setKey("SERVICE");
        result.setGroupDefinitions(Arrays.asList(groupDefinition));
    }

    private void setGroup(ResultByTime resultByTime) {
        Group group = new Group();

        MetricValue metricValue = new MetricValue();
        metricValue.setAmount("2.99");
        metricValue.setUnit("USD");

        metricsMap.put(UNBLENDED_COST.getName(), metricValue);
        group.setMetrics(metricsMap);
        group.setKeys(Arrays.asList("AWS Instances"));
        resultByTime.setGroups(Arrays.asList(group));
    }

    private void setTimeInterval(ResultByTime resultByTime) {
        DateInterval dateInterval = new DateInterval();
        dateInterval.setStart(costPayload.getStartDate());
        dateInterval.setEnd(costPayload.getEndDate());
        resultByTime.setTimePeriod(dateInterval);
    }
}
