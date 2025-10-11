package org.example.eurekaback.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", 
                        "Access-Control-Allow-Origin", "Access-Control-Allow-Headers")
                .exposedHeaders("Authorization", "Content-Type", "Access-Control-Allow-Origin", "Access-Control-Allow-Headers")
                .allowCredentials(true)
                .maxAge(3600);
    }
}