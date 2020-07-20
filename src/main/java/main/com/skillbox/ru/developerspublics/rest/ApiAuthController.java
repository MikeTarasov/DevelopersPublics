package main.com.skillbox.ru.developerspublics.rest;


import lombok.Data;
import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.config.AuthenticationProviderImpl;
import main.com.skillbox.ru.developerspublics.model.pojo.CaptchaCode;
import main.com.skillbox.ru.developerspublics.model.pojo.User;
import main.com.skillbox.ru.developerspublics.service.CaptchaCodeService;
import main.com.skillbox.ru.developerspublics.service.PostService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.apache.tomcat.util.http.parser.Authorization;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;


@Data
@RestController
public class ApiAuthController
{
    @Autowired
    private PostService postService;

    @Autowired
    private CaptchaCodeService captchaCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationProviderImpl authenticationProvider;


    //POST /api/auth/login
    @SneakyThrows
    @PostMapping("/api/auth/login")
    public JSONObject postApiAuthLogin(@RequestBody String requestBody, HttpSession httpSession) {

        JSONObject request = (JSONObject) new JSONParser().parse(requestBody);

        String email = request.get("e_mail").toString();
        String password = request.get("password").toString();

        //init переменные
        JSONObject response = new JSONObject();
        JSONObject userDetails = new JSONObject();

        //пробуем найти пользователя в БД
        User authUser = userService.findUserByLogin(email);

        //если не нашли
        if (authUser == null) {
            response.put("result", false);
            return response;
        }

        //если нашли - заносим user'а в контекст
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext
                .setAuthentication(
                    authenticationProvider
                        .authenticate(
                                new UsernamePasswordAuthenticationToken(authUser.getUsername(), password)));

        //запоминаем сессию
        userService.addHttpSession(httpSession.getId(), authUser.getId());


        //и заполняем ответ
        response.put("result", true);

        userDetails.put("id", authUser.getId());
        userDetails.put("name", authUser.getName());
        userDetails.put("photo", authUser.getPhoto());
        userDetails.put("email", authUser.getEmail());
        userDetails.put("moderation", authUser.getIsModerator() == 1);
        userDetails.put("moderationCount", userService.getModerationCount(authUser));
        userDetails.put("settings", authUser.getIsModerator() == 1);

        response.put("user", userDetails);

        return response;
    }

    //GET /api/auth/check
    // TODO Он должен проверять, сохранён ли идентификатор текущей сессии в списке авторизованных.
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
    public JSONObject authCheck() { //TODO why I not use Model model.addAttribute(key, value)????????
        //приготовим ответ
        JSONObject response = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        System.out.println("check-auth -> " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());


        //если авторизован
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            //собираем полный ответ

            //создаем объект для перечисления параметров
            JSONObject userProperties = new JSONObject();
            //вытаскиваем пользователя
            User user = userService.findUserByLogin(authentication.getName());

            //собираем ответ
            response.put("result", true);

            userProperties.put("id", user.getId());
            userProperties.put("name", user.getName());
            userProperties.put("photo", user.getPhoto());
            userProperties.put("email", user.getEmail());
            userProperties.put("moderation", user.getIsModerator() == 1);
            userProperties.put("moderationCount", userService.getModerationCount(user));
            userProperties.put("settings", user.getIsModerator() == 1);

            response.put("user", userProperties);
            return response;
        }

        //иначе польз. не авторизирован

            response.put("result", false);
            System.out.println("check => " + false);


        return response;
    }


    //TODO POST /api/auth/restore
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

    //TODO POST /api/auth/password
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

    //TODO POST /api/auth/register
    @SneakyThrows
    @PostMapping("/api/auth/register")
    public JSONObject postApiAuthRegister(@RequestBody String body) {
        JSONObject response = new JSONObject();
        boolean isEmailExist = false;
        boolean isNameWrong = false;
        boolean isPasswordInCorrect = false;
        boolean isCaptchaCorrect = false;

        JSONObject requestBody = (JSONObject) new JSONParser().parse(body);

        System.out.println("\tbody attr -> " + body);

        String email = "";
        String name = "";
        String password = "";
        String captcha = "";
        String captchaSecret = "";


        for (Object attribute : requestBody.keySet()) {
            String value = requestBody.get(attribute).toString();
            switch (attribute.toString()) {
                case "e_mail" : email = value; break;
                case "name" :   name = value;  break;
                case "password" : password = value; break;
                case "captcha" : captcha = value; break;
                case "captcha_secret" : captchaSecret = value; break;
            }
        }


        System.out.println("register");
        //проверяем email
        for (User user : userService.allUsers()) {
            if (user.getEmail().equals(email)) {
                isEmailExist = true;
                if (isNameWrong) break;
            }

            //и имя заодно
            if (user.getName().equals(name)) {
                isNameWrong = true;
                if (isEmailExist) break;
            }
        }
        //проверяем name
        if (!name.replaceAll("[0-9a-zA-Zа-яА-ЯёЁ]", "").equals("") || name.equals("")) {
            isNameWrong = true;
        }
        //проверяем password
        if (password.length() < 6) {
            isPasswordInCorrect = true;
        }
        //проверяем captcha
        for (CaptchaCode captchaCode : captchaCodeService.getAllCaptchaCodes()) {
            if (captchaCode.getCode().equals(captcha) && captchaCode.getSecretCode().equals(captchaSecret)) {
                isCaptchaCorrect = true;
                break;
            }
        }

        //собираем ответ
        if (!isEmailExist && !isNameWrong && !isPasswordInCorrect && isCaptchaCorrect) {
            //создаем new User и отправляем true
            User user = new User(email, name, password);
            boolean is = userService.saveUser(user);
            System.out.println("<is-> " + is);
            //собираем ответ
            response.put("result", true);
            return response;
        }

        //есть ошибки - собираем сообщение об ошибках
        response.put("result", false);

        JSONObject errors = new JSONObject();
        if (isEmailExist)           errors.put("email", "Этот e-mail уже зарегистрирован");
        if (isNameWrong)            errors.put("name", "Имя указано неверно");
        if (isPasswordInCorrect)    errors.put("password", "Пароль короче 6-ти символов");
        if (!isCaptchaCorrect)      errors.put("captcha", "Код с картинки введён неверно");

        response.put("errors", errors);

        return response;
    }

    //GET /api/auth/captcha
    @SneakyThrows
    @GetMapping("/api/auth/captcha")
    public JSONObject getApiAuthCaptcha() {
        JSONObject response = new JSONObject();

        //сначала удалим все старое
        //процесс долгий - убираем в фоновый поток
        new Thread(() -> captchaCodeService.deleteOldCaptcha()).start();

        //создадим новую капчу
        String base64 = captchaCodeService.createNewCaptcha().get("base64").toString();
        String code = captchaCodeService.createNewCaptcha().get("code").toString();

        //собираем ответ
        response.put("secret", code);
        response.put("image", "data:image/png;base64, " + base64);
        //и возвращаем его
        return response;
    }

    //TODO GET /api/auth/logout
    //{
    // "result": true
    //}


}
