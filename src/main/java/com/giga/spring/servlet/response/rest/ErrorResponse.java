package com.giga.spring.servlet.response.rest;

public class ErrorResponse extends Response {
    public ErrorResponse(int code, Object data) {
        super("error", code, data);
    }
}
