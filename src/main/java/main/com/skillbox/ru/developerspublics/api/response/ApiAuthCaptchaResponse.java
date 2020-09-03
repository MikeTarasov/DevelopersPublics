package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiAuthCaptchaResponse {
    String secret;
    String image;
}
