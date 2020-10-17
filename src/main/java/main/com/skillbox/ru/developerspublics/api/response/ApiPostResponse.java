package main.com.skillbox.ru.developerspublics.api.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiPostResponse {

  private int count;
  private List<PostResponse> posts;
}
