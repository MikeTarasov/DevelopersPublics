package main.com.skillbox.ru.developerspublics.api.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPostPutApiPost {

  private long timestamp;
  private int active;
  private String title;
  private List<String> tags;
  private String text;
}
