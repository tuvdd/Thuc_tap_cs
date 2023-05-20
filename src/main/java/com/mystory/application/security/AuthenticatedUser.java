package com.mystory.application.security;

import com.mystory.application.data.Role;
import com.mystory.application.data.entity.User;
import com.mystory.application.data.service.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.Collections;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUser {

    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
    }

    public Optional<User> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> userRepository.findByUsername(userDetails.getUsername()));
    }

    public void logout() {
        authenticationContext.logout();
    }

    public void register(String username, String password1) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userRepository.save(new User(username,passwordEncoder.encode(password1), Collections.singleton(Role.USER)));
    }
}
