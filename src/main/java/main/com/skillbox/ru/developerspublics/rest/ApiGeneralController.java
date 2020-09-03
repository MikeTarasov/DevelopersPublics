package main.com.skillbox.ru.developerspublics.rest;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiComment;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiModeration;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiSettings;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.model.entity.*;
import main.com.skillbox.ru.developerspublics.model.enums.*;
import main.com.skillbox.ru.developerspublics.service.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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

    @Autowired
    private PostVoteService postVoteService;

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
    public ResponseEntity<?> postApiComment(@RequestBody RequestApiComment requestBody) {
        //получаем запрос
        Integer parentId = requestBody.getParentId();
        int postId = requestBody.getPostId();
        String text = requestBody.getText();

        //выдергиваем из контекста пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        //test parenId
        if (parentId != null) {
            //try to find parent comment
            if (postCommentService.getPostCommentById(parentId) == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        //test postId
        if (postService.getPostById(postId) == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        //test text
        if (text.length() < 3)
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResultFalseErrorsResponse(
                            new ResultResponse(false),
                            new ErrorsResponse("text"))
                    );

        //ошибок нет - сохраняем
        int commentId = postCommentService.saveComment(parentId, postId, user.getId(), text);

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiCommentTrueResponse(commentId));
    }

    //GET /api/tag/
    @SneakyThrows
    @GetMapping("/api/tag")
    public ResponseEntity<?> getApiTag(@RequestParam(name = "query", required = false) String query) {
        //входной параметр
        //  query - часть тэга или тэг, может быть не задан или быть пустым (в этом случае выводятся все тэги)
        //выходной параметр
        // tags -> array {"name": , "weight": }
        List<String> tagNames = new ArrayList<>();

        //перебираем все тэги
        for (Tag tag : tagService.getTags()) {
            if (query == null) {
                //тэг не задан - выводим все
                tagNames.add(tag.getName());
            }   //иначе ищем совпадения
            else if (tag.getName().contains(query)) {
                //все совпадения заносим в список по шаблону
                tagNames.add(tag.getName());
            }
        }

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK)
                .body(new TagsListResponse(tagService.getTagResponseList(tagNames)));
    }

    //POST /api/moderation
    @SneakyThrows
    @Secured(MODERATOR)
    @PostMapping("/api/moderation")
    public ResponseEntity<?> postApiModeration(@RequestBody RequestApiModeration requestBody) {
        boolean hasErrors = false;
        String status = "";

        //получаем входные параметры
        int postId = requestBody.getPostId();
        String decision = requestBody.getDecision();
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
        return ResponseEntity.status(HttpStatus.OK).body(new ResultResponse(!hasErrors));
    }

    //GET /api/calendar
    @SneakyThrows
    @GetMapping("/api/calendar")
    public ResponseEntity<?> getApiCalendar(@RequestParam(name = "year", required = false) Integer year) {
        //входной параметр - year - год в виде четырёхзначного числа, если не передан - возвращать за текущий год
        //выходные параметры
        //  years - список всех годов, за которые была хотя бы одна публикация, в порядке возврастания
        //  posts - мапа из (дата, кол-во) в порядке убывания даты -> посты за год year

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
        return ResponseEntity.status(HttpStatus.OK).body(new ApiCalendarResponse(yearsWithPosts, dateCountPosts));
    }

    //POST /api/profile/my
    @SneakyThrows
    @Secured(USER)
    @PostMapping(value = "/api/profile/my", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<?> postApiProfileMy(@RequestBody(required = false) String requestBody,
                                       @RequestPart(value = "photo", required = false) MultipartFile avatar,
                                       @RequestPart(value = "email", required = false) String emailMP,
                                       @RequestPart(value = "name", required = false) String nameMP,
                                       @RequestPart(value = "password", required = false) String passwordMP,
                                       @RequestPart(value = "removePhoto", required = false) String removePhotoMP) {

        //if consumes = "multipart/form-data"
        String email = emailMP;
        String name = nameMP;
        String password = passwordMP;
        String removePhoto = removePhotoMP;

        System.out.println("request Body = " + requestBody);

        //else consumes = "application/json"
        if (requestBody != null) {
            JSONObject request = (JSONObject) new JSONParser().parse(requestBody);
            if (request.get("email") != null) email = request.get("email").toString();
            if (request.get("name") != null) name = request.get("name").toString();
            if (request.get("password") != null) password = request.get("password").toString();
            if (request.get("removePhoto") != null) removePhoto = request.get("removePhoto").toString();
        }

        //получаем user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        if (user == null) {
            return new ResponseEntity<>(new ResultFalseErrorsResponse(
                    new ResultResponse(false),
                    ErrorsResponse.builder().user("Пользователь не найден!").build()),
                    HttpStatus.OK);
        }

        //проверяем изменение имени
        if (!user.getName().equals(name)) {
            if (!userService.changeUserName(user, name))
                return new ResponseEntity<>(new ResultFalseErrorsResponse(
                        new ResultResponse(false),
                        ErrorsResponse.builder().name("Имя указано неверно").build()
                ), HttpStatus.OK);
        }

        //проверяем изменение e-mail
        if (!user.getEmail().equals(email)) {
            if (!userService.changeUserEmail(user, email))
                return new ResponseEntity<>(new ResultFalseErrorsResponse(
                        new ResultResponse(false),
                        ErrorsResponse.builder().email("Этот e-mail уже зарегистрирован").build()
                ), HttpStatus.OK);
        }

        //проверяем изменение пароля
        if (password != null) {
            if (password.length() >= 6) {
                userService.changeUserPassword(user, password);
            }
            else
                return new ResponseEntity<>(new ResultFalseErrorsResponse(
                    new ResultResponse(false),
                    ErrorsResponse.builder().password("Пароль короче 6-ти символов").build()), HttpStatus.OK);
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
                    userService.saveAvatar(user, inputStream);
                }
                else
                    return new ResponseEntity<>(new ResultFalseErrorsResponse(
                            new ResultResponse(false),
                            ErrorsResponse.builder().photo("Фото слишком большое, нужно не более 5 Мб").build()),
                            HttpStatus.OK);
            }
        }

    return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    //POST /api/image
    @SneakyThrows
    @Secured(USER)
    @PostMapping(value = "/api/image", consumes = {"multipart/form-data"})
    public @ResponseBody ResponseEntity<?> postApiImage(@RequestPart("image") MultipartFile avatar) {
        //получаем пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        String path = "";

        if (avatar != null) {
            InputStream inputStream = avatar.getInputStream();
            path = userService.saveAvatar(user, inputStream);
        }

        return ResponseEntity.status(HttpStatus.OK).body(path);
    }

    //GET /api/statistics/my
    @Secured(USER)
    @GetMapping("/api/statistics/my")
    public ResponseEntity<?> getApiStatisticsMy() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        int postsCount = 0;
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;
        long firstPublication = System.currentTimeMillis() / 1000;

        if (user != null) {
            for (Post post : postService.getPostsByUserId(user.getId())) {
                if (postService.isPostActive(post)) {

                    postsCount++;
                    viewsCount += post.getViewCount();

                    for (PostVote postVote : postVoteService.getPostVotesByPostId(post.getId())) {
                        if (postVote.getValue() == 1) likesCount++;
                        else dislikesCount++;
                    }

                    if (post.getTimestamp() < firstPublication) firstPublication = post.getTimestamp();
                }
            }
        }

        if (postsCount == 0) firstPublication = 0;

        return new ResponseEntity<>(new ApiStatisticsResponse(postsCount, likesCount, dislikesCount,
                viewsCount, firstPublication), HttpStatus.OK);
    }

    //GET /api/statistics/all
    @SneakyThrows
    @GetMapping("/api/statistics/all")
    public ResponseEntity<?> getApiStatisticsAll() {
        //получим пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());
        //получим настройку
        GlobalSetting globalSetting = globalSettingService.findGlobalSettingByCode(
                GlobalSettingsCodes.STATISTICS_IS_PUBLIC.toString());

        //if STATISTICS_IS_PUBLIC = NO & Auth=false -> HTTP.401
        if (globalSetting.getValue().equals(GlobalSettingsValues.NO.toString()) && user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        //init parameters
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;
        //timestamp of first publication
        long firstPublication = System.currentTimeMillis() / 1000;

        //перебираем все активные посты
        for (Post post : postService.getActivePosts()) {
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

        //собираем ответ и возвращаем его
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiStatisticsResponse(postsCount, likesCount, dislikesCount, viewsCount,
                        (postsCount == 0) ? 0 : firstPublication));
    }

    //GET /api/settings
    @GetMapping("/api/settings")
    public ResponseEntity<?> getApiSettings() {
        //init response
        TreeMap<String, Boolean> response = new TreeMap<>();
        //перебираем все настройки
        for (GlobalSetting globalSetting : globalSettingService.getAllGlobalSettings()) {
            //и запоминаем их в ответе -> сразу переводим value String в boolean
            response.put(globalSetting.getCode(),
                    globalSetting.getValue().equals(GlobalSettingsValues.YES.toString()));
        }
        //и возвращаем его
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //PUT /api/settings
    @SneakyThrows
    @Secured(MODERATOR)
    @PutMapping("/api/settings")
    public ResponseEntity<?> postApiSettings(@RequestBody RequestApiSettings requestBody) {
        boolean multiUserMode;
        boolean postPremoderation;
        boolean statisticsIsPublic;
        try {
            multiUserMode = Boolean.parseBoolean(requestBody.getMultiUserMode());
            postPremoderation = Boolean.parseBoolean(requestBody.getPostPremoderation());
            statisticsIsPublic = Boolean.parseBoolean(requestBody.getStatisticsIsPublic());
        }
        catch (Exception e) {  //Если при запросе данные не найдены - код 404
            e.printStackTrace();
            return ResponseEntity.status(404).body(new ResultResponse(false));
        }


        //Неверный параметр на входе - ответ с кодом 400 (Bad request)
        if (!globalSettingService.setGlobalSettings(multiUserMode, postPremoderation, statisticsIsPublic)) {
            return ResponseEntity.status(400).body(new MessageResponse("Глобальная настройка не найдена!"));
        }

        return ResponseEntity.status(200).body(new ResultResponse(true));
    }

    @SneakyThrows
    @GetMapping("/upload/{A}/{B}/{C}/{FILENAME}")
    @ResponseBody
    public ResponseEntity<Resource> getAvatar(@PathVariable("A") String a,
                                @PathVariable("B") String b,
                                @PathVariable("C") String c,
                                @PathVariable("FILENAME") String name) {

        Resource file = userService.getAvatar(a, b, c, name);

        if (file.exists()) return ResponseEntity.ok().body(file);
        return ResponseEntity.notFound().build();
    }
}
