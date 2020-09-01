package main.com.skillbox.ru.developerspublics.api.request;

import lombok.Data;

@Data
public class RequestApiProfileMy {
    private String email;
    private String name;
    private String password;
    private String removePhoto;
}
