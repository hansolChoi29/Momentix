package com.example.momentix.domain.auth.impl;

import com.example.momentix.domain.auth.entity.RoleType;
import com.example.momentix.domain.auth.entity.SignIn;
import com.example.momentix.domain.users.entity.Users;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private final SignIn signIn;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        RoleType role = signIn.getUser().getRole();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public Long getUserId() {
        return signIn.getUser().getUserId();
    }

    public RoleType getRole() {
        return signIn.getUser().getRole();
    }

    public String getRoleName() {
        return signIn.getUser().getRole().name();
    }

    public Users getUser() {
        return this.signIn.getUser();
    }

    @Override
    public String getPassword() {
        return signIn.getPassword();
    }

    @Override
    public String getUsername() {
        return signIn.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
