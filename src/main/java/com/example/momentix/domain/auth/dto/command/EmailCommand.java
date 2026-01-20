package com.example.momentix.domain.auth.dto.command;

public class EmailCommand {
    private String email;

    public EmailCommand(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
