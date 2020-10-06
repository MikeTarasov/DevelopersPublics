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
import org.springframework.transaction.annotation.Transactional;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsGlobalSettingService {
    @Autowired
    private GlobalSettingService service;

    @Test
    @Transactional
    public void testGetApiInitNotNull() {
        ResponseEntity<?> responseEntity = service.getApiInit();
        Assert.assertNotNull(responseEntity);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    @Transactional
    public void testGetApiSettingsNotNull() {
        ResponseEntity<?> responseEntity = service.getApiSettings();
        Assert.assertNotNull(responseEntity);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    @Transactional
    public void testPutApiSettings200() {
        RequestApiSettings goodRequest = new RequestApiSettings();
        for (GlobalSetting gs : service.getAllGlobalSettings()) {
            switch (gs.getCode()) {
                case "MULTIUSER_MODE" : goodRequest.setMultiUserMode(Boolean.parseBoolean(gs.getValue())); break;
                case "POST_PREMODERATION" : goodRequest.setPostPremoderation(Boolean.parseBoolean(gs.getValue())); break;
                case "STATISTICS_IS_PUBLIC" : goodRequest.setStatisticsIsPublic(Boolean.parseBoolean(gs.getValue())); break;
            }
        }
        ResponseEntity<?> goodResponse = service.putApiSettings(goodRequest);
        Assert.assertEquals(HttpStatus.OK, goodResponse.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), goodResponse.getBody());
    }

    @Test
    @Transactional
    public void testPutApiSettings400() {
        RequestApiSettings nullRequest = new RequestApiSettings();
        ResponseEntity<?> nullResponse = service.putApiSettings(nullRequest);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, nullResponse.getStatusCode());
        Assert.assertEquals(new MessageResponse("Глобальная настройка не найдена!"), nullResponse.getBody());
    }
}
