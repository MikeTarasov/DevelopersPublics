package main.com.skillbox.ru.developerspublics.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestApiAuthPassword {
    String code;
    String password;
    String captcha;
    @JsonProperty("captcha_secret")
    String captchaSecret;
}
