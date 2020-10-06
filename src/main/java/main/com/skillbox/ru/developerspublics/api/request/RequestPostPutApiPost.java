package main.com.skillbox.ru.developerspublics.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RequestPostPutApiPost {
    long timestamp;
    int active;
    String title;
    List<String> tags;
    String text;
}
