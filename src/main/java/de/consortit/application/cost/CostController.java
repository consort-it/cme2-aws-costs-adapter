package de.consortit.application.cost;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.consortit.application.AwsCostsAdapterException;
import de.consortit.application.restmodel.Errors;
import de.consortit.application.security.AuthorizationFilter;
import lombok.extern.slf4j.Slf4j;
import spark.Service;

import static spark.Service.ignite;

@Slf4j
public class CostController {

    private static final String BASE_URI = "/api/v1/aws-costs-adapter";

    private static final String AUTHORIZE_NAME = "scope";
    private static final String ROLE_ADMIN = "aws.cognito.signin.user.admin";

    public void initRoutes() {

        final Service http = ignite().port(8080);

        enableCORS(http, "*", "GET, POST", "Content-Type, Authorization");

        filterNotFoundAndInternalServerError(http);
        applyAuthorizationFilter(http);
        createRestApiRoutes(http);
    }

    private void filterNotFoundAndInternalServerError(Service http) {
        final ObjectMapper objectMapper = new ObjectMapper();
        http.notFound((req, res) -> objectMapper.writeValueAsString(new AwsCostsAdapterException(Errors.ERR_NOT_FOUND, "InitRoutes: Not Found.")));
        http.internalServerError((request, response) -> objectMapper.writeValueAsString(new AwsCostsAdapterException(Errors.ERR_UNKNOWN_ERROR, "InitRoutes: Unkown Error.")));
    }

    private void createRestApiRoutes(Service http) {
        http.get(BASE_URI + "/costs", CostService::getAwsCosts);
        http.get(BASE_URI + "/resource-groups", (req, res) -> CostService.getResourceGroups(res));
        http.get(BASE_URI + "/show-error", (req, res) -> {
            log.error("Logging an error!");
            return new ObjectMapper().writeValueAsString("ErrorResponse");
        });
    }

    private void applyAuthorizationFilter(Service http) {
        http.before(BASE_URI + "/costs", new AuthorizationFilter(AUTHORIZE_NAME, ROLE_ADMIN));
        http.before(BASE_URI + "/resource-groups", new AuthorizationFilter(AUTHORIZE_NAME, ROLE_ADMIN));
    }

    private static void enableCORS(final Service http, final String origin, final String methods, final String headers) {

        http.options("/*", (req, res) -> {

            final String acRequestHeaders = req.headers("Access-Control-Request-Headers");
            if (acRequestHeaders != null) {
                res.header("Access-Control-Allow-Headers", acRequestHeaders);
            }

            final String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        http.before((req, res) -> {
            res.header("Access-Control-Allow-Origin", origin);
            res.header("Access-Control-Request-Method", methods);
            res.header("Access-Control-Allow-Headers", headers);
            res.type("application/json");
            res.header("Server", "-");
        });
    }

}
