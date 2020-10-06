package main.com.skillbox.ru.developerspublics.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPostPutApiPost {
    long timestamp;
    int active;
    String title;
    List<String> tags;
    String text;
}
