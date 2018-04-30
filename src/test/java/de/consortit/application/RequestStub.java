package de.consortit.application;

import spark.Request;

import java.util.HashMap;
import java.util.Map;

public class RequestStub extends Request {

    private Map<String, String> queryParamsMap;

    public RequestStub() {
        setParams();
    }

    public void setParams(String key, String value) {
        queryParamsMap.put(key, value);
    }

    private void setParams() {
        queryParamsMap = new HashMap<>();
        queryParamsMap.put("month", "3");
        queryParamsMap.put("year", "2018");
        queryParamsMap.put("tag", "data");
    }

    @Override
    public String queryParams(String queryParam) {
        return queryParamsMap.get(queryParam);
    }
}
