package main.com.skillbox.ru.developerspublics.api.response;

import lombok.Data;

@Data
public class ResultFalseErrorsResponse {

  private boolean result;
  private ErrorsResponse errors;

  public ResultFalseErrorsResponse(ErrorsResponse errors) {
    result = false;
    this.errors = errors;
  }
}


