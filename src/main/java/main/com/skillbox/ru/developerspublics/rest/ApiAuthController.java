package main.java.main.com.skillbox.ru.developerspublics.rest;

//import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiAuthController
{
    //POST /api/auth/login
    //{
    // "e_mail":"my@email.com",
    // "password":"dHdf6dDHfd"
    //}
    //
    //{
    // "result": true,
    // "user": {
    // "id": 576,
    // "name": "Дмитрий Петров",
    // "photo": "/avatars/ab/cd/ef/52461.jpg",
    // "email": "my@email.com",
    // "moderation": true,
    // "moderationCount": 0,
    // "settings": true
    // }
    //}
    //
    //{
    //"result": false
    //}

    //GET /api/auth/check   TODO
    //{
    // "result": true,
    // "user": {
    // "id": 576,
    // "name": "Дмитрий Петров",
    // "photo": "/avatars/ab/cd/ef/52461.jpg",
    // "email": "petrov@petroff.ru",
    // "moderation": true,
    // "moderationCount": 56,
    // "settings": true
    // }
    //}
    //
    //{
    //"result": false
    //}
//    @GetMapping("/api/auth/check")
//    public JSONObject authCheck() {
//        JSONObject response = new JSONObject();
//        response.put("result", false);
//        return response;
//    }

    //POST /api/auth/restore
    //{
    // "email":"petrov@petroff.ru"
    //}
    //
    //{
    //"result": true
    //}
    //
    //{
    //"result": false
    //}

    //POST /api/auth/password
    //{
    // "code":"b55ca6ea6cb103c6384cfa366b7ce0bdcac092be26bc0",
    // "password":"123456",
    // "captcha":"3166",
    // "captcha_secret":"eqKIqurpZs"
    //}
    //
    //{
    // "result": true
    // }
    //
    //{
    //"result": false,
    //"errors": {
    //"code": "Ссылка для восстановления пароля устарела.
    //<a href=
    //\"/auth/restore\">Запросить ссылку снова</a>",
    //"password": "Пароль короче 6-ти символов",
    //"captcha": "Код с картинки введён неверно"
    //}
    //}

    //POST /api/auth/register
    //{
    //"e_mail":"konstantin@mail.ru",
    //"password":"123456",
    //"name":"Константин",
    //"captcha":"d34f",
    //"captcha_secret":"69sdFd67df7Pd9d3"
    //}
    //
    // {
    // "result": true
    // }
    //
    //{
    // "result": false,
    // "errors": {
    // "email": "Этот e-mail уже зарегистрирован",
    // "name": "Имя указано неверно",
    // "password": "Пароль короче 6-ти символов",
    // "captcha": "Код с картинки введён неверно"
    //}
    //}

    //GET /api/auth/captcha
    //{
    // "secret": "car4y8cryaw84cr89awnrc",
    // "image": "data:image/png;base64, код_изображения_в_base64"
    //}

    //GET /api/auth/logout
    //{
    // "result": true
    //}


}
