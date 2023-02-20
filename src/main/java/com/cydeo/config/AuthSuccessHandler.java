package com.cydeo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Configuration
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    //this class is to create an obj to pass at .successHandler() method in SecurityConfig filtering
    //so that each user (by role) will be landing on its appropriate page

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("Admin")){
            response.sendRedirect("/user/create");
        }
        if (roles.contains("Manager")){
            response.sendRedirect("/project/create");
        }
        if (roles.contains("Employee")){
            response.sendRedirect("/task/employee/pending-tasks");
        }
    }
}
