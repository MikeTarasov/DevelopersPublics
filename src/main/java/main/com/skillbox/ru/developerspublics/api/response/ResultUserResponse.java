package main.com.skillbox.ru.developerspublics.api.response;

import lombok.Data;

@Data
public class ResultUserResponse {

  private boolean result;
  private UserResponse user;

  public ResultUserResponse(UserResponse userResponse) {
    result = true;
    user = userResponse;
  }
}
