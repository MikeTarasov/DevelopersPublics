package com.skillbox.ru.developerspublics.test;


import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.response.ApiAuthCaptchaResponse;
import main.com.skillbox.ru.developerspublics.model.entity.CaptchaCode;
import main.com.skillbox.ru.developerspublics.model.repository.CaptchaCodesRepository;
import main.com.skillbox.ru.developerspublics.service.CaptchaCodeService;
import org.json.simple.JSONObject;
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
public class UnitTestsCaptchaCodeService {
    @Autowired
    private CaptchaCodeService service;
    @Autowired
    private CaptchaCodesRepository repository;

    private final String code = "testCode";
    private final String secretCode = "testSecretCode";


    @Test
    public void testSaveCaptchaNotNull() {
        service.saveCaptcha(code, secretCode);
        CaptchaCode test = repository.findByCodeAndSecretCode(code, secretCode);
        Assert.assertNotNull(test);
        repository.delete(test);
    }

    @Test
    public void testCreateNewCaptchaNotNull() {
        JSONObject test = service.createNewCaptcha();
        Assert.assertNotNull(test.get("secretCode"));
        Assert.assertNotNull(test.get("base64"));
    }

    @Test
    public void testGetApiAuthCaptchaNotNull() {
        ResponseEntity<?> response = service.getApiAuthCaptcha();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        ApiAuthCaptchaResponse body = (ApiAuthCaptchaResponse) response.getBody();
        Assert.assertNotNull(body);
        Assert.assertNotNull(body.getImage());
        Assert.assertNotNull(body.getSecret());
    }
}
