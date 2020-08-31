package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostCommentResponse {
    int id;
    long timestamp;
    String text;
    UserIdNamePhotoResponse user;
}
