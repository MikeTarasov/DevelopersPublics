package main.com.skillbox.ru.developerspublics.config;


import org.springframework.context.annotation.ComponentScan;

import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@EnableWebMvc
@ComponentScan("main.com.skillbox.ru.developerspublics")
public class WebAppConfig implements WebMvcConfigurer {

    // Позволяет видеть все ресурсы в папке pages, такие как картинки, стили и т.п.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("\t6 - addResourceHandlers");
        registry.addResourceHandler("/css/**", "/js/**", "/fonts/**", "/img/**", "/resources/**", "/html/**")
                .addResourceLocations("classpath:src/main/resources/");
    }
}
