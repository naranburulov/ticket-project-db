package com.cydeo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

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

    //everyone should be able to reach certain pages; without authentication - .antMatchers()
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .authorizeRequests()                    //each request must be authorized
                .antMatchers(
                        "/",
                        "/login",
                        "/fragments/**",
                        "/assets/**",
                        "/images/**"
                ).permitAll()                           //the above ones - permit without authentication
                .anyRequest().authenticated()   //any other requests must be authenticated
                .and()
                .httpBasic()                    //temporary custom authentication form
                .and().build();
    }




}
