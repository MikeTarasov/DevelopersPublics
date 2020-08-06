package main.com.skillbox.ru.developerspublics.rest;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.config.AuthenticationProviderImpl;
import main.com.skillbox.ru.developerspublics.model.entity.CaptchaCode;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.service.CaptchaCodeService;
import main.com.skillbox.ru.developerspublics.service.PostService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;


@RestController
@RequestMapping("/api/auth")
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

    private final String USER = "ROLE_USER";


    //POST /api/auth/login
    @SneakyThrows
    @PostMapping("/login")
    public JSONObject postApiAuthLogin(@RequestBody String requestBody, HttpSession httpSession) {
        //запоминаем входные параметры
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
                                new UsernamePasswordAuthenticationToken(authUser.getUsername(), password))); //TODO PASS w/out encoding!

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
    @GetMapping("/check")
    public JSONObject authCheck(HttpSession httpSession) {
        //приготовим ответ
        JSONObject response = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //если авторизован
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            //собираем полный ответ

            //создаем объект для перечисления параметров
            JSONObject userProperties = new JSONObject();
            //вытаскиваем пользователя
            User user = userService.findUserByLogin(authentication.getName());

            if (user != null) {
                //проверяем сохранён ли идентификатор текущей сессии в списке авторизованных TODO спросить зачем это?
                if (!userService.isHttpSessionSaved(user.getId())) {
                    userService.addHttpSession(httpSession.toString(), user.getId());
                }

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
        }

        //иначе польз. не авторизирован / не найден
        response.put("result", false);
        return response;
    }


    //POST /api/auth/restore
    @SneakyThrows
    @PostMapping("/restore")
    public JSONObject postApiAuthRestore(@RequestBody String requestBody) {
        JSONObject response = new JSONObject();

        JSONObject request = (JSONObject) new JSONParser().parse(requestBody);
        String email = request.get("email").toString();

        boolean result = false;
        User user = null;

        for (User userDB : userService.allUsers()) {
            if (userDB.getEmail().equals(email)) {
                user = userDB;
                result = true;
                break;
            }
        }

        if (result) {
           result = userService.sendEmail(user);
        }

        response.put("result", result);
        return response;
    }

    //POST /api/auth/password
    @SneakyThrows
    @PostMapping("/password")
    public JSONObject postApiAuthPassword(@RequestBody String requestBody) {
        JSONObject response = new JSONObject();

        JSONObject request = (JSONObject) new JSONParser().parse(requestBody);
        String code = request.get("code").toString();
        String password = request.get("password").toString();
        String captcha = request.get("captcha").toString();
        String captchaSecret = request.get("captcha_secret").toString();

        boolean isCodeCorrect = false;
        boolean isPasswordCorrect = true;
        boolean isCaptchaCorrect = false;
        User user = null;

        //test code
        for (User userDB : userService.allUsers()) {
            if (userDB.getCode().equals(code)) {
                isCodeCorrect = true;
                user = userDB;
                break;
            }
        }

        //test password
        if (password.length() < 6) isPasswordCorrect = false;
        else if (user != null) user.setPassword(password);

        //test captcha
        for (CaptchaCode captchaCode : captchaCodeService.getAllCaptchaCodes()) {
            if (captchaCode.getCode().equals(captcha) && captchaCode.getSecretCode().equals(captchaSecret)) {
                isCaptchaCorrect = true;
                break;
            }
        }

        JSONObject errors = new JSONObject();
        if (!isCodeCorrect) errors.put("code", "Ссылка для восстановления пароля устарела." +
                "<a href=\"/auth/restore\">Запросить ссылку снова</a>");
        if (!isPasswordCorrect) errors.put("password", "Пароль короче 6-ти символов");
        if (!isCaptchaCorrect) errors.put("captcha", "Код с картинки введён неверно");

        if (errors.size() != 0) {
            response.put("result", false);
            response.put("errors", errors);
        }
        else response.put("result", true);

        return response;
    }

    //POST /api/auth/register
    @SneakyThrows
    @PostMapping("/register")
    public JSONObject postApiAuthRegister(@RequestBody String body) {
        JSONObject response = new JSONObject();
        boolean isPasswordCorrect = true;
        boolean isCaptchaCorrect = false;

        JSONObject requestBody = (JSONObject) new JSONParser().parse(body);

        String email = requestBody.get("e_mail").toString();
        String name = requestBody.get("name").toString();
        String password = requestBody.get("password").toString();
        String captcha = requestBody.get("captcha").toString();
        String captchaSecret = requestBody.get("captcha_secret").toString();

        //проверяем email
        boolean isEmailExist = userService.isEmailExist(email);

        //проверяем name
        boolean isNameWrong = !userService.isCorrectUserName(name);

        //проверяем password
        if (password.length() < 6) {
            isPasswordCorrect = false;
        }
        //проверяем captcha
        for (CaptchaCode captchaCode : captchaCodeService.getAllCaptchaCodes()) {
            if (captchaCode.getCode().equals(captcha) && captchaCode.getSecretCode().equals(captchaSecret)) {
                isCaptchaCorrect = true;
                break;
            }
        }

        //собираем ответ
        if (!isEmailExist && !isNameWrong && isPasswordCorrect && isCaptchaCorrect) {
            //создаем new User и отправляем true
            User user = new User(email, name, password);
            boolean isUserSaved = userService.saveUser(user);
            //собираем ответ
            response.put("result", isUserSaved);
            return response;
        }

        //есть ошибки - собираем сообщение об ошибках
        response.put("result", false);

        JSONObject errors = new JSONObject();
        if (isEmailExist)           errors.put("email", "Этот e-mail уже зарегистрирован");
        if (isNameWrong)            errors.put("name", "Имя указано неверно");
        if (!isPasswordCorrect)     errors.put("password", "Пароль короче 6-ти символов");
        if (!isCaptchaCorrect)      errors.put("captcha", "Код с картинки введён неверно");

        response.put("errors", errors);

        return response;
    }

    //GET /api/auth/captcha
    @SneakyThrows
    @GetMapping("/captcha")
    public JSONObject getApiAuthCaptcha() {
        //TODO Время устаревания должно быть задано в конфигурации приложения (по умолчанию, 1 час)
        JSONObject response = new JSONObject();

        //создадим новую капчу
        JSONObject newCaptcha = captchaCodeService.createNewCaptcha();

        //собираем ответ
        response.put("secret", newCaptcha.get("code").toString());
        response.put("image", "data:image/png;base64, " + newCaptcha.get("base64").toString());
        //и возвращаем его
        return response;
    }

    //TODO GET /api/auth/logout
    @Secured(USER)
    @GetMapping("/logout")
    public JSONObject getApiAuthLogout() {
        JSONObject response = new JSONObject();

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        if (user != null) {
            Set<GrantedAuthority> grantedAuthority = new HashSet<>();
            grantedAuthority.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
            authentication.setAuthenticated(false);
//            securityContext.setAuthentication(
//                    authenticationProvider.authenticate(
//                            new AnonymousAuthenticationToken(
//                                    "anonymous",
//                                    new org.springframework.security.core.userdetails
//                                            .User("anonymous", "anonymous", grantedAuthority),
//                                    grantedAuthority
//                            )));
            userService.deleteHttpSession(user.getId());
        }

        response.put("result", true);
        return response;
    }
}