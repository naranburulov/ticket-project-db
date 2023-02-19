package com.cydeo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    // manual (hard-coded) way of overriding Spring-security User
    // when performing the first time sign-in
    // without connecting to DB, but just saving in the memory of an app
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder){
        List<UserDetails> userList = new ArrayList<>();

        userList.add(new User("mike", encoder.encode("Abc1"),
                        Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        userList.add(new User("naran", encoder.encode("Abc1"),
                        Arrays.asList(new SimpleGrantedAuthority("ROLE_MANAGER"))));

        return new InMemoryUserDetailsManager(userList);

    }

}
