package io.github.boomkalakasha.icarus.scaffold.server;

import io.github.boomkalakasha.icarus.scaffold.core.exception.InvalidScaffoldRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Keeps validation failures predictable without echoing request contents. */
@RestControllerAdvice
public final class ScaffoldExceptionHandler {

    @ExceptionHandler({InvalidScaffoldRequestException.class, HttpMessageNotReadableException.class})
    ResponseEntity<Map<String, String>> invalidRequest(Exception ignored) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "invalid request"));
    }
}
