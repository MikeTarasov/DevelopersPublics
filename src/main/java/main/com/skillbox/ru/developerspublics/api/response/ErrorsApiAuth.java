package main.com.skillbox.ru.developerspublics.api.response;


public class ErrorsApiAuth {
    String code;
    String password;
    String captcha;
    String email;
    String name;

    public ErrorsApiAuth(boolean isCodeCorrect,
                         boolean isPasswordCorrect,
                         boolean isCaptchaCorrect,
                         boolean isEmailExist,
                         boolean isNameWrong) {
        if (!isCodeCorrect) code = "Ссылка для восстановления пароля устарела. <a href=\"/auth/restore\">" +
                "Запросить ссылку снова</a>";
        if (!isPasswordCorrect) password = "Пароль короче 6-ти символов";
        if (!isCaptchaCorrect) captcha = "Код с картинки введён неверно";
        if (isEmailExist) email = "Этот e-mail уже зарегистрирован";
        if (isNameWrong) name = "Имя указано неверно";
    }
}
