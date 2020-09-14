package main.com.skillbox.ru.developerspublics.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestApiAuthRegister {
    @JsonProperty("e_mail")
    String email;
    String name;
    String password;
    String captcha;
    @JsonProperty("captcha_secret")
    String captchaSecret;
}
