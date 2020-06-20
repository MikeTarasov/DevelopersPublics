package main.com.skillbox.ru.developerspublics.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



@Configuration
@ComponentScan("main.com.skillbox.ru.developerspublics")
public class WebAppConfig implements WebMvcConfigurer {

    // Позволяет видеть все ресурсы в папке pages, такие как картинки, стили и т.п.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("src/main/resources/**")
                .addResourceLocations("src/main/resources/");
    }
}
