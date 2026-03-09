// exception/RateLimitExceededException.java
package com.plantidentifier.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
@Getter
public class RateLimitExceededException extends RuntimeException {

    // Сколько осталось ждать до сброса (в секундах)
    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}