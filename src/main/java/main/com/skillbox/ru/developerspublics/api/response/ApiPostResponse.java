package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ApiPostResponse {
    int count;
    List<PostResponse> posts;
}
