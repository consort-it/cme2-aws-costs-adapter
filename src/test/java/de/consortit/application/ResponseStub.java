package de.consortit.application;

import lombok.NoArgsConstructor;
import spark.Response;

@NoArgsConstructor
public class ResponseStub extends Response {

    @Override
    public void type(String contentType) { }
}
