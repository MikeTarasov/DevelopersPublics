package main.com.skillbox.ru.developerspublics.rest;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.init.*;
import main.com.skillbox.ru.developerspublics.model.entity.*;
import main.com.skillbox.ru.developerspublics.model.enums.*;
import main.com.skillbox.ru.developerspublics.service.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.*;
import java.util.*;


@RestController
public class ApiGeneralController
{
    @Autowired
    private CaptchaCodeService captchaCodeService;

    @Autowired
    private GlobalSettingService globalSettingService;

    @Autowired
    private TagService tagService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostCommentService postCommentService;

    private final String USER = "ROLE_USER";
    private final String MODERATOR = "ROLE_MODERATOR";

    //GET /api/init/
    @GetMapping("/api/init")
    public ResponseEntity<BlogInfo> getApiInit() {
        //при запуске проверяем заполнены ли глобальные настройки
        globalSettingService.initGlobalSettings();
        //и возвращаем инфо о блоге
        return ResponseEntity.status(HttpStatus.OK).body(new BlogInfo());
    }

    //POST /api/comment/
    @SneakyThrows
    @Secured(USER)
    @PostMapping("/api/comment")
    public ResponseEntity<JSONObject> postApiComment(@RequestBody String requestBody) {
        System.out.println("POST /api/comment/");
        JSONObject response = new JSONObject();

        //получаем запрос
        JSONObject request = (JSONObject) new JSONParser().parse(requestBody);
        String parentIdString;
        try {
            parentIdString = request.get("parent_id").toString();
        }
        catch (Exception ex) {
            parentIdString = "";
        }
        int postId = Integer.parseInt(request.get("post_id").toString());
        String text = request.get("text").toString();

        System.out.println(parentIdString + " - parentID");
        System.out.println(postId + " - postId");
        System.out.println(text + " - text");

        //выдергиваем из контекста пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        //test parenId
        int parentId = 0;
        if (!parentIdString.equals("")) parentId = Integer.parseInt(parentIdString);

        if (parentId != 0) {
            //try to find parent comment
            if (postCommentService.getPostCommentById(parentId) == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        //test postId
        if (postService.getPostById(postId) == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        //test text
        if (text.length() < 3) {
            response.put("result", false);
            JSONObject error = new JSONObject();
            error.put("text", "Текст комментария не задан или слишком короткий");
            response.put("errors", error);
            return ResponseEntity.status(200).body(response);
        }

        //ошибок нет - сохраняем
        int commentId = postCommentService.saveComment(parentId, postId, user.getId(), text);

        //собираем ответ
        response.put("id", commentId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //GET /api/tag/
    @SneakyThrows
    @GetMapping("/api/tag")
    public ResponseEntity<JSONObject> getApiTag(@RequestParam(name = "query", required = false) String query) {
        //входной параметр
        //  query - часть тэга или тэг, может быть не задан или быть пустым (в этом случае выводятся все тэги)
        //выходной параметр
        // tags -> array {"name": , "weight": }

        //init response
        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        //если тэг не задан - делаем его пустым
        if (query == null) {
            query = "";
        }

        //перебираем все тэги
        for (Tag tag : tagService.getTags()) {
            if (query.equals("")) {
                jsonArray.add(tagService.tagToJSON(tag));
            }   //ищем совпадения
            else if (tag.getName().contains(query)) {
                //все совпадения заносим в список по шаблону
                jsonArray.add(tagService.tagToJSON(tag));
            }
        }

        //собираем ответ
        response.put("tags", jsonArray);
        //и возвращаем его
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //POST /api/moderation
    @SneakyThrows
    @Secured(MODERATOR)
    @PostMapping("/api/moderation")
    public JSONObject postApiModeration(@RequestBody String requestBody) {
        JSONObject response = new JSONObject();
        boolean hasErrors = false;
        String status = "";

        //получаем входные параметры
        JSONObject request = (JSONObject) new JSONParser().parse(requestBody);
        int postId = Integer.parseInt(request.get("post_id").toString());
        String decision = request.get("decision").toString();
        //получаем пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User moderator = userService.findUserByLogin(authentication.getName());

        //переводим decision -> status
        if (decision.equals("accept")) status = ModerationStatuses.ACCEPTED.toString();
        else if (decision.equals("decline")) status = ModerationStatuses.DECLINED.toString();

        //проверяем на ошибки: не найден модератор, не правильный статус, не найден пост
        if (moderator == null || status.equals("")) hasErrors = true;
        else if (!postService.setModerationStatus(postId, status, moderator.getId())) hasErrors = true;

        //собираем ответ
        response.put("result", !hasErrors);
        return response;
    }

    //GET /api/calendar
    @SneakyThrows
    @GetMapping("/api/calendar")
    public JSONObject getApiCalendar(@RequestParam(name = "year", required = false) Integer year) {
        //входной параметр - year - год в виде четырёхзначного числа, если не передан - возвращать за текущий год
        //выходные параметры
        //  years - список всех годов, за которые была хотя бы одна публикация, в порядке возврастания
        //  posts - мапа из (дата, кол-во) в порядке убывания даты -> посты за год year

        //init response
        JSONObject response = new JSONObject();
        //init списка всех годов публикаций
        TreeSet<String> yearsWithPosts = new TreeSet<>();
        //init списка дата - кол-во постов
        TreeMap<String, Integer> dateCountPosts = new TreeMap<>(Comparator.reverseOrder());

        //подготовим два паттерна для форматирования дат
        DateFormat yearDF = new SimpleDateFormat("yyyy");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        //если год не передан - возвращаем за текущий год
        if (year == 0) {
            year = Calendar.getInstance().get(Calendar.YEAR);
        }
        //для удобства переводим в String
        String stringYear = Integer.toString(year);

        //перебираем все посты
        for (Post post : postService.getPosts()) {
            //запомним год публикации данного поста
            String postYear = yearDF.format(post.getTime());
            //занесем его в список
            yearsWithPosts.add(postYear);
            //если год публикации = выбранному году
            if (postYear.equals(stringYear)) {
                //сохраняем его в список дата-кол. постов
                String postDate = dateFormat.format(post.getTime());
                //проверяем есть ли в списке такая дата
                if (dateCountPosts.containsKey(postDate)) {
                    //если есть - инкрементируем кол-во
                    dateCountPosts.put(postDate, dateCountPosts.get(postDate) + 1);
                }   //иначе добавляем новую дату
                else dateCountPosts.put(postDate, 1);
            }
        }

        //собираем ответ
        response.put("years", yearsWithPosts);
        response.put("posts", dateCountPosts);
        //и возвращаем его
        return response;
    }

    //TODO POST /api/profile/my
    //{
    // "name":"Sendel",
    // "email":"sndl@mail.ru"
    //}
    //
    //{
    // "name":"Sendel",
    // "email":"sndl@mail.ru",
    // "password":"123456"
    //}
    //
    //{
    // "photo": <binary_file>,
    // "name":"Sendel",
    // "email":"sndl@mail.ru",
    // "password":"123456",
    // "removePhoto":0
    //}
    //
    //{
    // "name":"Sendel",
    // "email":"sndl@mail.ru",
    // "removePhoto":1,
    // "photo": ""
    //}
    //
    //{
    // "result": true
    // }
    //
    //{
    // "result": false,
    // "errors": {
    // "email": "Этот e-mail уже зарегистрирован",
    // "photo": "Фото слишком большое, нужно не более 5 Мб",
    // "name": "Имя указано неверно",
    // "password": "Пароль короче 6-ти символов",
    // }
    //}
    @SneakyThrows
    @Secured(USER)
    @PostMapping(value = "/api/profile/my", consumes = {"multipart/form-data", "application/json"})
    public JSONObject postApiProfileMy(@RequestBody String requestBody,
                                       @RequestPart(value = "photo", required = false) MultipartFile avatar) {
        JSONObject response = new JSONObject();
        JSONObject errors = new JSONObject();

        JSONObject request = (JSONObject) new JSONParser().parse(requestBody);
        String email = request.get("email").toString();
        String name = request.get("name").toString();
        String password = null;
        if (request.get("password") != null) password = request.get("password").toString();
        String removePhoto = null;
        if (request.get("removePhoto") != null) removePhoto = request.get("removePhoto").toString();

        //получаем user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        if (user == null) {
            response.put("result", false);
            errors.put("user", "Пользователь не найден!");
            response.put("errors", errors);
            return response;
        }

        //проверяем изменение имени
        if (!user.getName().equals(name)) {
            if (!userService.changeUserName(user, name)) errors.put("name", "Имя указано неверно");
        }

        //проверяем изменение e-mail
        if (!user.getEmail().equals(email)) {
            if (!userService.changeUserEmail(user, email)) errors.put("email", "Этот e-mail уже зарегистрирован");
        }

        //проверяем изменение пароля
        if (password != null) {
            if (password.length() >= 6) {
                userService.changeUserPassword(user, password);
            }
            else errors.put("password", "Пароль короче 6-ти символов");
        }

        if (removePhoto != null) {
            //удаление фото
            if (removePhoto.equals("1")) {
                userService.removePhoto(user);
            }

            //изменение фото
            if (removePhoto.equals("0")) {
                if (avatar.getSize() <= 5*1024*1024) {
                    InputStream inputStream = avatar.getInputStream();
                    userService.changeUserPhoto(user, inputStream); //TODO
                }
                else errors.put("photo", "Фото слишком большое, нужно не более 5 Мб");
            }
        }

        if (errors.size() == 0) {
            response.put("result", true);
        }
        else {
            response.put("result", false);
            response.put("errors", errors);
        }
        return response;
    }

    //TODO POST /api/image
    //Запрос: Content-Type: multipart/form-data
    //image - файл изображения
    //javascript - "photo" /// avatar
    //
    //  /upload/ab/cd/ef/52461.jpg
    @SneakyThrows
    @Secured(USER)
    @PostMapping(value = "/api/image", consumes = {"multipart/form-data"})
    public @ResponseBody String postApiImage(@RequestPart("image") MultipartFile avatar) {

        //получаем пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        String path = "";

        if (avatar != null) {
            InputStream inputStream = avatar.getInputStream();
            path = userService.saveAvatar(user, inputStream);
        }

        return path;
    }

    //GET /api/statistics/my
    @Secured(USER)
    @GetMapping("/api/statistics/my")
    public JSONObject getApiStatisticsMy() {
        JSONObject response = new JSONObject();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        int postsCount = 0;
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;
        long firstPublication = System.currentTimeMillis() / 1000;

        if (user != null) {
            for (Post post : postService.getActivePosts()) {
                if (post.getUserId() == user.getId()) {

                    postsCount++;
                    viewsCount += post.getViewCount();

                    for (PostVote postVote : post.getPostVotes()) {
                        if (postVote.getValue() == 1) likesCount++;
                        else dislikesCount++;
                    }

                    if (post.getTimestamp() < firstPublication) firstPublication = post.getTimestamp();
                }
            }
        }

        if (postsCount == 0) firstPublication = 0;

        response.put("postsCount", postsCount);
        response.put("likesCount", likesCount);
        response.put("dislikesCount", dislikesCount);
        response.put("viewsCount", viewsCount);
        response.put("firstPublication", firstPublication);

        return response;
    }

    //GET /api/statistics/all
    @SneakyThrows
    @GetMapping("/api/statistics/all")
    public ResponseEntity<JSONObject> getApiStatisticsAll() {
        //получим пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());
        //получим настройку
        GlobalSetting globalSetting = globalSettingService.findGlobalSettingByCode(GlobalSettingsCodes.STATISTICS_IS_PUBLIC.toString());

        //if STATISTICS_IS_PUBLIC = NO & Auth=false -> HTTP.401
        if (globalSetting.getValue().equals(GlobalSettingsValues.NO.toString()) && user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        //init response & out parameters
        JSONObject response = new JSONObject();
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;
        //timestamp of first publication
        long firstPublication = System.currentTimeMillis() / 1000;

        //перебираем все посты
        for (Post post : postService.getInitPosts()) {
            //считаем общ. кол-во лайков
            likesCount += postService.getLikesDislikesCount(post,1);
            //считаем общее кол-во дислайков
            dislikesCount += postService.getLikesDislikesCount(post,-1);
            //считаем общее кол-во просмотров
            viewsCount += post.getViewCount();
            //ищем дату самого первого поста
            if (firstPublication > post.getTimestamp()) firstPublication = post.getTimestamp();
        }
        int postsCount = postService.getPosts().size();

        //собираем ответ
        response.put("postsCount", postsCount);
        response.put("likesCount", likesCount);
        response.put("dislikesCount", dislikesCount);
        response.put("viewsCount", viewsCount);
        response.put("firstPublication", (postsCount == 0) ? 0 : firstPublication);
        //и возвращаем его
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //GET /api/settings
    @GetMapping("/api/settings")
    public TreeMap<String, Boolean> getApiSettings() {
        //init response
        TreeMap<String, Boolean> response = new TreeMap<>();
        //перебираем все настройки
        for (GlobalSetting globalSetting : globalSettingService.getAllGlobalSettings()) {
            //и запоминаем их в ответе -> сразу переводим value String в boolean
            response.put(globalSetting.getCode(),
                    globalSetting.getValue().equals(GlobalSettingsValues.YES.toString()));
        }
        //и возвращаем его
        return response;
    }

    //PUT /api/settings
    @SneakyThrows
    @Secured(MODERATOR)
    @PutMapping("/api/settings")
    public ResponseEntity postApiSettings(@RequestBody String requestBody) {
        JSONObject request = (JSONObject) new JSONParser().parse(requestBody);
        boolean multiUserMode;
        boolean postPremoderation;
        boolean statisticsIsPublic;

        try {
            multiUserMode = Boolean.getBoolean(request.get("MULTIUSER_MODE").toString());
            postPremoderation = Boolean.getBoolean(request.get("POST_PREMODERATION").toString());
            statisticsIsPublic = Boolean.getBoolean(request.get("STATISTICS_IS_PUBLIC").toString());
        }
        catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }

        if (!globalSettingService.setGlobalSettings(multiUserMode, postPremoderation, statisticsIsPublic))
            return ResponseEntity.status(400).body(null);

        return ResponseEntity.status(200).body(null);
    }
}
