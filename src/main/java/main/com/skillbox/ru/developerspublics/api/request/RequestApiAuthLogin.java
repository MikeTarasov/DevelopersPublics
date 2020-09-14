package main.com.skillbox.ru.developerspublics.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestApiAuthLogin {
    @JsonProperty("e_mail")
    private String email;
    private String password;
}
