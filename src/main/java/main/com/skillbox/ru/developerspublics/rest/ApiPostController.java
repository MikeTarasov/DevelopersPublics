package main.com.skillbox.ru.developerspublics.rest;


import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.model.Post;
import main.com.skillbox.ru.developerspublics.model.TagToPost;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.enums.Roles;
import main.com.skillbox.ru.developerspublics.repository.PostsRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;


@AllArgsConstructor
@RestController
public class ApiPostController
{
//    @Autowired
//    private UsersRepository usersRepository;

    @Autowired
    private PostsRepository postsRepository;

    private final String GUEST = "ROLE_GUEST";
    private final String USER = "ROLE_USER";
    private final String MODERATOR = "ROLE_MODERATOR";

    //GET /api/post/
    //{
    //"count": 390,
    //"posts": [
    //{
    //"id": 345,
    //"time": "Вчера, 17:32",
    //"user":
    //{
    //"id": 88,
    //"name": "Дмитрий Петров"
    //},
    //"title": "Заголовок поста",
    //"announce": "Текст анонса поста без HTML-тэгов",
    //"likeCount": 36,
    //"dislikeCount": 3,
    //"commentCount": 15,
    //"viewCount": 55
    //},
    //{...}
    //]
    //}
    @SneakyThrows
//    @Secured(GUEST) //TODO
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

        //запоминаем активные посты во временном списке posts
        ArrayList<Post> posts = new ArrayList<>();

        for (Post post : postsRepository.findAll()) {  //TODO УБРАТЬ ПОВТОРЯШКУ!!!!
            if (isPostActive(post)) {
                posts.add(post);
            }
        }

        //собираем ответ
        response.put("count", posts.size());
        //сортируем -> обрезаем -> переносим в список ответа
        response.put("posts", responsePosts(posts, offset, limit, mode));
        // и возвращаем его
        return response;
    }

    private boolean isPostActive(Post post) {
        return post.getIsActive() == 1 && post.getModerationStatus() == ModerationStatuses.ACCEPTED
                && !post.getTime().after(new Date(System.currentTimeMillis()));
    }

    private Stream<Post> sortedByMode(Stream<Post> stream, String mode) {
        if (mode.equals("")) {
            mode = "recent";
        }
        switch (mode){
            case "recent": return stream.sorted(Comparator.comparing(Post::getTime));

            case "popular": return stream.sorted(Comparator.comparing(Post::getCommentsCount).reversed());

            case "best": return stream.sorted(Comparator.comparing(e -> e.getLikesDislikesCount(1)));

            case "early": return stream.sorted(Comparator.comparing(Post::getTime).reversed());
        }
        return stream;
    }

    private ArrayList<Post> responsePosts(ArrayList<Post> posts, int offset, int limit, String mode) {
        ArrayList<Post> responsePosts = new ArrayList<>();
        //сортируем -> обрезаем -> переносим в список ответа
        sortedByMode(posts.stream(), mode)
                .skip(offset).limit(limit)
                .forEach(responsePosts::add);
        return responsePosts;
    }


    //GET /api/post/search/
    // {
    // "count": 20,
    // "posts": [
    // {
    // "id": 345,
    // "time": "Вчера, 17:32",
    // "user":
    // {
    // "id": 88,
    // "name": "Дмитрий Петров"
    // },
    // "title": "Заголовок поста",
    // "announce": "Текст анонса поста без HTML-тэгов",
    // "likeCount": 36,
    // "dislikeCount": 3,
    // "commentCount": 15,
    // "viewCount": 55
    // },
    // {...}
    // ]
    // }
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
        ArrayList<Post> posts = new ArrayList<>();

        for (Post post : postsRepository.findAll()) {
            if (isPostActive(post)) {
                posts.add(post);
            }
        }

        //создадим список для найденных постов findPosts
        ArrayList<Post> findPosts = new ArrayList<>();

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
        response.put("posts", responsePosts(findPosts, offset, limit, ""));
        // и возвращаем его
        return response;
    }


    //GET /api/post/{ID}
    //{
    //"id": 34,
    //"time": "17:32, 25.05.2020",
    //"user":
    // {
    // "id": 88,
    // "name": "Дмитрий Петров"
    // },
    //"title": "Заголовок поста",
    //"announce": "Текст анонса поста без HTML-тэгов",
    //"likeCount": 36,
    //"dislikeCount": 3,
    //"commentCount": 15,
    //"viewCount": 55,
    //"comments": [
    // {
    // "id": 776,
    // "time": "Вчера, 17:32",
    // "text": "Текст комментария в формате HTML",
    // "user":
    // {
    // "id": 88,
    // "name": "Дмитрий Петров",
    // "photo": "/avatars/ab/cd/ef/52461.jpg"
    // }
    // },
    // {...}
    // ],
    //"tags": ["Статьи", "Java"]
    //}
    @SneakyThrows
    @GetMapping("/api/post/{ID}")
    public ResponseEntity<JSONObject> getApiPostId(@PathVariable int id) {
        //входной параметр - id
        //обязательные условия:
        //  isActive = 1
        //  moderation_status = ACCEPTED
        //  посты с датой публикации не позднее текущего момента
        //выходные параметры:
        //  id
        //  time
        //  user
        //  title
        //  announce
        //  likeCount
        //  dislikeCount
        //  commentCount
        //  viewCount
        //  comments
        //  tags

        JSONObject response = new JSONObject();

        //ищем нужный пост по id
        Optional<Post> optionalPost = postsRepository.findById(id);

        //если находим
        if (optionalPost.isPresent()) {
            //запоминаем его
            Post post = optionalPost.get();

            //проверяем его активность
            if (isPostActive(post)) {

                //собираем ответ
                response.put("id", post.getId());
                response.put("time", post.getTime());
                response.put("user", post.getUserPost());
                response.put("title", post.getTitle());
                response.put("announce", post.getAnnounce());
                response.put("likeCount", post.getLikesDislikesCount(1));
                response.put("dislikeCount", post.getLikesDislikesCount(-1));
                response.put("commentCount", post.getCommentsCount());
                response.put("viewCount", post.getViewCount());
                response.put("comments", post.getPostComments());
                response.put("tags", post.getTagToPosts());
                //и возвращаем его
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }
        //не нашли или не активен
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    //GET /api/post/byDate
    //{
    //"count": 2,
    //"posts": [
    // {
    // "id": 345,
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

        //перебираем репозиторий
        for (Post post : postsRepository.findAll()) {
            //ищем активные посты с заданной датой
            if (isPostActive(post) && dateFormat.format(post.getTime()).equals(date)) {
                //результат запоминаем в posts
                posts.add(post);
            }
        }

        //собираем ответ
        response.put("count", posts.size());
        response.put("posts", responsePosts(posts, offset, limit, ""));
        //и возвращаем его
        return response;
    }

    //GET /api/post/byTag
    //{
    //"count": 2,
    //"posts": [
    // {
    // "id": 345,
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
    @SneakyThrows
    @GetMapping("/api/post/byTag")
    public JSONObject getApiPostByTag(@RequestParam(name = "offset") int offset,
                                      @RequestParam(name = "limit") int limit,
                                      @RequestParam(name = "tag") String tag) {
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

        //перебираем все посты
        for (Post post : postsRepository.findAll()) {
            //ищем активные
            if (isPostActive(post)) {
                //у активных перебираем список из таблицы тэг-пост
                for (TagToPost tagToPost : post.getTagToPosts()) {
                    //ищем тэг по имени
                    if (tagToPost.getTagPost().getName().equals(tag)) {
                        //если нашли - сохраняем
                        posts.add(post);
                    }
                }
            }
        }
        //собираем ответ
        response.put("count", posts.size());
        response.put("posts", responsePosts(posts, offset, limit, ""));
        return response;
    }

    //GET /api/post/moderation
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

    //GET /api/post/my
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

    //PUT /api/post/{ID}
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

    //POST /api/post/like
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

    //POST /api/post/dislike
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


}
