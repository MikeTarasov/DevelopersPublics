package main.com.skillbox.ru.developerspublics.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestApiComment {
    @JsonProperty("parent_id")
    Integer parentId;
    @JsonProperty("post_id")
    int postId;
    String text;
}
