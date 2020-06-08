package main.com.skillbox.ru.developerspublics.rest;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.config.InitGlobalSettings;
import main.com.skillbox.ru.developerspublics.model.BlogInfo;
import main.com.skillbox.ru.developerspublics.model.GlobalSetting;
import main.com.skillbox.ru.developerspublics.model.Tag;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
import main.com.skillbox.ru.developerspublics.repository.GlobalSettingsRepository;
import main.com.skillbox.ru.developerspublics.repository.TagsRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

@AllArgsConstructor
@RestController
public class ApiGeneralController
{
    @Autowired
    private GlobalSettingsRepository globalSettingsRepository;

    @Autowired
    private TagsRepository tagsRepository;

    //GET /api/init/
    @GetMapping("/api/init")
    public ResponseEntity<BlogInfo> init() {
        InitGlobalSettings.init(globalSettingsRepository);
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

    //GET /api/tag/   TODO
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
    public ResponseEntity<JSONObject> getApiTag() {
        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Tag tag : tagsRepository.findAll()) {
            jsonArray.add(tag);
        }
        response.put("tags", jsonArray);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //POST /api/moderation
    //{
    // "post_id":31,
    // "decision":"accept"
    //}

    //GET /api/calendar/ TODO
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
    public ResponseEntity<JSONObject> getApiCalendar() {
        JSONObject response = new JSONObject();
        response.put("years", new ArrayList(Collections.singleton("2020")));
        response.put("posts", "");
        return ResponseEntity.status(HttpStatus.OK).body(response);
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

    //GET /api/settings/
    @GetMapping("/api/settings")
    public TreeMap<String, Boolean> getGlobalSettings() {
        TreeMap<String, Boolean> treeMap = new TreeMap<>();
        for (GlobalSetting globalSetting : globalSettingsRepository.findAll()) {
            treeMap.put(globalSetting.getCode(),
                    globalSetting.getValue().equals(GlobalSettingsValues.YES.toString()));
        }
        return treeMap;
    }

    //PUT /api/settings/
    //{
    // "MULTIUSER_MODE": false,
    //"POST_PREMODERATION": true,
    //"STATISTICS_IS_PUBLIC": null
    //}
}
