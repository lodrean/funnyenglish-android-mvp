package com.funnyenglish.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/**", "/h2-console/**").permitAll()
                    .requestMatchers("/admin/**").authenticated()
                    .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                    .anyRequest().permitAll()
            }
            .formLogin { form ->
                form
                    .loginPage("/admin/login")
                    .loginProcessingUrl("/admin/login")
                    .defaultSuccessUrl("/admin/dashboard", true)
                    .permitAll()
            }
            .logout { logout ->
                logout
                    .logoutUrl("/admin/logout")
                    .logoutSuccessUrl("/admin/login")
                    .permitAll()
            }
            .csrf { csrf ->
                csrf.ignoringRequestMatchers("/h2-console/**", "/api/v1/**")
            }
            .headers { headers ->
                headers.frameOptions { frame -> frame.sameOrigin() }
            }
        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val admin = User.builder()
            .username("admin")
            .password("{noop}admin123")
            .roles("ADMIN")
            .build()
        return InMemoryUserDetailsManager(admin)
    }
}
