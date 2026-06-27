package com.example.userservice5.utils;

import com.example.userservice5.SpringApplicationContext;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

public class SecurityConstants {
    public static final long EXPIRATION_TIME = 864000000;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/users";
    public static final String VERIFICATION_URL = "/users/email-verification";
    public static final String PASSWORD_RESET_REQUEST_URL = "/users/password-reset-request";
    public static final String PASSWORD_RESET_URL = "/users/password-reset";
    public static final String H2_CONSOLE = "/h2-console/**";
    public static String getTokenSecret(){
        Environment environment = (Environment) SpringApplicationContext.getBean("environment");
        return environment.getProperty("tokenSecret");
    }
    public static String getCorsAllowedOrigins() {
        Environment environment = (Environment) SpringApplicationContext.getBean("environment");
        String corsAllowedOrigins =  environment.getProperty("cors.allowed-origins");
        return corsAllowedOrigins;
    }
}
