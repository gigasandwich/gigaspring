package com.giga.spring.servlet.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Response {
    protected String status;
    protected int code;
    protected Object data;

    public Response (String status, int code, Object data) {
        this.status = status;
        this.code = code;
        this.data = data;
    }

    public String _toString() throws JsonProcessingException {
        return toJson(this);
    }

    public static String toJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
