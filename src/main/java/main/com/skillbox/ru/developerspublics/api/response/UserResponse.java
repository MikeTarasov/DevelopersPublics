package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    int id;
    String name;
    String photo;
    String email;
    boolean moderation;
    int moderationCount;
    boolean settings;
}
