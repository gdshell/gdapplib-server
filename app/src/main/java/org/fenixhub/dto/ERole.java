package org.fenixhub.dto;

public enum ERole {
    ADMIN("admin"),
    USER("user"),
    DEVELOPER("developer");

    private final String role;

    ERole(String role) {
        this.role = role;
    }
    public String toString() {
        return role;
    }

}
