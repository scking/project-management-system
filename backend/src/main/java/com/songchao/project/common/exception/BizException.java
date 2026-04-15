package com.songchao.project.common.exception;

public class BizException extends RuntimeException {
    private final int statusCode;

    public BizException(String message) {
        this(message, 400);
    }

    public BizException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
