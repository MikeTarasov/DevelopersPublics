package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserIdNamePhotoResponse {

  private int id;
  private String name;
  private String photo;
}
