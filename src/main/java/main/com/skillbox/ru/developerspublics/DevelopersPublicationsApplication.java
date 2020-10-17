package main.com.skillbox.ru.developerspublics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;


@SpringBootApplication
@EnableScheduling
//@ConfigurationPropertiesScan(basePackages = "src.main.java.main.com.skillbox.ru.developerspublics")
public class DevelopersPublicationsApplication {
    @Value("${db.timezone}")
    private String timeZone;

    @PostConstruct
    public void started() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }

    public static void main(String[] args) {
        SpringApplication.run(DevelopersPublicationsApplication.class, args);
    }
}