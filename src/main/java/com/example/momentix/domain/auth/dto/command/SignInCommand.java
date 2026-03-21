package com.example.momentix.domain.auth.dto.command;

public class SignInCommand {
    private final String username;
    private final String password;

    public SignInCommand(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
