package main.com.skillbox.ru.developerspublics.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestApiModeration {

  @JsonProperty("post_id")
  private int postId;
  private String decision;
}
