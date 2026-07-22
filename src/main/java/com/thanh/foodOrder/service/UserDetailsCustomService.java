package com.thanh.foodorder.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class UserDetailsCustomService implements UserDetailsService {
    private final UserService userService;

    public UserDetailsCustomService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.thanh.foodorder.domain.User user = this.userService.getUserByEmail(username);
        List<GrantedAuthority> authorities = new ArrayList<>();
        User user2 = new User(user.getEmail(), user.getPassword(), authorities);
        return user2;

    }

}
