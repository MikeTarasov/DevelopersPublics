package main.com.skillbox.ru.developerspublics.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestApiSettings {

  @JsonProperty("MULTIUSER_MODE")
  private Boolean multiUserMode;
  @JsonProperty("POST_PREMODERATION")
  private Boolean postPremoderation;
  @JsonProperty("STATISTICS_IS_PUBLIC")
  private Boolean statisticsIsPublic;
}
