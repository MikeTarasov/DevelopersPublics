package main.java.main.com.skillbox.ru.developerspublics.rest;

//import main.model.*;
//import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

//@AllArgsConstructor
@RestController
public class ApiPostController
{
//    @Autowired
//    private UsersRepository usersRepository;
//    @Autowired
//    private PostVotesRepository postVotesRepository;
//    @Autowired
//    private PostCommentsRepository postCommentsRepository;
//    @Autowired
//    private PostsRepository postsRepository;


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
//    @GetMapping("/api/post")
//    public JSONObject post(@RequestHeader(value = "mode", defaultValue = "") String mode) {
//        JSONObject response = new JSONObject();
//        System.out.println(mode + " --- ");
//        response.put("count", 0);
//        response.put("posts", new ArrayList<>());
//
////        for (Posts post : postsRepository.findAll()) {
////            posts.put(post.getId(), post);
////        }
//        return response;
//    }


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


    //GET /api/post/byDate
    //{
    //"count": 2,
    //"posts": [
    // {
    // "id": 345,
    // "time": "17.10.2019 17:32",
    // "title": "Заголовок поста",
    // "announce": "Текст анонса (часть основного текста) поста без HTMLтэгов",
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

    //GET /api/post/byTag
    //{
    //"count": 2,
    //"posts": [
    // {
    // "id": 345,
    // "time": "17.10.2019 17:32",
    // "title": "Заголовок поста",
    // "announce": "Текст анонса (часть основного текста) поста без HTMLтэгов",
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

    //GET /api/post/moderation
    //{
    //"count": 3,
    //"posts": [
    // {
    // "id": 31,
    // "time": "17.10.2019 17:32",
    // "title": "Заголовок поста",
    // "announce": "Текст анонса (часть основного текста) поста без HTMLтэгов",
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
    // "announce": "Текст анонса (часть основного текста) поста без HTMLтэгов",
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
