package main.com.skillbox.ru.developerspublics.controllers;


import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthPassword;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRegister;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRestore;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthLogin;
import main.com.skillbox.ru.developerspublics.service.CaptchaCodeService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class ApiAuthController
{
    @Autowired
    private CaptchaCodeService captchaCodeService;
    @Autowired
    private UserService userService;

    private final String USER = "ROLE_USER";


    //POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> postApiAuthLogin(@RequestBody RequestApiAuthLogin requestApiAuthLogin) {
        return userService.postApiAuthLogin(requestApiAuthLogin);
    }


    //GET /api/auth/check
    @GetMapping("/check")
    public ResponseEntity<?> authCheck() {
        return userService.getApiAuthCheck();
    }


    //POST /api/auth/restore
    @PostMapping("/restore")
    public ResponseEntity<?> postApiAuthRestore(@RequestBody RequestApiAuthRestore requestBody) {
        return userService.postApiAuthRestore(requestBody);
    }


    //POST /api/auth/password
    @PostMapping("/password")
    public ResponseEntity<?> postApiAuthPassword(@RequestBody RequestApiAuthPassword requestBody) {
        return userService.postApiAuthPassword(requestBody);
    }


    //POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> postApiAuthRegister(@RequestBody RequestApiAuthRegister requestBody) {
        return userService.postApiAuthRegister(requestBody);
    }


    //GET /api/auth/captcha
    @GetMapping("/captcha")
    public ResponseEntity<?> getApiAuthCaptcha() {
        return captchaCodeService.getApiAuthCaptcha();
    }


    //GET /api/auth/logout
    @Secured(USER)
    @GetMapping("/logout")
    public ResponseEntity<?> getApiAuthLogout() {
        return userService.getApiAuthLogout();
    }
}