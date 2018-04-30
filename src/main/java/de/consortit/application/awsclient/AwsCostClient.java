package de.consortit.application.awsclient;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.costexplorer.AWSCostExplorer;
import com.amazonaws.services.costexplorer.AWSCostExplorerClientBuilder;
import com.amazonaws.services.costexplorer.model.*;
import de.consortit.application.cost.CostPayload;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import static com.amazonaws.regions.Regions.US_EAST_1;

/**
 * The AwsCostClient connects to the AWS SDK to create requests against AWS.
 */
@Slf4j
public class AwsCostClient {

    private static final String TYPE = "DIMENSION";

    private static final String KEY = "SERVICE";

    private static GetCostAndUsageRequest getGetCostAndUsageRequest(CostPayload costPayload) {
        GetCostAndUsageRequest request = new GetCostAndUsageRequest();
        setGranularity(request);
        setTimePeriod(request, costPayload.getStartDate(), costPayload.getEndDate());
        setMetrics(request, costPayload.getCostType());
        setGroupByDefinition(request);

        return request;
    }

    private static AWSCostExplorer configureAwsCostExplorer() {

        AWSCredentials awsCredentials = new BasicAWSCredentials(
                System.getenv("ACCESS_KEY_ID"),
                System.getenv("SECRET_KEY")
        );

        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        return AWSCostExplorerClientBuilder.standard()
                .withRegion(US_EAST_1.getName())
                .withCredentials(awsCredentialsProvider)
                .build();
    }

    private static void setGroupByDefinition(final GetCostAndUsageRequest request) {

        Collection<GroupDefinition> groupByCollection = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();

        groupDefinition.setKey(KEY);
        groupDefinition.setType(TYPE);

        groupByCollection.add(groupDefinition);
        request.setGroupBy(groupByCollection);
    }

    private static void setMetrics(final GetCostAndUsageRequest request, String costsType) {

        Collection<String> metricsCollection = new ArrayList<>();
        metricsCollection.add(costsType);
        request.setMetrics(metricsCollection);
    }

    private static void setTimePeriod(final GetCostAndUsageRequest request, String startDate, String endDate) {

        DateInterval dateInterval = new DateInterval();
        dateInterval.setStart(startDate); // 2017-12-01
        dateInterval.setEnd(endDate); // 2018-01-01
        request.setTimePeriod(dateInterval);
    }

    private static void setGranularity(final GetCostAndUsageRequest request) {

        request.setGranularity(Granularity.MONTHLY.toString()); // MONTHLY or DAILY
    }

    public static GetCostAndUsageResult getGetCostAndUsageResult(CostPayload costPayload) {

        log.info("Configure and prepare cost request against AWS Cost Explorer API.");

        AWSCostExplorer explorer = AwsCostClient.configureAwsCostExplorer();
        GetCostAndUsageRequest request = AwsCostClient.getGetCostAndUsageRequest(costPayload);

        log.debug("Execute request ce:GetCostAndUsage");
        return explorer.getCostAndUsage(request);
    }

    public static GetTagsResult getGetTagsResult() {

        log.info("Configure and prepare tag request against AWS Cost Explorer API.");

        AWSCostExplorer explorer = AwsCostClient.configureAwsCostExplorer();
        GetTagsRequest request = new GetTagsRequest();

        DateInterval dateInterval = new DateInterval();
        LocalDate localDate = LocalDate.now();

        dateInterval.setStart(localDate.minusMonths(1).toString());
        dateInterval.setEnd(localDate.toString());
        request.setTimePeriod(dateInterval);

        return explorer.getTags(request);
    }

}
