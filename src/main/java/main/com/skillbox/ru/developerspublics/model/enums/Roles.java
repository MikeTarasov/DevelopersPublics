package main.com.skillbox.ru.developerspublics.model.enums;

public enum Roles {
    GUEST("ROLE_GUEST"),
    USER("ROLE_USER"),
    MODERATOR("ROLE_MODERATOR");

    private final String role;
    Roles(String role) {
        this.role = role;
    }
    public String getRole() {
        return role;
    }
}
