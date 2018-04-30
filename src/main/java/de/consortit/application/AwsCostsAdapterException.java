package de.consortit.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.consortit.application.restmodel.Errors;
import lombok.Getter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@JsonIgnoreProperties({"cause", "stackTrace", "localizedMessage", "suppressed"})
public class AwsCostsAdapterException extends Exception {

    private final int status;

    private final String message;

    private final String location;

    private final String time;

    private final StackTraceElement[] trace;

    public AwsCostsAdapterException(Errors err, String location) {
        super(err.getMessage());
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC").normalized());
        status = err.getStatus();
        message = err.getMessage();
        this.location = location;
        trace = this.getStackTrace();
        time = zonedDateTime.toString();
    }

    public AwsCostsAdapterException(Errors err, String message, String location) {
        super(err.getMessage());
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC").normalized());
        status = err.getStatus();
        this.message = message;
        this.location = location;
        trace = this.getStackTrace();
        time = zonedDateTime.toString();
    }

}
