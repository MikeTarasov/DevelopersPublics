package main.com.skillbox.ru.developerspublics.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestApiAuthPassword {
    String code;
    String password;
    String captcha;
    @JsonProperty("captcha_secret")
    String captchaSecret;
}
