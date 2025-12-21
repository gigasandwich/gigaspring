package com.giga.spring.servlet.rest;

public class SuccessResponse extends Response {
    public SuccessResponse(int code, Object data) {
        super("success", code, data);
    }
}
