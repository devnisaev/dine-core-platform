package com.dinecore.order.web;

import com.dinecore.common.ApiError;
import com.dinecore.common.RequestHeaders;
import com.dinecore.order.error.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handle(ApiException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.status()).body(new ApiError(exception.code(), exception.getMessage(), traceId(request)));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> conflict(OptimisticLockingFailureException exception, HttpServletRequest request) {
        return ResponseEntity.status(409).body(new ApiError("CONFLICT", "Version conflict", traceId(request)));
    }

    private static String traceId(HttpServletRequest request) {
        String traceId = request.getHeader(RequestHeaders.REQUEST_ID);
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return traceId;
    }
}
