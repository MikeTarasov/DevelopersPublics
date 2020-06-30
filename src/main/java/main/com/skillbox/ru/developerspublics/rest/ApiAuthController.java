package main.com.skillbox.ru.developerspublics.rest;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.config.UserService;
import main.com.skillbox.ru.developerspublics.model.CaptchaCode;
import main.com.skillbox.ru.developerspublics.model.Post;
import main.com.skillbox.ru.developerspublics.model.User;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.repository.CaptchaCodesRepository;
import main.com.skillbox.ru.developerspublics.repository.PostsRepository;
import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.http.parser.Authorization;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.*;


@RestController
public class ApiAuthController
{
    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;

    @Autowired
    private UserService userService;

    private final String GUEST = "ROLE_GUEST";

    //POST /api/auth/login
    @SneakyThrows
    @PostMapping("/api/auth/login")
    public JSONObject postApiAuthLogin(@RequestBody String requestBody,
                                       HttpSession httpSession) {

        JSONObject request = (JSONObject) new JSONParser().parse(requestBody);

        String email = request.get("e_mail").toString();
        String password = request.get("password").toString();

        System.out.println(email + "\n" + password + "\n" + httpSession);
        //init переменные
        JSONObject response = new JSONObject();
        JSONObject userDetails = new JSONObject();
        User authUser = null;

        //пробуем найти пользователя в БД
        for (User user : userService.allUsers()) {
            if (user.getEmail().equals(email)) {
                if (userService.isPasswordCorrect(user, password)) {
                    authUser = user;
                    break;
                }
            }
        }

        //если не нашли
        if (authUser == null) {
            response.put("result", false);
            return response;
        }

        //если нашли - запоминаем сессию
        userService.addHttpSession(httpSession.getId(), authUser.getId());

        //и заполняем ответ
        response.put("result", true);

        userDetails.put("id", authUser.getId());
        userDetails.put("name", authUser.getName());
        userDetails.put("photo", authUser.getPhoto());
        userDetails.put("email", authUser.getEmail());
        userDetails.put("moderation", authUser.getIsModerator() == 1);
        userDetails.put("moderationCount", moderationCount(authUser));
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
    public JSONObject authCheck(Authentication authentication) {
        //приготовим ответ
        JSONObject response = new JSONObject();
        //если запрос от гостя
        if (authentication == null) {
            //отвечаем {"result": false}
            response.put("result", false);
            return response;
        }

        if (authentication.isAuthenticated()) {
            //иначе - собираем полный ответ

            //создаем объект для перечисления параметров
            JSONObject userProperties = new JSONObject();
            //вытаскиваем пользователя
            User user = (User) authentication.getPrincipal();

            //собираем ответ
            response.put("result", true);

            userProperties.put("id", user.getId());
            userProperties.put("name", user.getName());
            userProperties.put("photo", user.getPhoto());
            userProperties.put("email", user.getEmail());
            userProperties.put("moderation", user.getIsModerator() == 1);
            userProperties.put("moderationCount", moderationCount(user));
            userProperties.put("settings", user.getIsModerator() == 1);

            response.put("user", userProperties);
        }
        return response;
    }

    //считаем кол-во постов для модерации
    private int moderationCount(User user) {
        int moderationCount = 0;
        if (user.getIsModerator() == 1) {
            for (Post post : postsRepository.findAll()) {
                if (post.getModerationStatus() == ModerationStatuses.NEW) {
                    moderationCount++;
                }
            }
        }
        return moderationCount;
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

    //POST /api/auth/register TODO
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


        System.out.println("login");
        //проверяем email
        for (User user : userService.allUsers()) {
            if (user.getEmail().equals(email)) {
                isEmailExist = true;
                break;
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
        for (CaptchaCode captchaCode : captchaCodesRepository.findAll()) {
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
    public JSONObject getApiAuthCaptcha(Authentication authentication, Authorization authorization, Session session,
                                        HttpSession httpSession,
                                        SecurityContextHolder securityContextHolder) {
        System.out.println(authentication + " <-> " + authorization);
        System.out.println(session + " <> " + httpSession);
        System.out.println(" <=> " + securityContextHolder);


        JSONObject response = new JSONObject();

        //сначала удалим все старое
        //процесс долгий - убираем в фоновый поток
        new Thread(() -> {
            long timeOfOldCaptcha = 60 * 60 * 1000; //время устаревания кода в мс = 1 час

            ArrayList<CaptchaCode> oldCaptchaList = new ArrayList<>();
            for (CaptchaCode captchaCode : captchaCodesRepository.findAll()) {
                if (captchaCode.getTime().before(new Date(System.currentTimeMillis() - timeOfOldCaptcha))) {
                    oldCaptchaList.add(captchaCode);
                }
            }
            captchaCodesRepository.deleteAll(oldCaptchaList);
        }).start();

        //создадим новую капчу
        //кол-во символов
        int iTotalChars = 6;
        //высота капчи
        int iHeight = 50;
        //ширина капчи
        int iWidth = 110;
        //шрифт
        int fontSize = (int) (1.67 * iWidth / iTotalChars);
        //фон
        Font fntStyle = new Font("Arial", Font.BOLD, fontSize);

        Random randChars = new Random();
        //генерируем слово
        String code = (Long.toString(Math.abs(randChars.nextLong()), 36)).substring(0, iTotalChars);
        //генерируем картинку
        BufferedImage biImage = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2dImage = (Graphics2D) biImage.getGraphics();
        int iCircle = 15;
        for (int i = 0; i < iCircle; i++) {
            g2dImage.setColor(new Color(randChars.nextInt(255), randChars.nextInt(255), randChars.nextInt(255)));
        }
        g2dImage.setFont(fntStyle);
        for (int i = 0; i < iTotalChars; i++) {
            g2dImage.setColor(new Color(randChars.nextInt(255), randChars.nextInt(255), randChars.nextInt(255)));
            if (i % 2 == 0) {
                g2dImage.drawString(code.substring(i, i + 1), (int)(fontSize * i *0.6), (int)(fontSize/1.25));
            } else {
                g2dImage.drawString(code.substring(i, i + 1), (int)(fontSize * i * 0.6), (int)(iHeight-fontSize/4));
            }
        }
        //создаем временный файл в нужном формате
        File file = new File("target/1.png");
        ImageIO.write(biImage, "png", file);
        //кодируем картинку в текст
        String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        //убираем мусор
        file.delete();
        g2dImage.dispose();

        //сохраняем капчу в репозиторий
        CaptchaCode captcha = new CaptchaCode(code);
        captchaCodesRepository.save(captcha);

        //собираем ответ
        response.put("secret", captcha.getSecretCode());
        response.put("image", "data:image/png;base64, " + base64);
        //и возвращаем его
        return response;
    }

    //GET /api/auth/logout
    //{
    // "result": true
    //}


}
