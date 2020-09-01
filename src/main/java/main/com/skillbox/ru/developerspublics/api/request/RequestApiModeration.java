package main.com.skillbox.ru.developerspublics.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestApiModeration {
    @JsonProperty("post_id")
    int postId;
    String decision;
}
