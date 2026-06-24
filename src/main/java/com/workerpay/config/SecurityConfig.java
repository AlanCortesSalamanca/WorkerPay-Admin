package com.workerpay.config;

import com.workerpay.auth.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/dashboard").authenticated()
                .requestMatchers("/users/**", "/reports/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,
                    "/workers/**",
                    "/advances/**",
                    "/debts/**",
                    "/payroll/**",
                    "/payment-periods/**"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,
                    "/workers/new",
                    "/workers/*/edit",
                    "/advances/new",
                    "/advances/*/edit",
                    "/debts/new",
                    "/debts/*/edit",
                    "/debts/*/payments/new",
                    "/payroll/new",
                    "/payroll/*/edit",
                    "/payment-periods/new",
                    "/payment-periods/*/edit"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,
                    "/workers/**",
                    "/advances/**",
                    "/debts/**",
                    "/payroll/**",
                    "/payment-periods/**"
                ).hasAnyRole("ADMIN", "OPERATOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(exception -> exception.accessDeniedPage("/error/403"));

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
