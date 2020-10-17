package main.com.skillbox.ru.developerspublics.api.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostByIdResponse {

  private int id;
  private long timestamp;
  private UserIdNameResponse user;
  private String title;
  private String text;
  private boolean active;
  private int likeCount;
  private int dislikeCount;
  private int commentCount;
  private int viewCount;
  private List<PostCommentResponse> comments;
  private List<String> tags;
}
