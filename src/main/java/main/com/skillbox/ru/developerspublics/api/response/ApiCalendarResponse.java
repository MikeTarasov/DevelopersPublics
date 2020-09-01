package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;

import java.util.TreeMap;
import java.util.TreeSet;

@AllArgsConstructor
public class ApiCalendarResponse {
    TreeSet<String> years;
    TreeMap<String, Integer> posts;
}
