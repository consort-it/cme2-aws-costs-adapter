package de.consortit.application.cost;

public class FailedToCreateErrorResponse extends RuntimeException {

    public FailedToCreateErrorResponse(String message) {
        super(message);
    }
}
