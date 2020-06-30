package main.com.skillbox.ru.developerspublics.rest;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.init.InitGlobalSettings;
import main.com.skillbox.ru.developerspublics.init.BlogInfo;
import main.com.skillbox.ru.developerspublics.init.InitGuest;
import main.com.skillbox.ru.developerspublics.model.GlobalSetting;
import main.com.skillbox.ru.developerspublics.model.Post;
import main.com.skillbox.ru.developerspublics.model.Tag;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
import main.com.skillbox.ru.developerspublics.repository.GlobalSettingsRepository;
import main.com.skillbox.ru.developerspublics.repository.PostsRepository;
import main.com.skillbox.ru.developerspublics.repository.TagsRepository;
import main.com.skillbox.ru.developerspublics.repository.UsersRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@AllArgsConstructor
@RestController
public class ApiGeneralController
{
    @Autowired
    private GlobalSettingsRepository globalSettingsRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private UsersRepository usersRepository;

    private final String USER = "ROLE_USER";
    private final String MODERATOR = "ROLE_MODERATOR";

    //GET /api/init/
    @GetMapping("/api/init")
    public ResponseEntity<BlogInfo> getApiInit() {
        //при запуске проверяем заполнены ли глобальные настройки
        InitGlobalSettings.init(globalSettingsRepository);
        InitGuest.addGuest(usersRepository);
        //и возвращаем инфо о блоге
        return ResponseEntity.status(HttpStatus.OK).body(new BlogInfo());
    }

    //POST /api/image
    //Запрос: Content-Type: multipart/form-data
    //image - файл изображения
    //
    //  /upload/ab/cd/ef/52461.jpg

    //POST /api/comment/
    //{
    // "parent_id":"",
    // "post_id":21,
    // "text":"привет, какой <span style="font-weight: bold;">интересный</span>
    //<span style="font-style: italic;">пост!</span>"
    //}
    //
    //{
    // "parent_id":"31",
    // "post_id":21,
    // "text":"текст комментария"
    //}
    //
    //{
    //"id": 345
    //}
    //
    //{
    //"result": false,
    //"errors": {
    //"text": "Текст комментария не задан или слишком короткий"
    //}
    //}

    //GET /api/tag/
    //{
    // "tags":
    // [
    // {
    // "name": "PHP",
    // "weight": 1
    // },
    // {
    // "name": "Python",
    // "weight": 0.33
    // }
    // ]
    //}
    @SneakyThrows
    @GetMapping("/api/tag")
    public ResponseEntity<JSONObject> getApiTag(@RequestParam(name = "query", required = false) String query) {
        //входной параметр
        //  query - часть тэга или тэг, может быть не задан или быть пустым (в этом случае выводятся все тэги)
        //выходной параметр
        // tags -> array {"name": ,"weight": }

        //init response
        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        //если тэг не задан - делаем его пустым
        if (query == null) {
            query = "";
        }

        //перебираем все тэги
        for (Tag tag : tagsRepository.findAll()) {
            //ищем совпадения
            if (tag.getName().contains(query)) {
                //все совпадения заносим в список по шаблону
                jsonArray.add(tag.toString(postsRepository, tagsRepository));
            }
        }

        //собираем ответ
        response.put("tags", jsonArray);
        //и возвращаем его
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //POST /api/moderation
    //{
    // "post_id":31,
    // "decision":"accept"
    //}

    //GET /api/calendar/
    //{
    //"years": [2017, 2018, 2019, 2020],
    //"posts": {
    //"2019-12-17": 56,
    //"2019-12-14": 11,
    //"2019-06-17": 1,
    //"2020-03-12": 6
    //}
    //}
    @SneakyThrows
    @GetMapping("/api/calendar")
    public JSONObject getApiCalendar(@RequestParam(name = "year", required = false) Integer year) {
        //входной параметр - year - год в виде четырёхзначного числа, если не передан - возвращать за текущий год
        //выходные параметры
        //  years - список всех годов, за которые была хотя бы одна публикация, в порядке убывания
        //  posts - мапа из (дата, кол-во) в порядке убывания даты -> посты за год year

        //init response
        JSONObject response = new JSONObject();
        //init списка всех годов публикаций
        TreeSet<String> yearsWithPosts = new TreeSet<>(Comparator.reverseOrder());
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
        for (Post post : postsRepository.findAll()) {
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

    //POST /api/profile/my
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
    // "removePhoto":0,
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

    //GET /api/statistics/my
    //{
    //"postsCount":7,
    //"likesCount":15,
    //"dislikesCount":2,
    //"viewsCount":58,
    //"firstPublication":"2018-07-16 17:35"
    //}

    //GET /api/statistics/all
    //{
    //"postsCount":7,
    //"likesCount":15,
    //"dislikesCount":2,
    //"viewsCount":58,
    //"firstPublication":"2018-07-16 17:35"
    //}
    @SneakyThrows
    @GetMapping("/api/statistics/all")
    public ResponseEntity<JSONObject> getApiStatisticsAll() {
        //входные параметры - нет
        //требования
        //TODO  if STATISTICS_IS_PUBLIC = NO & Auth=false -> HTTP.401

//        for (GlobalSetting globalSetting : globalSettingsRepository.findAll()) {
//            if (globalSetting.getCode().equals(GlobalSettingsCodes.STATISTICS_IS_PUBLIC) &&
//                    globalSetting.getValue().equals(GlobalSettingsValues.NO)) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
//            }
//        }

        //init response & out parameters
        JSONObject response = new JSONObject();
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;
        Date firstPublication = new Date(System.currentTimeMillis());

        //перебираем все посты
        for (Post post : postsRepository.findAll()) {
            //считаем общ. кол-во лайков
            likesCount += post.getLikesDislikesCount(1);
            //считаем общее кол-во дислайков
            dislikesCount += post.getLikesDislikesCount(-1);
            //считаем общее кол-во просмотров
            viewsCount += post.getViewCount();
            //ищем дату самого первого поста
            if (firstPublication.after(post.getTime())) {
                firstPublication = post.getTime();
            }
        }

        //собираем ответ
        response.put("postsCount", (int) postsRepository.count());
        response.put("likesCount", likesCount);
        response.put("dislikesCount", dislikesCount);
        response.put("viewsCount", viewsCount);
        response.put("firstPublication", firstPublication);
        //и возвращаем его
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //GET /api/settings
    @GetMapping("/api/settings")
    public TreeMap<String, Boolean> getApiSettings() {
        //TODO требования - запрашивающий пользователь авторизован и является модератором

        //init response
        TreeMap<String, Boolean> response = new TreeMap<>();
        //перебираем все настройки
        for (GlobalSetting globalSetting : globalSettingsRepository.findAll()) {
            //и запоминаем их в ответе -> сразу переводим value String в boolean
            response.put(globalSetting.getCode(),
                    globalSetting.getValue().equals(GlobalSettingsValues.YES.toString()));
        }
        //и возвращаем его
        return response;
    }

    //PUT /api/settings/
    //{
    // "MULTIUSER_MODE": false,
    //"POST_PREMODERATION": true,
    //"STATISTICS_IS_PUBLIC": null
    //}
}
