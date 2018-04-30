package de.consortit.application.cost;

import de.consortit.application.RequestStub;
import lombok.SneakyThrows;
import org.junit.Test;

import static de.consortit.application.cost.CostPayload.TAG_VALUE;
import static de.consortit.application.cost.CostPayload.getAwsCostPayload;
import static de.consortit.application.cost.CostType.UNBLENDED_COST;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CostPayloadTest {

    @Test
    @SneakyThrows
    public void should_build_cost_payload() {

        // GIVEN
        RequestStub requestStub = new RequestStub();

        // WHEN
        CostPayload costPayload = getAwsCostPayload(requestStub, UNBLENDED_COST);

        // THEN
        assertThat(costPayload.getStartDate()).isNotNull();
        assertThat(costPayload.getEndDate()).isNotNull();
        assertThat(costPayload.getTagKey()).isNotNull();
        assertThat(costPayload.getCostType()).isEqualTo(UNBLENDED_COST.getName());
    }

    @Test
    @SneakyThrows
    public void should_set_correct_month_values() {

        // GIVEN
        RequestStub requestStub = new RequestStub();
        requestStub.setParams("month", "0");
        requestStub.setParams("year", "2018");

        // WHEN
        CostPayload costPayload = getAwsCostPayload(requestStub, UNBLENDED_COST);

        // THEN
        assertThat(costPayload.getStartDate()).isEqualTo("2018-01-01");
        assertThat(costPayload.getEndDate()).isEqualTo("2018-02-01");
    }

    @Test
    @SneakyThrows
    public void should_calculate_correct_date_for_switch_to_next_year() {

        // GIVEN
        RequestStub requestStub = new RequestStub();
        requestStub.setParams("month", "11");
        requestStub.setParams("year", "2017");

        // WHEN
        CostPayload costPayload = getAwsCostPayload(requestStub, UNBLENDED_COST);

        // THEN
        assertThat(costPayload.getStartDate()).isEqualTo("2017-12-01");
        assertThat(costPayload.getEndDate()).isEqualTo("2018-01-01");
    }

    @Test
    public void should_get_tag() {

        // GIVEN
        RequestStub requestStub = new RequestStub();

        // WHEN
        String tagValue = CostPayload.getTagValue(requestStub);

        // THEN
        assertThat(tagValue).isEqualTo("data");
    }

    @Test
    public void should_get_default_tag() {

        // GIVEN
        RequestStub requestStub = new RequestStub();
        requestStub.setParams("tag", null);

        // WHEN
        String tagValue = CostPayload.getTagValue(requestStub);

        // THEN
        assertThat(tagValue).isEqualTo(TAG_VALUE);

    }
}
