package main.com.skillbox.ru.developerspublics;

import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class DevelopersPublicationsApplication {

  @Value("${db.timezone}")
  private String timeZone;

  public static void main(String[] args) {
    SpringApplication.run(DevelopersPublicationsApplication.class, args);
  }

  @PostConstruct
  public void started() {
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
  }
}