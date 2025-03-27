package com.bulk;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(authorize -> authorize
          // Permit access to static resources and the landing page
          .requestMatchers("/", "/index.html", "/css/**", "/js/**").permitAll()
          // All other endpoints require authentication
          .anyRequest().authenticated()
      )
      .oauth2Login(oauth2 -> oauth2
          // After login, always redirect to our main static page
          .defaultSuccessUrl("/index.html", true)
      )
      .logout(logout -> logout
          .logoutSuccessUrl("/").permitAll()
      );
    return http.build();
  }
}
