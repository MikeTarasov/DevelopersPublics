package main.com.skillbox.ru.developerspublics.api.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagsListResponse {

  private List<TagResponse> tags;
}
