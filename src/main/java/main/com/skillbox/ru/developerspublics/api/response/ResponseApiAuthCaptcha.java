package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ResponseApiAuthCaptcha {
    String secret;
    String image;
}
