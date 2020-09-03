package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.TreeMap;
import java.util.TreeSet;

@Data
@AllArgsConstructor
public class ApiCalendarResponse {
    TreeSet<String> years;
    TreeMap<String, Integer> posts;
}
