package main.com.skillbox.ru.developerspublics.api.response;

import lombok.Data;

@Data
public class ResultUserResponse {
    boolean result;
    UserResponse user;

    public ResultUserResponse(UserResponse userResponse) {
        result = true;
        user = userResponse;
    }
}
