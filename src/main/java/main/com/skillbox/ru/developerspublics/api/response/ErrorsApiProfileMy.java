package main.com.skillbox.ru.developerspublics.api.response;

import lombok.Builder;

@Builder
public class ErrorsApiProfileMy {
    private String user;
    private String name;
    private String email;
    private String password;
    private String photo;

//    public ErrorsApiProfileMy(boolean isUserWrong,
//                                        boolean isNameWrong,
//                                        boolean isEmailWrong,
//                                        boolean isPasswordWrong,
//                                        boolean isPhotoWrong) {
//        if (isUserWrong) user = "Пользователь не найден!";
//        if (isNameWrong) name = "Имя указано неверно";
//        if (isEmailWrong) email = "Этот e-mail уже зарегистрирован";
//        if (isPasswordWrong) password = "Пароль короче 6-ти символов";
//        if (isPhotoWrong) photo = "Фото слишком большое, нужно не более 5 Мб";
//    } //TODO Объединить все еррорсы в 1 билдер!
}
