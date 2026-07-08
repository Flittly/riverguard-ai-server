package io.riverguard.module.system.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long userId;

    @Getter
    private final List<String> roleCodes;

    private final String username;
    private final String password;
    private final boolean enabled;

    public CustomUserDetails(Long userId, String username, String password,
                             List<String> roleCodes, boolean enabled) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.roleCodes = roleCodes;
        this.enabled = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleCodes.stream()
                .map(rc -> new SimpleGrantedAuthority("ROLE_" + rc))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
