package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PostByIdResponse {
    int id;
    long timestamp;
    UserIdNameResponse user;
    String title;
    String text;
    boolean active;
    int likeCount;
    int dislikeCount;
    int commentCount;
    int viewCount;
    List<PostCommentResponse> comments;
    List<TagResponse> tags;
}
