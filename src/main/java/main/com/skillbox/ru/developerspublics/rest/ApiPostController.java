package main.com.skillbox.ru.developerspublics.rest;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiPostLike;
import main.com.skillbox.ru.developerspublics.api.request.RequestPosPutApiPost;
import main.com.skillbox.ru.developerspublics.api.response.ApiPostResponse;
import main.com.skillbox.ru.developerspublics.api.response.ErrorsResponse;
import main.com.skillbox.ru.developerspublics.api.response.ResultFalseErrorsResponse;
import main.com.skillbox.ru.developerspublics.api.response.ResultResponse;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.service.PostService;
import main.com.skillbox.ru.developerspublics.service.PostVoteService;
import main.com.skillbox.ru.developerspublics.service.TagToPostService;
import main.com.skillbox.ru.developerspublics.service.UserService;
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
@RequestMapping("/api/post")
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
    @GetMapping("")
    public ResponseEntity<?> getApiPost(@RequestParam(name = "offset") int offset,
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

        //запоминаем активные посты
        List<Post> posts = postService.getActivePosts();

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                posts.size(),
                postService.responsePosts(posts, offset, limit, mode)
        ));
    }


    //GET /api/post/search/
    @SneakyThrows
    @GetMapping("/search")
    public ResponseEntity<?> getApiPostSearch(@RequestParam(name = "offset") int offset,
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
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                findPosts.size(),
                postService.responsePosts(findPosts, offset, limit, "")
        ));
    }


    //GET /api/post/{ID}
    @SneakyThrows
    @GetMapping("/{ID}")
    public ResponseEntity<?> getApiPostId(@PathVariable(name = "ID") int id) {
        //ищем нужный пост по id
        Post post = postService.getInitPostById(id);

        //При успешном запросе увеличиваем количество просмотров поста на 1, кроме случаев:
        // - Если модератор авторизован, то не считаем его просмотры вообще
        // - Если автор авторизован, то не считаем просмотры своих же публикаций
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        boolean isNewView = true;
        if (user != null && post != null) {
            if (user.getIsModerator() == 1 || post.getUserId() == user.getId()) isNewView = false;
        }

        //если находим
        if (post != null) {
            //проверяем его активность
            if (postService.isPostActive(post)) {
                //увеличиваем просмотры
                if (isNewView) {
                    postService.incrementViewCount(post);
                }
                //собираем ответ и возвращаем его
                return ResponseEntity.status(HttpStatus.OK).body(postService.postByIdToJSON(post));
            }

            //если редактирует модератор - проверяем только дату и isActive
            if (user != null) {
                if (user.getIsModerator() == 1 && postService.isPostReadyToEditByModerator(post)) {
                    //собираем ответ и возвращаем его
                    return ResponseEntity.status(HttpStatus.OK).body(postService.postByIdToJSON(post));
                }
            }
        }
        //не нашли или не активен
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    //GET /api/post/byDate
    @SneakyThrows
    @GetMapping("/byDate")
    public ResponseEntity<?> getApiPostByDate(@RequestParam(name = "offset") int offset,
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

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                posts.size(),
                postService.responsePosts(posts, offset, limit, "")
        ));
    }

    //GET /api/post/byTag
    @SneakyThrows
    @GetMapping("/byTag")
    public ResponseEntity<?> getApiPostByTag(@RequestParam(name = "offset") int offset,
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

        ArrayList<Post> posts = new ArrayList<>();

        //получаем список тэг-пост для тега по имени тэга
        for (TagToPost tagToPost : tagToPostService.getTagToPostsByTagName(tagName)) {
            //находим пост по id
            Post post = postService.getInitPostById(tagToPost.getPostId());
            //проверяем на активность
            if (postService.isPostActive(post)) {
                //и запоминаем
                posts.add(post);
            }
        }

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                posts.size(),
                postService.responsePosts(posts, offset, limit, "")
        ));
    }

    //GET /api/post/moderation
    @Secured(MODERATOR)
    @GetMapping("/moderation")
    public ResponseEntity<?> getApiPostModeration(@RequestParam(name = "offset") int offset,
                                           @RequestParam(name = "limit") int limit,
                                           @RequestParam(name = "status") String status) {
        //выдергиваем из контекста пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        status = status.toUpperCase();

        //получим список постов
        List<Post> posts = new ArrayList<>();
        for (Post post : postService.getPostsByModerationStatus(status)) {
            //отбираем активные посты с нужным статусом:
            //NEW - выводим все
            //ACCEPTED-DECLINED -> выводим только для текущего пользователя
            if (post.getIsActive() == 1) {
                if (status.equals(ModerationStatuses.NEW.toString())) {
                    posts.add(post);
                }
                else if (post.getModeratorId() != null) {
                    if (post.getModeratorId() == user.getId()) {
                        posts.add(post);
                    }
                }
            }
        }

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                posts.size(),
                postService.responsePosts(posts, offset, limit, "")
        ));
    }

    //TODO GET /api/post/my
    @Secured(USER)
    @GetMapping("/my")
    public ResponseEntity<?> getApiPostMy(@RequestParam(name = "offset") int offset,
                                   @RequestParam(name = "limit") int limit,
                                   @RequestParam(name = "status") String status) {
        //status - статус модерации:
        //  inactive - скрытые, ещё не опубликованы (is_active = 0)
        //  pending - активные, ожидают утверждения модератором (is_active = 1, moderation_status = NEW)
        //  declined - отклонённые по итогам модерации (is_active = 1, moderation_status = DECLINED)
        //  published - опубликованные по итогам модерации (is_active = 1, moderation_status = ACCEPTED)

        //выдергиваем из контекста пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        //получим список постов
        List<Post> posts = new ArrayList<>();
        for (Post post : postService.getPostsByUserId(user.getId())) {
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

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                posts.size(),
                postService.responsePosts(posts, offset, limit, "")
        ));
    }

    //POST /api/post
    @Secured(USER)
    @PostMapping("")
    public ResponseEntity<?> postApiPost(@RequestBody RequestPosPutApiPost requestBody) {
       return postPutApiPost(requestBody, null);
    }

    //PUT /api/post/{ID}
    @Secured(USER)
    @PutMapping("/{ID}")
    public ResponseEntity<?> putApiPostId(@RequestBody RequestPosPutApiPost requestBody,
                                          @PathVariable(name = "ID") int postId) {
        return postPutApiPost(requestBody, postId);
    }

    @SneakyThrows
    private ResponseEntity<?> postPutApiPost(RequestPosPutApiPost requestBody, Integer postId) {
        //актуализируем время
        long timestamp = requestBody.getTimestamp();
        if (timestamp < System.currentTimeMillis()) {
            timestamp = System.currentTimeMillis();
        }

        //заголовок не короче 3х символов
        String title = requestBody.getTitle();
        if (title.length() < 3)
            return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultFalseErrorsResponse(
                        new ResultResponse(false),
                        ErrorsResponse.builder().title("Заголовок не установлен").build()
                ));

        //текст не короче 50ти символов
        String text = requestBody.getText();
        if (text.length() < 50)
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResultFalseErrorsResponse(
                            new ResultResponse(false),
                            ErrorsResponse.builder().text("Текст публикации слишком короткий").build()
                    ));


        //ошибок нет - добавляем пост
        int userId = userService
                .findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName())
                .getId();
        int isActive = requestBody.getActive();
        List<String> tagsNames = requestBody.getTags();

        //POST /api/post  -> postId = null -> создаем новый
        //PUT /api/post/{ID}  -> postId != null -> изменяем
        if (postId == null) {
            postService.savePost(timestamp, isActive, title, text, userId, tagsNames);
        }
        else postService.editPost(postId, timestamp, isActive, title, text, userId, tagsNames);

        //возвращаем ответ
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultResponse(true));
    }

    //POST /api/post/like
    @Secured(USER)
    @PostMapping("/like")
    public ResponseEntity<?> postApiPostLike(@RequestBody RequestApiPostLike requestBody) {
        return postApiPostLikeDislike(requestBody, 1);
    }

    //POST /api/post/dislike
    @Secured(USER)
    @PostMapping("/dislike")
    public ResponseEntity<?> postApiPostDislike(@RequestBody RequestApiPostLike requestBody) {
        return postApiPostLikeDislike(requestBody, -1);
    }

    @SneakyThrows
    private ResponseEntity<?> postApiPostLikeDislike(RequestApiPostLike requestBody, int value) {
        //из запроса достаем ИД поста
        int postId = requestBody.getPostId();

        //из контекста достаем пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        //пробуем поставить оценку - результат помещаем в ответ
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultResponse(postVoteService.setLikeDislike(postId, user.getId(), value)));
    }
}