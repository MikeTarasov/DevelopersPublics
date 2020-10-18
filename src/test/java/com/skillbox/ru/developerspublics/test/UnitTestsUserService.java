package com.skillbox.ru.developerspublics.test;

import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthLogin;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthPassword;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRegister;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRestore;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.model.entity.CaptchaCode;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.UsersRepository;
import main.com.skillbox.ru.developerspublics.service.CaptchaCodeService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;


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
    @Value("${moderator.email}")
    private String realEmail;


    @Transactional
    private void authUser() {
        saveUser();
        service.authUser(testUser.getEmail(), userPassword);
    }

    @Transactional
    private void saveUser() {
        deleteUser();
        service.saveUser(testUser);
    }


    @Transactional
    private void saveModerator() {
        service.deleteUser(testModerator);
        testModerator.setIsModerator(1);
        service.saveUser(testModerator);
    }

    private void deleteUser() {
        service.deleteUser(testUser);
    }

    @SneakyThrows
    private MultipartFile getAvatar(String path) {
        return new MockMultipartFile("file", new FileInputStream(new File(path)));
    }

    private CaptchaCode getCaptchaCode() {
        return captchaCodeService
                .findCaptchaCodeBySecretCode(
                        captchaCodeService
                                .createNewCaptcha().get("secretCode").toString());
    }


    @Test
    @Transactional
    public void testPostApiAuthLoginWrongPassword() {
        saveUser();
        RequestApiAuthLogin request = new RequestApiAuthLogin();
        request.setEmail(testUser.getEmail());
        request.setPassword(testUser.getPassword());

        ResponseEntity<?> response = service.postApiAuthLogin(request);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(false), response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiAuthLoginUserNotFound() {
        saveUser();
        RequestApiAuthLogin request = new RequestApiAuthLogin();
        request.setEmail("123");
        request.setPassword(userPassword);

        ResponseEntity<?> response = service.postApiAuthLogin(request);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(false), response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiAuthLogin200() {
        saveUser();
        RequestApiAuthLogin request = new RequestApiAuthLogin();
        request.setEmail(testUser.getEmail());
        request.setPassword(userPassword);

        ResponseEntity<?> response = service.postApiAuthLogin(request);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultUserResponse(
                        new UserResponse(
                                testUser.getId(),
                                testUser.getName(),
                                testUser.getPhoto(),
                                testUser.getEmail(),
                                testUser.getIsModerator() == 1,
                                service.getModerationCount(testUser),
                                testUser.getIsModerator() == 1)),
                response.getBody()
                );

        deleteUser();
    }


    @Test
    @Transactional
    public void testGetApiAuthCheckUserNotFound() {
        authUser();
        testUser.setEmail("bcjddhvsidos");
        saveUser();
        ResponseEntity<?> response = service.getApiAuthCheck();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(false), response.getBody());
        deleteUser();
    }


    @Test
    @Transactional
    public void testGetApiAuthCheckAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "anonymous",
                "anonymous",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        ));
        ResponseEntity<?> response = service.getApiAuthCheck();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(false), response.getBody());
        deleteUser();
    }


    @Test
    @Transactional
    public void testGetApiAuthCheckAuthNull() {
        ResponseEntity<?> response = service.getApiAuthCheck();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(false), response.getBody());
        deleteUser();
    }


    @Test
    @Transactional
    public void testGetApiAuthCheckTrue() {
        authUser();
        ResponseEntity<?> response = service.getApiAuthCheck();

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultUserResponse(
                        new UserResponse(
                                testUser.getId(),
                                testUser.getName(),
                                testUser.getPhoto(),
                                testUser.getEmail(),
                                testUser.getIsModerator() == 1,
                                service.getModerationCount(testUser),
                                testUser.getIsModerator() == 1
                        )),
                response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiAuthRestoreWrongEmail() {
        testUser.setCode(null);
        saveUser();
        RequestApiAuthRestore requestBody = new RequestApiAuthRestore();
        requestBody.setEmail("1");

        ResponseEntity<?> response = service.postApiAuthRestore(requestBody);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(false), response.getBody());
        User dbUser = service.findUserByLogin(testUser.getEmail());
        Assert.assertNull(dbUser.getCode());
        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiAuthRestore200() {
        testUser.setCode(null);
        testUser.setEmail(realEmail);
        saveUser();
        RequestApiAuthRestore requestBody = new RequestApiAuthRestore();
        requestBody.setEmail(testUser.getEmail());

        ResponseEntity<?> response = service.postApiAuthRestore(requestBody);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());
        User dbUser = service.findUserByLogin(testUser.getEmail());
        Assert.assertNotNull(dbUser.getCode());
        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiAuthPasswordWrongCaptcha() {
        testUser.setCode("testCode");
        saveUser();
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthPassword request = new RequestApiAuthPassword(
                "testCode",
                testUser.getPassword(),
                captchaCode.getCode() + "1",
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthPassword(request);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(new ErrorsResponse(
                                true,
                                true,
                                false,
                                false,
                                false)),
                response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiAuthPasswordWrongPassword() {
        testUser.setCode("testCode");
        saveUser();
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthPassword request = new RequestApiAuthPassword(
                "testCode",
                "1",
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthPassword(request);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(new ErrorsResponse(
                                true,
                                false,
                                true,
                                false,
                                false)),
                response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiAuthPasswordWrongCode() {
        testUser.setCode("testCode");
        saveUser();
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthPassword request = new RequestApiAuthPassword(
                "wrongCode",
                testUser.getPassword(),
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthPassword(request);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(new ErrorsResponse(
                                false,
                                true,
                                true,
                                false,
                                false)),
                response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
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

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
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

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
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

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(new ErrorsResponse(
                true,
                        true,
                        true,
                        true,
                        false)),
                response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
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

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(new ErrorsResponse(
                        true,
                        true,
                        true,
                        false,
                        true)),
                response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
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

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(new ErrorsResponse(
                        true,
                        false,
                        true,
                        false,
                        false)),
                response.getBody());
        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiAuthRegisterWrongCaptcha() {
        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                testUser.getName(),
                testUser.getPassword(),
                "1",
                "2"
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(new ErrorsResponse(
                        true,
                        true,
                        false,
                        false,
                        false)),
                response.getBody());
        deleteUser();
    }


    @Test
    @Transactional
    public void testGetApiAuthLogout() {
        authUser();
        ResponseEntity<?> response = service.getApiAuthLogout();

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Assert.assertEquals("anonymous", userDetails.getUsername());

        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiProfileMyNoChanePhotoAndPassword() {
        authUser();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail());
        requestBody.put("name",testUser.getName());
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiProfileMyWithChangePasswordWithoutChangePhoto() {
        authUser();
        JSONObject requestBody = new JSONObject();
        String email = testUser.getEmail();
        String name = testUser.getName();
        String password = testUser.getPassword();
        requestBody.put("email",email + "1");
        requestBody.put("name",name + "1");
        requestBody.put("password",password + "1");
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());
        Assert.assertNotEquals(password, testUser.getPassword());
        Assert.assertNotEquals(email, testUser.getEmail());
        Assert.assertNotEquals(name, testUser.getName());

        deleteUser();
    }


    @Test
    @Transactional
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

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());
        Assert.assertNotNull(repository.findUserByEmail(testUser.getEmail()).getPhoto());
        Assert.assertNotEquals(service.encodePassword(userPassword), testUser.getPassword());

        deleteUser();
    }


    @Test
    @Transactional
    @SneakyThrows
    public void testPostApiProfileMyDeletePhotoWithoutChangePassword() {
        authUser();
        service.saveAvatar(testUser, new FileInputStream(new File(testFilePath)));
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail());
        requestBody.put("name",testUser.getName());
        requestBody.put("removePhoto","1");
        requestBody.put("photo", "");
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());
        Assert.assertEquals("", service.findUserByLogin(testUser.getEmail()).getPhoto());

        deleteUser();
    }


    @Test
    @Transactional
    @SneakyThrows
    public void testPostApiProfileMyUserNotFound() {
        authUser();
        deleteUser();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail());
        requestBody.put("name",testUser.getName());
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(
                ErrorsResponse.builder().user("Пользователь не найден!").build()),
                response.getBody());
        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiProfileMyWrongName() {
        authUser();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail());
        requestBody.put("name","");
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(ErrorsResponse.builder().name("Имя указано неверно").build()),
                response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiProfileMyWrongEmail() {
        authUser();
        saveModerator();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testModerator.getEmail());
        requestBody.put("name",testUser.getName());
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(
                ErrorsResponse.builder().email("Этот e-mail уже зарегистрирован").build()),
                response.getBody());

        deleteUser();
        service.deleteUser(testModerator);
    }


    @Test
    @Transactional
    public void testPostApiProfileMyWrongPassword() {
        authUser();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail());
        requestBody.put("name",testUser.getName());
        requestBody.put("password","1");
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(
                        ErrorsResponse.builder().password("Пароль короче 6-ти символов").build()),
                response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
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

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(
                        ErrorsResponse.builder().photo("Фото слишком большое, нужно не более 5 Мб").build()),
                response.getBody());

        deleteUser();
    }


    @Test
    @Transactional
    public void testPostApiImage200() {
        authUser();
        MultipartFile avatar = getAvatar(testFilePath);

        ResponseEntity<?> response = service.postApiImage(avatar);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotEquals("", response.getBody());
        deleteUser();
    }
}