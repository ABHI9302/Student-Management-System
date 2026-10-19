package com.cwm.studentmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringConfig {

	private static final String[] PUBLIC_PATH = {
			"/login",
			"/css/**",
			"/images/**",
			"/js/**",
			"/favicon.ico",
			"/error"
	};

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// CSRF is intentionally left ENABLED: the Thymeleaf templates (login.html, layout.html)
		// read ${_csrf.parameterName} / ${_csrf.token}, which only exist when CSRF is on.
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_PATH).permitAll()
						.anyRequest().authenticated()
				)
				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/login")
						.defaultSuccessUrl("/dashboard", true)
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("JSESSIONID")
						.permitAll());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// NOTE: No UserDetailsService bean here on purpose. UserServiceImpl (database-backed)
	// is the only UserDetailsService; defining a second one made Spring Security skip
	// auto-configuration and login failed.
}
