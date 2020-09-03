package main.com.skillbox.ru.developerspublics.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiStatisticsResponse {
    private int postsCount;
    private int likesCount;
    private int dislikesCount;
    private int viewsCount;
    private long firstPublication;
}
