package com.example.userservice5.security;

import com.example.userservice5.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
@Configuration
public class WebSecurity {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserService userService;
    private AuthenticationManager authenticationManager;
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private CustomAccessDeniedHandler customAccessDeniedHandler;
    private CustomAuthorizationFilter customAuthorizationFilter;

    public WebSecurity(BCryptPasswordEncoder bCryptPasswordEncoder,
                       UserService userService,
                       CustomAccessDeniedHandler customAccessDeniedHandler,
                       CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                       CustomAuthorizationFilter customAuthorizationFilter
                       ) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userService = userService;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAuthorizationFilter = customAuthorizationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
        authenticationManager = authenticationManagerBuilder.build();

        http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(
                        (auth) -> auth
                                .requestMatchers(HttpMethod.POST, "/users/signup")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/users/partners/signup")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/users/initiate-reset-password")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/users/reset-password")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/auth")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/users/verify-email/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/booking")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/users/verify-email/**")
                                .permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .authenticationManager(authenticationManager);
        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @DependsOn("filterChain")
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return authenticationManager;
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5400"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setMaxAge(Long.valueOf(3600));
        configuration.addAllowedHeader("*");

        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
