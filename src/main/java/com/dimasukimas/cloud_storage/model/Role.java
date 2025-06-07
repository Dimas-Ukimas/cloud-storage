package com.dimasukimas.cloud_storage.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    USER("USER");

    Role(String roleUser) {
    }

    @Override
    public String getAuthority() {
        return name();
    }
}
