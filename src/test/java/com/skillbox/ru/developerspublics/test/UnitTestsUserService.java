package com.skillbox.ru.developerspublics.test;

import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthPassword;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRegister;
import main.com.skillbox.ru.developerspublics.api.response.ErrorsResponse;
import main.com.skillbox.ru.developerspublics.api.response.ResultFalseErrorsResponse;
import main.com.skillbox.ru.developerspublics.api.response.ResultResponse;
import main.com.skillbox.ru.developerspublics.model.entity.CaptchaCode;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.UsersRepository;
import main.com.skillbox.ru.developerspublics.service.CaptchaCodeService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsUserService {
    @Autowired
    private UserService service;
    @Autowired
    private UsersRepository repository;
    @Autowired
    private CaptchaCodeService captchaCodeService;

    private final String userPassword = "userPassword";
    private final String moderatorPassword = "moderatorPassword";
    private final User testUser = new User("test@test.test", "testUser", userPassword);
    private final User testModerator = new User(
            "testM@testM.test",
            "testModerator",
            moderatorPassword);
    private final String testFilePath = "src/test/java/com/skillbox/ru/developerspublics/test/TestImage.jpg";
    private final String bigFilePath = "src/test/java/com/skillbox/ru/developerspublics/test/BigTestImage.jpg";


    private void authUser() {
        saveUser();
        service.authUser(testUser.getEmail(), userPassword);
    }

    private void saveUser() {
        service.deleteUser(testUser);
        service.saveUser(testUser);
    }


    private void saveModerator() {
        service.deleteUser(testModerator);
        testModerator.setIsModerator(1);
        service.saveUser(testModerator);
    }

    @SneakyThrows
    private MultipartFile getAvatar(String path) {
        return new MockMultipartFile("file", new FileInputStream(new File(path)));
    }

    private CaptchaCode getCaptchaCode() {
        return captchaCodeService
                .getCaptchaCodeBySecretCode(
                        captchaCodeService
                                .createNewCaptcha().get("secretCode").toString());
    }


    @Test
    public void testPostApiAuthPassword200() {
        testUser.setCode("testCode");
        saveUser();
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthPassword request = new RequestApiAuthPassword(
            "testCode",
                testUser.getPassword(),
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthPassword(request);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));

        service.deleteUser(service.findUserByLogin(testUser.getEmail()));
    }


    @Test
    public void testPostApiAuthRegister200() {
        CaptchaCode captchaCode = getCaptchaCode();

        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                testUser.getName(),
                userPassword,
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));

        service.deleteUser(service.findUserByLogin(testUser.getEmail()));
    }


    @Test
    public void testPostApiAuthRegisterEmailExist() {
        authUser();
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                testUser.getName(),
                userPassword,
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(new ErrorsResponse(
                false,
                        true,
                        true,
                        true,
                        false)));

        service.deleteUser(service.findUserByLogin(testUser.getEmail()));
    }


    @Test
    public void testPostApiAuthRegisterWrongName() {
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                "",
                userPassword,
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(new ErrorsResponse(
                        false,
                        true,
                        true,
                        false,
                        true)));
    }


    @Test
    public void testPostApiAuthRegisterWrongPassword() {
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                testUser.getName(),
                "",
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(new ErrorsResponse(
                        false,
                        false,
                        true,
                        false,
                        false)));
    }


    @Test
    public void testPostApiAuthRegisterWrongCaptcha() {
        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                testUser.getName(),
                testUser.getPassword(),
                "1",
                "2"
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(new ErrorsResponse(
                        false,
                        true,
                        false,
                        false,
                        false)));
    }


    @Test
    public void testGetApiAuthLogout() {
        authUser();
        ResponseEntity<?> response = service.getApiAuthLogout();

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Assert.assertEquals(userDetails.getUsername(), "anonymous");

        service.deleteUser(testUser);
    }


//    @Test
//    public void testPostApiProfileMyNoChanePhotoAndPassword() {
//        authUser();
//        RequestApiProfileMy requestBody = new RequestApiProfileMy();
//        requestBody.setEmail(testUser.getEmail());
//        requestBody.setName(testUser.getName());
//        ResponseEntity<?> response = service.postApiProfileMy(
//                requestBody,
//                null,
//                null,
//                null,
//                null,
//                null);
//
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        Assert.assertEquals(response.getBody(), new ResultResponse(true));
//
//        service.deleteUser(testUser);
//    }


//    @Test
//    public void testPostApiProfileMyWithChangePasswordWithoutChangePhoto() {
//        authUser();
//        RequestApiProfileMy requestBody = new RequestApiProfileMy();
//        requestBody.setEmail(testUser.getEmail() + "1");
//        requestBody.setName(testUser.getName() + "1");
//        requestBody.setPassword(userPassword + "1");
//        ResponseEntity<?> response = service.postApiProfileMy(
//                requestBody,
//                null,
//                null,
//                null,
//                null,
//                null);
//
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        Assert.assertEquals(response.getBody(), new ResultResponse(true));
//        Assert.assertNotEquals(testUser.getPassword(), service.encodePassword(userPassword));
//        Assert.assertNotEquals(testUser.getEmail(), repository.findById(testUser.getId()).get().getEmail());
//        Assert.assertNotEquals(testUser.getName(), repository.findById(testUser.getId()).get().getName());
//
//        service.deleteUser(testUser);
//    }


    @Test
    public void testPostApiProfileMyWithChangePasswordAndPhoto() {
        authUser();
        MultipartFile avatar = getAvatar(testFilePath);
        ResponseEntity<?> response = service.postApiProfileMy(
                null,
                avatar,
                testUser.getEmail(),
                testUser.getName(),
                userPassword + "1",
                "0");

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));
        Assert.assertNotNull(repository.findUserByEmail(testUser.getEmail()).getPhoto());
        Assert.assertNotEquals(testUser.getPassword(), service.encodePassword(userPassword));

        service.deleteUser(testUser);
    }


//    @Test
//    @SneakyThrows
//    public void testPostApiProfileMyDeletePhotoWithoutChangePassword() {
//        authUser();
//        service.saveAvatar(testUser, new FileInputStream(new File(testFilePath)));
//        RequestApiProfileMy requestBody = new RequestApiProfileMy();
//        requestBody.setEmail(testUser.getEmail());
//        requestBody.setName(testUser.getName());
//        requestBody.setRemovePhoto("1");
//        requestBody.setPhoto("");
//        ResponseEntity<?> response = service.postApiProfileMy(
//                requestBody,
//                null,
//                null,
//                null,
//                null,
//                null);
//
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        Assert.assertEquals(response.getBody(), new ResultResponse(true));
//        Assert.assertEquals(service.findUserByLogin(testUser.getEmail()).getPhoto(), "");
//
//        service.deleteUser(testUser);
//    }


//    @Test
//    @SneakyThrows
//    public void testPostApiProfileMyUserNotFound() {
//        authUser();
//        service.deleteUser(testUser);
//        RequestApiProfileMy requestBody = new RequestApiProfileMy();
//        requestBody.setEmail(testUser.getEmail());
//        requestBody.setName(testUser.getName());
//        ResponseEntity<?> response = service.postApiProfileMy(
//                requestBody,
//                null,
//                null,
//                null,
//                null,
//                null);
//
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        Assert.assertEquals(
//                response.getBody(),
//                new ResultFalseErrorsResponse(
//                        ErrorsResponse.builder().user("Пользователь не найден!").build()));
//    }


//    @Test
//    public void testPostApiProfileMyWrongName() {
//        authUser();
//        RequestApiProfileMy requestBody = new RequestApiProfileMy();
//        requestBody.setEmail(testUser.getEmail());
//        requestBody.setName("");
//        ResponseEntity<?> response = service.postApiProfileMy(
//                requestBody,
//                null,
//                null,
//                null,
//                null,
//                null);
//
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        Assert.assertEquals(
//                response.getBody(),
//                new ResultFalseErrorsResponse(ErrorsResponse.builder().name("Имя указано неверно").build()));
//
//        service.deleteUser(testUser);
//    }


//    @Test
//    public void testPostApiProfileMyWrongEmail() {
//        authUser();
//        saveModerator();
//        RequestApiProfileMy requestBody = new RequestApiProfileMy();
//        requestBody.setEmail(testModerator.getEmail());
//        requestBody.setName(testUser.getName());
//        ResponseEntity<?> response = service.postApiProfileMy(
//                requestBody,
//                null,
//                null,
//                null,
//                null,
//                null);
//
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        Assert.assertEquals(
//                response.getBody(),
//                new ResultFalseErrorsResponse(ErrorsResponse.builder().email("Этот e-mail уже зарегистрирован").build())
//        );
//
//        service.deleteUser(testUser);
//        service.deleteUser(testModerator);
//    }


//    @Test
//    public void testPostApiProfileMyWrongPassword() {
//        authUser();
//        RequestApiProfileMy requestBody = new RequestApiProfileMy();
//        requestBody.setEmail(testUser.getEmail());
//        requestBody.setName(testUser.getName());
//        requestBody.setPassword("1");
//        ResponseEntity<?> response = service.postApiProfileMy(
//                requestBody,
//                null,
//                null,
//                null,
//                null,
//                null);
//
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        Assert.assertEquals(
//                response.getBody(),
//                new ResultFalseErrorsResponse(
//                        ErrorsResponse.builder().password("Пароль короче 6-ти символов").build())
//        );
//
//        service.deleteUser(testUser);
//    }


    @Test
    public void testPostApiProfileMyLagePhoto() {
        authUser();
        MultipartFile avatar = getAvatar(bigFilePath);
        ResponseEntity<?> response = service.postApiProfileMy(
                null,
                avatar,
                testUser.getEmail(),
                testUser.getName(),
                userPassword + "1",
                "0");

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(
                response.getBody(),
                new ResultFalseErrorsResponse(
                        ErrorsResponse.builder().photo("Фото слишком большое, нужно не более 5 Мб").build())
        );

        service.deleteUser(testUser);
    }


    @Test
    public void testPostApiImage200() {
        authUser();
        MultipartFile avatar = getAvatar(testFilePath);

        ResponseEntity<?> response = service.postApiImage(avatar);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNotEquals(response.getBody(), "");
        service.deleteUser(testUser);
    }
}
