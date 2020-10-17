package main.com.skillbox.ru.developerspublics.api.response;

import java.util.TreeMap;
import java.util.TreeSet;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiCalendarResponse {

  private TreeSet<String> years;
  private TreeMap<String, Integer> posts;
}
