package com.giga.spring.util.http;

public class ModelAndView {
    private String view;

    public ModelAndView(String view) {
        this.view = view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }
}