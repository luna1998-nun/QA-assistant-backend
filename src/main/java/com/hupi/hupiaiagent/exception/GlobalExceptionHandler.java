package com.hupi.hupiaiagent.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 用于捕获和处理各种异常，特别是 400 Bad Request 错误
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.error("[400] Missing parameter: {} in request: {}", ex.getParameterName(), request.getRequestURI());
        log.error("[400] Request URL: {}", request.getRequestURL());
        log.error("[400] Query string: {}", request.getQueryString());
        
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Missing Parameter");
        error.put("message", "缺少必需参数: " + ex.getParameterName());
        error.put("path", request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.error("[400] Type mismatch for parameter: {} in request: {}", ex.getName(), request.getRequestURI());
        log.error("[400] Request URL: {}", request.getRequestURL());
        log.error("[400] Query string: {}", request.getQueryString());
        
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Type Mismatch");
        error.put("message", "参数类型不匹配: " + ex.getName() + ", 期望类型: " + ex.getRequiredType());
        error.put("path", request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.error("[400] Illegal argument in request: {}", request.getRequestURI());
        log.error("[400] Request URL: {}", request.getRequestURL());
        log.error("[400] Query string: {}", request.getQueryString());
        log.error("[400] Error message: {}", ex.getMessage(), ex);
        
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Illegal Argument");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 处理 404 错误
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NoHandlerFoundException ex, HttpServletRequest request) {
        log.error("[404] No handler found for: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Not Found");
        error.put("message", "未找到处理器: " + ex.getHttpMethod() + " " + ex.getRequestURL());
        error.put("path", request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("[500] Unexpected error in request: {}", request.getRequestURI(), ex);
        log.error("[500] Request URL: {}", request.getRequestURL());
        log.error("[500] Query string: {}", request.getQueryString());
        
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Internal Server Error");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

