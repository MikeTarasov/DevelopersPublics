package main.com.skillbox.ru.developerspublics.rest;

import main.com.skillbox.ru.developerspublics.model.Post;
import main.com.skillbox.ru.developerspublics.model.User;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.repository.PostsRepository;
import main.com.skillbox.ru.developerspublics.repository.UsersRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiAuthController
{
    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private UsersRepository usersRepository;

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
    @PostMapping("/api/auth/login")
    public JSONObject postApiAuthLogin(@RequestParam(name = "e_mail") String email,
                                       @RequestParam(name = "password") String password) {
        //заготовка ответа
        JSONObject response = new JSONObject();
        //пробуем найти пользователя в БД
        for (User user : usersRepository.findAll()) {
            if (user.getEmail().equals(email)) {
                String passwordDB = user.getPassword();
            }
        }

        return response;
    }

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
    @GetMapping("/api/auth/check")
    public JSONObject authCheck(Authentication authentication) {
        //приготовим ответ
        JSONObject response = new JSONObject();
        //если запрос от гостя
        if (authentication == null) {
            //отвечаем {"result": false}
            response.put("result", false);
        }
        else if (authentication.isAuthenticated()) {
            //если от user - собираем полный ответ

            //создаем объект для перечисления параметров
            JSONObject userProperties = new JSONObject();
            //вытаскиваем пользователя
            User user = (User) authentication.getPrincipal();
            //если он модератор - нужно считать кол-во постов для модерации
            //иначе возвращаем 0
            int moderationCount = 0;
            if (user.getIsModerator() == 1) {
                for (Post post : postsRepository.findAll()) {
                    if (post.getModerationStatus() == ModerationStatuses.NEW) {
                        moderationCount++;
                    }
                }
            }
            //все посчитали - собираем ответ
            response.put("result", true);

            userProperties.put("id", user.getId());
            userProperties.put("name", user.getName());
            userProperties.put("photo", user.getPhoto());
            userProperties.put("email", user.getEmail());
            userProperties.put("moderation", user.getIsModerator() == 1);
            userProperties.put("moderationCount", moderationCount);
            userProperties.put("settings", user.getIsModerator() == 1);

            response.put("user", userProperties);
        }
        return response;
    }

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
