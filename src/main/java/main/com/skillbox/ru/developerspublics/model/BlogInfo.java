package main.com.skillbox.ru.developerspublics.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BlogInfo
{
    private final String title;
    private final String subtitle;
    private final String phone;
    private final String email;
    private final String copyright;
    private final String copyrightFrom;

    public BlogInfo() {
        title = "Developers Publications";
        subtitle = "Рассказы разработчиков";
        phone = "+7 904 550-48-47";
        email = "mktarasov@gmail.com";
        copyright = "Михаил Тарасов";
        copyrightFrom = "2020";
    }
}
