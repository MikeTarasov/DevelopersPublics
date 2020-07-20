package main.com.skillbox.ru.developerspublics.config;


import org.springframework.context.annotation.ComponentScan;

import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;


@EnableWebMvc
@ComponentScan("main.com.skillbox.ru.developerspublics")
public class WebMVCConfig implements WebMvcConfigurer {



    // Позволяет видеть все ресурсы в папке pages, такие как картинки, стили и т.п.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("\t6 - addResourceHandlers");
        registry.addResourceHandler("/css/**", "/js/**", "/fonts/**", "/img/**", "/resources/**", "/html/**")
                .addResourceLocations("classpath:src/main/resources/");
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        System.out.println("\t - configureViewResolvers");
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/static/");
        resolver.setSuffix(".html");
        registry.viewResolver(resolver);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        System.out.println("\t - addViewControllers");
        registry.addViewController("/login").setViewName("login");
    }
}