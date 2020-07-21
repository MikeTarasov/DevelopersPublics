package main.com.skillbox.ru.developerspublics.rest;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.service.PostService;
import main.com.skillbox.ru.developerspublics.service.PostVoteService;
import main.com.skillbox.ru.developerspublics.service.TagToPostService;
import main.com.skillbox.ru.developerspublics.service.UserService;
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


import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;




@RestController
public class ApiPostController
{
    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private TagToPostService tagToPostService;

    @Autowired
    private PostVoteService postVoteService;


    private final String USER = "ROLE_USER";
    private final String MODERATOR = "ROLE_MODERATOR";

    //GET /api/post/
    @SneakyThrows
    @GetMapping("/api/post")
    public JSONObject getApiPost(@RequestParam(name = "offset") int offset,
                           @RequestParam(name = "limit") int limit,
                           @RequestParam(name = "mode") String mode) {
        //получаем значения параметров из заголовка:
        // offset - начальный пост
        // limit - кол-во отображ. постов
        // mode - сортировка
        //      - recent - сортировать по дате публикации, выводить сначала новые
        //      - popular - сортировать по убыванию количества комментариев
        //      - best - сортировать по убыванию количества лайков
        //      - early - сортировать по дате публикации, выводить сначала старые
        //обязательные условия:
        //  isActive = 1
        //  moderation_status = ACCEPTED
        //  посты с датой публикации не позднее текущего момента

        //создаем пустой ответ
        JSONObject response = new JSONObject();

        //запоминаем активные посты
        List<Post> posts = postService.getActivePosts();

        //собираем ответ
        response.put("count", posts.size());
        //сортируем -> обрезаем -> переносим в список ответа
        DateFormat dateFormat = new SimpleDateFormat("HH:mm, dd.MM.yyyy");
        response.put("posts", postService.responsePosts(posts, offset, limit, mode, dateFormat));
        // и возвращаем его
        return response;
    }



    //GET /api/post/search/
    @SneakyThrows
    @GetMapping("/api/post/search")
    public JSONObject getApiPostSearch(@RequestParam(name = "offset") int offset,
                                    @RequestParam(name = "limit") int limit,
                                    @RequestParam(name = "query") String query) {
        //входные параметры:
        //  offset - сдвиг от 0 для постраничного вывода
        //  limit - количество постов, которое надо вывести
        //  query - поисковый запрос
        //обязательные условия:
        //  isActive = 1
        //  moderation_status = ACCEPTED
        //  посты с датой публикации не позднее текущего момента
        //выходные параметры:
        //  count содержит общее кол-во постов, найденых по переданному в параметре query запросу
        //  posts - список постов по запросу

        //создаем пустой ответ
        JSONObject response = new JSONObject();

        //создадим список активных постов posts
        List<Post> posts = postService.getActivePosts();

        //создадим список для найденных постов findPosts
        List<Post> findPosts = new ArrayList<>();

        //если запрос пустой, метод должен выводить все посты
        if (query.length() == 0) {
            findPosts = posts;
        }
        else { //иначе ищем query в текстовых полях
            for (Post post : posts) {
                if (post.getTitle().contains(query) || post.getText().contains(query)) {
                    findPosts.add(post);
                }
            }
        }

        //собираем ответ
        response.put("count", findPosts.size());
        DateFormat dateFormat = new SimpleDateFormat("HH:mm, dd.MM.yyyy");
        response.put("posts", postService.responsePosts(findPosts, offset, limit, "", dateFormat));
        // и возвращаем его
        return response;
    }


    //TODO GET /api/post/{ID}
    @SneakyThrows
    @GetMapping("/api/post/{ID}")
    public ResponseEntity<JSONObject> getApiPostId(@PathVariable(name = "ID") int id) {
        //входной параметр - id
        //обязательные условия:
        //  isActive = 1
        //  moderation_status = ACCEPTED
        //  посты с датой публикации не позднее текущего момента
        //выходные параметры:
        //  id: 34
        //  time: "17:32, 25.05.2020"
        //  user: { "id": 88, "name": "Дмитрий Петров" }
        //  title": "Заголовок поста"
        //  announce: "Текст анонса поста без HTML-тэгов"
        //  likeCount": 36
        //  dislikeCount": 36
        //  commentCount": 15
        //  viewCount": 55
        //  comments [ { "id": 776, "time": "Вчера, 17:32", "text": "Текст комментария в формате HTML", "user":
        //             { "id": 88, "name": "Дмитрий Петров", "photo": "/avatars/ab/cd/ef/52461.jpg" } }, {...} ]
        //  tags: ["Статьи", "Java"]

        //ищем нужный пост по id
        Post post = postService.getInitPostById(id);

        //если находим
        if (post != null) {
            //проверяем его активность
            if (postService.isPostActive(post)) {
                //запоминаем просмотр
                postService.incrementViewCount(post);
                //собираем ответ и возвращаем его
                DateFormat dateFormat = new SimpleDateFormat("HH:mm, dd.MM.yyyy");
                return ResponseEntity.status(HttpStatus.OK).body(postService.postByIdToJSON(post, dateFormat));
            }
        }
        //не нашли или не активен
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    //GET /api/post/byDate
    @SneakyThrows
    @GetMapping("/api/post/byDate")
    public JSONObject getApiPostByDate(@RequestParam(name = "offset") int offset,
                                       @RequestParam(name = "limit") int limit,
                                       @RequestParam(name = "date") String date) {
        //входные параметры
        //  offset - сдвиг от 0 для постраничного вывода
        //  limit - количество постов, которое надо вывести
        //  date - дата в формате "2019-10-15"
        //обязательные условия:
        //  isActive = 1
        //  moderation_status = ACCEPTED
        //  посты с датой публикации не позднее текущего момента
        //выходные параметры
        //  count - кол-во найденных
        //  posts - список постов

        JSONObject response = new JSONObject();
        ArrayList<Post> posts = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        //перебираем активные посты
        for (Post post : postService.getActivePosts()) {
            //ищем посты с заданной датой
            if (dateFormat.format(post.getTime()).equals(date)) {
                //результат запоминаем в posts
                posts.add(post);
            }
        }
        dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        //собираем ответ
        response.put("count", posts.size());
        response.put("posts", postService.responsePosts(posts, offset, limit, "", dateFormat));
        //и возвращаем его
        return response;
    }

    //GET /api/post/byTag
    @SneakyThrows
    @GetMapping("/api/post/byTag")
    public JSONObject getApiPostByTag(@RequestParam(name = "offset") int offset,
                                      @RequestParam(name = "limit") int limit,
                                      @RequestParam(name = "tag") String tagName) {
        //входные параметры
        //  offset - сдвиг от 0 для постраничного вывода
        //  limit - количество постов, которое надо вывести
        //  tag - тэг, по которому нужно вывести все посты
        //обязательные условия:
        //  isActive = 1
        //  moderation_status = ACCEPTED
        //  посты с датой публикации не позднее текущего момента
        //выходные параметры
        //  count - кол-во найденных
        //  posts - список постов

        JSONObject response = new JSONObject();
        ArrayList<Post> posts = new ArrayList<>();

        //перебираем таблицу тэг-пост
        for (TagToPost tagToPost : tagToPostService.getInitTagToPosts()) {
            //ищем теги по наименованию
            if (tagToPost.getTagPost().getName().equals(tagName)) {
                //находим пост по id
                Post post = postService.getInitPostById(tagToPost.getPostId());
                //проверяем на активность
                if (postService.isPostActive(post)) {
                    //и запоминаем
                    posts.add(post);
                }
            }
        }

        //собираем ответ
        response.put("count", posts.size());
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        response.put("posts", postService.responsePosts(posts, offset, limit, "", dateFormat));
        return response;
    }

    //TODO GET /api/post/moderation
    //{
    //"count": 3,
    //"posts": [
    // {
    // "id": 31,
    // "time": "17.10.2019 17:32",
    // "title": "Заголовок поста",
    // "announce": "Текст анонса (часть основного текста) поста без HTML-тэгов",
    // "likeCount": 36,
    // "dislikeCount": 3,
    // "commentCount": 15,
    // "viewCount": 55,
    // "user":
    // {
    // "id": 88,
    // "name": "Дмитрий Петров"
    // }
    // },
    // {...}
    //]
    //}
    @Secured(MODERATOR)
    @GetMapping("/api/post/moderation")
    public JSONObject getApiPostModeration(@RequestParam(name = "offset") int offset,
                                           @RequestParam(name = "limit") int limit,
                                           @RequestParam(name = "status") String status) {
        JSONObject response = new JSONObject();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        //выдергиваем из контекста пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        //получим список постов
        List<Post> posts = new ArrayList<>();
        for (Post post : postService.getInitPosts()) {
            //отбираем активные посты с нужным статусом:
            //NEW - выводим все
            //ACCEPTED-DECLINED -> выводим только для текущего пользователя
            if (post.getIsActive() == 1) {
                if (status.equals(ModerationStatuses.NEW.toString())) {
                    posts.add(post);
                }
                else if (post.getModeratorId() == user.getId()) {
                    posts.add(post);
                }
            }
        }

        //собираем ответ
        response.put("count", posts.size());
        response.put("posts", postService.responsePosts(posts, offset, limit, "", dateFormat));

        return response;
    }

    //TODO GET /api/post/my
    //{
    //"count": 3,
    //"posts": [
    // {
    // "id": 31,
    // "time": "17.10.2019 17:32",
    // "title": "Заголовок поста",
    // "announce": "Текст анонса (часть основного текста) поста без HTML-тэгов",
    // "likeCount": 36,
    // "dislikeCount": 3,
    // "commentCount": 15,
    // "viewCount": 55,
    // "user":
    // {
    // "id": 88,
    // "name": "Дмитрий Петров"
    // }
    // },
    // {...}
    //]
    //}
    @Secured(USER)
    @GetMapping("/api/post/my")
    public JSONObject getApiPostMy(@RequestParam(name = "offset") int offset,       //TODO error String -> int ???????
                                   @RequestParam(name = "limit") int limit,
                                   @RequestParam(name = "status") String status) {
        //status - статус модерации:
        //  inactive - скрытые, ещё не опубликованы (is_active = 0)
        //  pending - активные, ожидают утверждения модератором (is_active = 1, moderation_status = NEW)
        //  declined - отклонённые по итогам модерации (is_active = 1, moderation_status = DECLINED)
        //  published - опубликованные по итогам модерации (is_active = 1, moderation_status = ACCEPTED)

        JSONObject response = new JSONObject();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        //выдергиваем из контекста пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        //получим список постов
        List<Post> posts = new ArrayList<>();
        for (Post post : postService.getInitPosts()) {
            //выбираем посты user
            if (post.getUserId() == user.getId()) {
                //в зависимости от статуса добавляем нужные
                switch (status) {
                    case "inactive" : if (post.getIsActive() == 0)
                        posts.add(post); break;

                    case "pending" : if (post.getIsActive() == 1 &&
                                        post.getModerationStatus().equals(ModerationStatuses.NEW.toString()))
                        posts.add(post); break;

                    case "declined" : if (post.getIsActive() == 1 &&
                                        post.getModerationStatus().equals(ModerationStatuses.DECLINED.toString()))
                        posts.add(post); break;

                    case "published" : if (post.getIsActive() == 1 &&
                                        post.getModerationStatus().equals(ModerationStatuses.ACCEPTED.toString()))
                        posts.add(post); break;
                }
            }
        }

        //собираем ответ
        response.put("count", posts.size());
        response.put("posts", postService.responsePosts(posts, offset, limit, "", dateFormat));

        return response;
    }

    //POST /api/post
    //{
    // "time":"2020-05-23 02:55",
    // "active":1,
    // "title":"заголовок",
    // "tags":["java","spring"],
    // "text":"Текст поста включащий <b>тэги форматирования</b>"
    //}
    //
    //{
    //"result": true
    //}
    //
    //{
    // "result": false,
    // "errors": {
    // "title": "Заголовок не установлен",
    // "text": "Текст публикации слишком короткий"
    // }
    //}
    @Secured(USER)  //доступ с правами ROLE_USER
    @PostMapping("/api/post")
    public JSONObject postApiPost(@RequestBody String requestBody) {
       return postPutApiPost(requestBody, null);
    }

    //TODO PUT /api/post/{ID}
    //{
    // "time":"2020-05-23 02:55",
    // "active":1,
    // "title":"заголовок",
    // "tags":["java","spring"],
    // "text":"Текст поста включащий <b>тэги форматирования</b>"
    //}
    //
    //{
    //"result": true
    //}
    //
    //{
    // "result": false,
    // "errors": {
    // "title": "Заголовок слишком короткий",
    // "text": "Текст публикации слишком короткий"
    // }
    //}
    @Secured(USER)
    @PutMapping("/api/post/{ID}")
    public JSONObject putApiPostId(@RequestBody String requestBody, @PathVariable(name = "ID") int postId) {
        return postPutApiPost(requestBody, postId);
    }

    @SneakyThrows
    private JSONObject postPutApiPost(String requestBody, Integer postId) {
        JSONObject response = new JSONObject();
        JSONObject errors = new JSONObject();

        JSONObject request = (JSONObject) new JSONParser().parse(requestBody);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date time = dateFormat.parse(request.get("time").toString());
        int isActive = Integer.parseInt(request.get("active").toString());
        String title = request.get("title").toString();
        JSONArray tagsArray = (JSONArray) request.get("tags");
        String text = request.get("text").toString();

        //получаем список тэгов
        List<String> tagsNames = new ArrayList<>();
        tagsArray.forEach(e -> tagsNames.add(e.toString()));

        //актуализируем время
        if (time.before(new Date(System.currentTimeMillis()))) {
            time = new Date(System.currentTimeMillis());
        }

        //заголовок не короче 3х символов
        if (title.length() < 3) errors.put("title", "Заголовок не установлен");

        //текст не короче 50ти символов
        if (text.length() < 50) errors.put("text", "Текст публикации слишком короткий");

        //если есть ошибки - возвращаем их
        if (errors.size() != 0) {
            response.put("result", false);
            response.put("errors", errors);
            return response;
        }

        //ошибок нет - добавляем пост
        int userId = userService
                .findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName())
                .getId();

        //POST /api/post  -> postId = null -> создаем новый
        //PUT /api/post/{ID}  -> postId != null -> изменяем
        if (postId == null) {
            postService.savePost(time, isActive, title, text, userId, tagsNames);
        }
        else postService.editPost(postId, time, isActive, title, text, userId, tagsNames);
        //возвращаем ответ
        response.put("result", true);
        return response;
    }

    //TODO POST /api/post/like
    //{
    // "post_id": 151
    //}
    //
    //{
    // "result": true
    //}
    //
    //{
    // "result": false
    //}
    @Secured(USER)
    @PostMapping("/api/post/like")
    public JSONObject postApiPostLike(@RequestBody String requestBody) {
        return postApiPostLikeDislike(requestBody, 1);
    }



    //TODO POST /api/post/dislike
    //{
    // "post_id": 151
    //}
    //
    //{
    // "result": true
    //}
    //
    //{
    // "result": false
    //}
    @Secured(USER)
    @PostMapping("/api/post/dislike")
    public JSONObject postApiPostDislike(@RequestBody String requestBody) {
        return postApiPostLikeDislike(requestBody, -1);
    }

    @SneakyThrows
    private JSONObject postApiPostLikeDislike(String requestBody, int value) {
        JSONObject response = new JSONObject();

        //из запроса достаем ИД поста
        JSONObject request = (JSONObject) new JSONParser().parse(requestBody);
        int postId = Integer.parseInt(request.get("post_id").toString());

        //из контекста достаем пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        //пробуем поставить оценку - результат помещаем в ответ
        response.put("result", postVoteService.setLikeDislike(postId, user.getId(), value));

        return response;
    }
}