package main.com.skillbox.ru.developerspublics.api.request;

import lombok.Data;

import java.util.List;

@Data
public class RequestPosPutApiPost {
    long timestamp;
    int active;
    String title;
    List<String> tags;
    String text;
}
