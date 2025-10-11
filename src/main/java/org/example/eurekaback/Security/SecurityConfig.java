package org.example.eurekaback.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private PanelistSecurityService panelistSecurityService;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/files/**").permitAll()
                .requestMatchers("/api/images/**").permitAll()
                .requestMatchers("/api/ai/health").permitAll()
                .requestMatchers("/api/ai/**").hasAnyRole("ADMIN", "ANNONCEUR")
                // Gamification endpoints
                .requestMatchers(HttpMethod.GET, "/api/gamification/profile").hasAnyRole("ADMIN", "ANNONCEUR", "PANELISTE")
                .requestMatchers(HttpMethod.GET, "/api/gamification/profile/me").hasRole("PANELISTE")
                .requestMatchers(HttpMethod.GET, "/api/gamification/profile/*").hasAnyRole("ADMIN", "ANNONCEUR", "PANELISTE")
                .requestMatchers(HttpMethod.GET, "/api/gamification/leaderboard").hasAnyRole("ADMIN", "ANNONCEUR", "PANELISTE")
                .requestMatchers(HttpMethod.POST, "/api/gamification/events").hasAnyRole("ADMIN", "ANNONCEUR")
                .requestMatchers(HttpMethod.POST, "/api/gamification/recompute/leaderboard").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/gamification/cleanup").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/gamification/rewards/*/claim").hasRole("PANELISTE")
                .requestMatchers("/api/campagnes/{campaignId}/participation-status/{panelisteId}").hasAnyRole("ADMIN", "ANNONCEUR", "PANELISTE")
                .requestMatchers("/api/campagnes/{campaignId}/panelistes-status").hasAnyRole("ADMIN", "ANNONCEUR")
                .requestMatchers("/api/campaigns/{campaignId}/accept-invitation").hasAnyRole("ADMIN", "PANELISTE")
                .requestMatchers("/api/campaigns/{campaignId}/decline-invitation").hasAnyRole("ADMIN", "PANELISTE")
                .requestMatchers("/api/reponses/campaign/{campaignId}/paneliste/{panelisteId}").hasAnyRole("ADMIN", "ANNONCEUR", "PANELISTE")
                .requestMatchers("/api/reponses/campaign/{campaignId}").hasAnyRole("ADMIN", "ANNONCEUR")
                .requestMatchers("/api/reponses/campaign/{campaignId}/paneliste/{panelisteId}/details").hasAnyRole("ADMIN", "ANNONCEUR")
                .requestMatchers("/api/reponses/campaign/{campaignId}/export").hasAnyRole("ADMIN", "ANNONCEUR")
                .requestMatchers(HttpMethod.GET, "/api/panelistes/*/invitations").hasAnyRole("ADMIN", "ANNONCEUR", "PANELISTE")
                .requestMatchers(HttpMethod.GET, "/api/panelistes/*/participation-history").hasAnyRole("ADMIN", "ANNONCEUR")
                .requestMatchers(HttpMethod.GET, "/api/paneliste-campagnes/paneliste/*/participated").hasAnyRole("ADMIN", "ANNONCEUR", "PANELISTE")
                .requestMatchers(HttpMethod.POST, "/api/panelistes/*/invitations/*/accept").hasAnyRole("ADMIN", "PANELISTE")
                .requestMatchers(HttpMethod.POST, "/api/panelistes/*/invitations/*/reject").hasAnyRole("ADMIN", "PANELISTE")
                .requestMatchers(HttpMethod.GET, "/api/panelistes/{id}").access((authentication, context) -> {
                    // Allow ADMIN and ANNONCEUR roles
                    if (authentication.get().getAuthorities().stream()
                            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN") || 
                                            authority.getAuthority().equals("ROLE_ANNONCEUR"))) {
                        return new org.springframework.security.authorization.AuthorizationDecision(true);
                    }
                    
                    // For PANELISTE role, check ownership
                    if (authentication.get().getAuthorities().stream()
                            .anyMatch(authority -> authority.getAuthority().equals("ROLE_PANELISTE"))) {
                        try {
                            // Extract the ID from the URL path
                            String pathId = context.getVariables().get("id");
                            Integer panelisteId = Integer.parseInt(pathId);
                            
                            // Use the PanelistSecurityService to check ownership
                            boolean isOwner = panelistSecurityService.isOwner(authentication.get(), panelisteId);
                            return new org.springframework.security.authorization.AuthorizationDecision(isOwner);
                        } catch (NumberFormatException e) {
                            return new org.springframework.security.authorization.AuthorizationDecision(false);
                        }
                    }
                    
                    return new org.springframework.security.authorization.AuthorizationDecision(false);
                })
                .requestMatchers(HttpMethod.GET, "/api/panelistes/**").hasAnyRole("ADMIN", "ANNONCEUR")
                .requestMatchers(HttpMethod.POST, "/api/panelistes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/panelistes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/panelistes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/panelistes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/formulaires/campaign/**").hasAnyRole("PANELISTE", "ADMIN", "ANNONCEUR")
                .requestMatchers(HttpMethod.GET, "/api/campagnes/*/formulaire").hasAnyRole("PANELISTE", "ADMIN", "ANNONCEUR")
                .requestMatchers(HttpMethod.GET, "/api/formulaires").hasAnyRole("PANELISTE", "ADMIN", "ANNONCEUR")
                .requestMatchers(HttpMethod.GET, "/api/formulaires/*/with-questions").hasAnyRole("PANELISTE", "ADMIN", "ANNONCEUR")
                .requestMatchers(HttpMethod.GET, "/api/formulaires/*/questions").hasAnyRole("PANELISTE", "ADMIN", "ANNONCEUR")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Allow-Origin", "Access-Control-Allow-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "Access-Control-Allow-Origin", "Access-Control-Allow-Headers"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configure a custom HttpFirewall that allows double slashes in URLs
     * This is needed for handling image URLs that might contain double slashes
     */
    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // Allow double slashes in URLs
        firewall.setAllowUrlEncodedDoubleSlash(true);
        // Allow semicolons in URLs
        firewall.setAllowSemicolon(true);
        return firewall;
    }

    /**
     * Register the custom HttpFirewall with Spring Security
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }
}
