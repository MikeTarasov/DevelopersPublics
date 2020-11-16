package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import main.com.skillbox.ru.developerspublics.model.projections.TagResponseProjection;

@Data
@AllArgsConstructor
public class TagResponse {

  private String name;
  private Float weight;

  public TagResponse(TagResponseProjection tagResponseProjection) {
    name = tagResponseProjection.getName();
    weight = tagResponseProjection.getWeight();
  }
}
