package com.factory_dynamics.erp.erp_server.common;

import org.springframework.http.HttpStatus;

public class BizException extends RuntimeException {
    private final HttpStatus status;
    public BizException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() { return status; }
}