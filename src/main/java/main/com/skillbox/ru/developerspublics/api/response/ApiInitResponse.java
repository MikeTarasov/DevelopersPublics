package main.com.skillbox.ru.developerspublics.api.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties("blog")
public class ApiInitResponse {

  private String title;
  private String subtitle;
  private String phone;
  private String email;
  private String copyright;
  private String copyrightFrom;
}