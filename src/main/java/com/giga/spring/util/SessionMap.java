package com.giga.spring.util;

import java.util.AbstractMap;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

public class SessionMap extends AbstractMap<String, Object>{
    private final HttpSession session;

    public SessionMap(HttpSession session) {
        this.session = session;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'entrySet'");
    }
    
    @Override
    public Object put(String key, Object value) {
        session.setAttribute(key, value);
        return null;
    }

    @Override
    public Object get(Object key) {
        return session.getAttribute((String) key);
    }

    @Override
    public Object remove(Object key) {
        session.removeAttribute((String) key);
        return null;
    }
}
