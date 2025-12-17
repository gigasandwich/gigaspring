package com.giga.spring.servlet.rest;

public class ErrorResponse extends Response {
    public ErrorResponse(int code, Object data) {
        super("success", code, data);
    }
}
