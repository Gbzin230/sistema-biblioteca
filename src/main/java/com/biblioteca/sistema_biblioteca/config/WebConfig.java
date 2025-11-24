package com.biblioteca.sistema_biblioteca.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // mapeia /uploads/** para a pasta de uploads no disco
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/Users/astronyx/Documents/sistema-biblioteca/uploads/");
    }
}
