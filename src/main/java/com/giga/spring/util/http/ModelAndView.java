package com.giga.spring.util.http;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String view;

    private Map<String, Object> attributes;

    public ModelAndView(String view) {
        this.view = view;
        this.attributes = new HashMap<>();
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
}