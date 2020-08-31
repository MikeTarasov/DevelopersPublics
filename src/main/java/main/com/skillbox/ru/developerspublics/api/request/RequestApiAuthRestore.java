package main.com.skillbox.ru.developerspublics.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestApiAuthRestore {
    @JsonProperty("e_mail")
    String email;
}
