package br.com.devl.mfc.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.devl.mfc.auth.exception.SecurityExceptionHandler;
import br.com.devl.mfc.auth.exception.CustomAccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtFilter jwtFilter;
	private final SecurityExceptionHandler securityExceptionHandler;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	public SecurityConfig(JwtFilter jwtFilter, SecurityExceptionHandler securityExceptionHandler,
			CustomAccessDeniedHandler accessDeniedHandler) {
		this.jwtFilter = jwtFilter;
		this.securityExceptionHandler = securityExceptionHandler;
		this.accessDeniedHandler = accessDeniedHandler;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
	        .cors(Customizer.withDefaults())
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(
	                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	        )
		        .exceptionHandling(ex -> ex
		                .authenticationEntryPoint(securityExceptionHandler)
		                .accessDeniedHandler(accessDeniedHandler)
		        )
	        .authorizeHttpRequests(auth -> auth
				.requestMatchers("/auth/**", "/actuator/health", "/actuator/health/**").permitAll()

	                .requestMatchers(
	                    "/v3/api-docs/**",
	                    "/v3/api-docs.yaml",
	                    "/swagger-ui/**",
	                    "/swagger-ui.html",
	                    "/swagger-resources/**",
	                    "/webjars/**"
	                ).permitAll()

		                .requestMatchers("/admin/**").hasRole("ADMIN")
		                .anyRequest().authenticated()
		        )
		        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

    @Bean
    PasswordEncoder passwordEncoder() {
    	return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
    throws Exception {
    	return authenticationConfiguration.getAuthenticationManager();
    }

}
