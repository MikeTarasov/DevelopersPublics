package com.skillbox.ru.developerspublics.test;

import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiSettings;
import main.com.skillbox.ru.developerspublics.api.response.MessageResponse;
import main.com.skillbox.ru.developerspublics.api.response.ResultResponse;
import main.com.skillbox.ru.developerspublics.model.entity.GlobalSetting;
import main.com.skillbox.ru.developerspublics.service.GlobalSettingService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsGlobalSettingService {
    @Autowired
    private GlobalSettingService service;

    @Test
    public void testGetApiInitNotNull() {
        ResponseEntity<?> responseEntity = service.getApiInit();
        Assert.assertNotNull(responseEntity);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testGetApiSettingsNotNull() {
        ResponseEntity<?> responseEntity = service.getApiSettings();
        Assert.assertNotNull(responseEntity);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testPostApiSettings200() {
        RequestApiSettings goodRequest = new RequestApiSettings();
        for (GlobalSetting gs : service.getAllGlobalSettings()) {
            switch (gs.getCode()) {
                case "MULTIUSER_MODE" : goodRequest.setMultiUserMode(gs.getValue()); break;
                case "POST_PREMODERATION" : goodRequest.setPostPremoderation(gs.getValue()); break;
                case "STATISTICS_IS_PUBLIC" : goodRequest.setStatisticsIsPublic(gs.getValue()); break;
            }
        }
        ResponseEntity<?> goodResponse = service.postApiSettings(goodRequest);
        Assert.assertEquals(goodResponse.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(goodResponse.getBody(), new ResultResponse(true));
    }

    @Test
    public void testPostApiSettings404() {
        RequestApiSettings nullRequest = new RequestApiSettings();
        ResponseEntity<?> nullResponse = service.postApiSettings(nullRequest);
        Assert.assertEquals(nullResponse.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(nullResponse.getBody(), new ResultResponse(false));
    }

    @Test
    public void testPostApiSettings400() {
        RequestApiSettings badRequest = new RequestApiSettings();
        badRequest.setStatisticsIsPublic("null");
        badRequest.setPostPremoderation("null");
        badRequest.setMultiUserMode("null");
        ResponseEntity<?> badResponse = service.postApiSettings(badRequest);
        Assert.assertEquals(badResponse.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(badResponse.getBody(), new MessageResponse("Глобальная настройка не найдена!"));
    }
}
