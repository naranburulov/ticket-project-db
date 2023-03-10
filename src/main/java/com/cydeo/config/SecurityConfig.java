package com.cydeo.config;

import com.cydeo.service.SecurityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    private final SecurityService securityService;
    private final AuthSuccessHandler authSuccessHandler;

    public SecurityConfig(SecurityService securityService, AuthSuccessHandler authSuccessHandler) {
        this.securityService = securityService;
        this.authSuccessHandler = authSuccessHandler;
    }

    //filtering restrictions for pages
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .authorizeRequests()   //each request must be authorized
                //defining access to pages by Roles
                .antMatchers("/users/**").hasAuthority("Admin") //"Admin" spelling has to match with the data.sql
                .antMatchers("/project/**").hasAuthority("Manager")
                .antMatchers("/task/employee/**").hasAuthority("Employee")
                .antMatchers("/task/**").hasAuthority("Manager")
                .antMatchers(
                        "/",
                        "/login",
                        "/fragments/**",
                        "/assets/**",
                        "/images/**"
                ).permitAll()                   //the above ones - permit without authentication
                .anyRequest().authenticated()   //any other requests must be authenticated
                .and()
//                  .httpBasic()                      //form provided by Spring Security
                    .formLogin()                      //filtering for my login form
                    .loginPage("/login")
                    //.defaultSuccessUrl("/welcome")
                    .successHandler(authSuccessHandler)     //each role will land on appropriate page, see at config
                    .failureUrl("/login?error=true")
                    .permitAll()
                .and()
                .logout()                           //filtering for logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/login")
                .and()
                .rememberMe()                       //filtering for remember-me
                    .tokenValiditySeconds(120)
                    .key("cydeo")
                    .userDetailsService(securityService)    //remember who
                .and().build();
    }




}
