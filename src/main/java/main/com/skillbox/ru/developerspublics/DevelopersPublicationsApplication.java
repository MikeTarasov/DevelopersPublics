package main.com.skillbox.ru.developerspublics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class DevelopersPublicationsApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevelopersPublicationsApplication.class, args);
    }
}