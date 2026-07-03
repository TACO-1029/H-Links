package com.hlinks.global.exception;

import com.hlinks.global.response.ErrorResponse;
import com.hlinks.global.response.code.BaseResponseCode;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/*
이 핸들러는 @RestControllerAdvice라서 JSON을 반환합니다.
즉 화면 요청에서 예외가 나도 JSON이 나올 수 있습니다.
지금은 API 공통 응답 기반을 먼저 잡는 것이고, 화면용 에러 페이지는 나중에 분리 가능합니다.
1. /api/** 예외 -> RestApiExceptionHandler
2. 화면 요청 예외 -> ViewExceptionHandler
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        if (isClientDisconnected(e)) {
            log.debug("Client disconnected while streaming response: {}", getRootMessage(e));
            return;
        }

        log.warn("Async request is no longer usable: {}", e.getMessage());
    }

    /*
    throw new BaseException.. 같은 애들이 예외핸들러 어노테이션이 붙어있는 곳으로 들어옵니다.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse<Void>> handleBaseException(BaseException e) {
        BaseResponseCode responseCode = e.getResponseCode();

        log.warn("BaseException: code={}, message={}", responseCode.getCode(), e.getMessage());

        ErrorResponse<Void> errorResponse = ErrorResponse.from(responseCode, e.getMessage());

        return ResponseEntity
                .status(responseCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(ErrorResponseCode.INVALID_REQUEST_BODY.getMessage());

        log.warn("MethodArgumentNotValidException: {}", message);

        ErrorResponse<Void> errorResponse = ErrorResponse.from(
                ErrorResponseCode.INVALID_REQUEST_BODY,
                message
        );

        return ResponseEntity
                .status(ErrorResponseCode.INVALID_REQUEST_BODY.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(ErrorResponseCode.INVALID_REQUEST_PARAMETER.getMessage());

        log.warn("BindException: {}", message);

        ErrorResponse<Void> errorResponse = ErrorResponse.from(
                ErrorResponseCode.INVALID_REQUEST_BODY,
                message
        );

        return ResponseEntity
                .status(ErrorResponseCode.INVALID_REQUEST_PARAMETER.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());

        ErrorResponse<Void> errorResponse = ErrorResponse.from(ErrorResponseCode.INVALID_REQUEST_BODY);

        return ResponseEntity
                .status(ErrorResponseCode.INVALID_REQUEST_BODY.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException: {}", e.getMessage());

        ErrorResponse<Void> errorResponse = ErrorResponse.from(ErrorResponseCode.INVALID_REQUEST_PARAMETER);

        return ResponseEntity
                .status(ErrorResponseCode.INVALID_REQUEST_PARAMETER.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("MissingServletRequestParameterException: {}", e.getMessage());

        ErrorResponse<Void> errorResponse = ErrorResponse.from(ErrorResponseCode.INVALID_REQUEST_PARAMETER);

        return ResponseEntity
                .status(ErrorResponseCode.INVALID_REQUEST_PARAMETER.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("HttpRequestMethodNotSupportedException: {}", e.getMessage());

        ErrorResponse<Void> errorResponse = ErrorResponse.from(ErrorResponseCode.METHOD_NOT_ALLOWED);

        return ResponseEntity
                .status(ErrorResponseCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("NoResourceFoundException: {}", e.getMessage());

        ErrorResponse<Void> errorResponse = ErrorResponse.from(ErrorResponseCode.NOT_FOUND_ENDPOINT);

        return ResponseEntity
                .status(ErrorResponseCode.NOT_FOUND_ENDPOINT.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception occurred", e);

        ErrorResponse<Void> errorResponse = ErrorResponse.from(ErrorResponseCode.INTERNAL_SERVER_ERROR);

        return ResponseEntity
                .status(ErrorResponseCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(errorResponse);
    }

    private boolean isClientDisconnected(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            String className = current.getClass().getName();
            String message = current.getMessage();

            if (className.contains("ClientAbortException") || containsClientDisconnectMessage(message)) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private boolean containsClientDisconnectMessage(String message) {
        if (message == null) {
            return false;
        }

        return message.contains("Broken pipe") || message.contains("Connection reset by peer");
    }

    private String getRootMessage(Throwable throwable) {
        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage();
    }
}
