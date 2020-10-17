package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostCommentResponse {

  private int id;
  private long timestamp;
  private String text;
  private UserIdNamePhotoResponse user;
}
