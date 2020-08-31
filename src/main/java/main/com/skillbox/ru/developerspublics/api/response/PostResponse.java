package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostResponse {
    int id;
    long timestamp;
    UserIdNameResponse user;
    String title;
    String announce;
    int likeCount;
    int dislikeCount;
    int commentCount;
    int viewCount;
}
