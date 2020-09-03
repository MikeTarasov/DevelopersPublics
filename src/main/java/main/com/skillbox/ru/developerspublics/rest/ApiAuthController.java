package main.com.skillbox.ru.developerspublics.rest;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthPassword;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRegister;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRestore;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.config.AuthenticationProviderImpl;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthLogin;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.service.CaptchaCodeService;
import main.com.skillbox.ru.developerspublics.service.PostService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;


@RestController
@RequestMapping("/api/auth")
public class ApiAuthController
{
    @Autowired
    private PostService postService;

    @Autowired
    private CaptchaCodeService captchaCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationProviderImpl authenticationProvider;

    private final String USER = "ROLE_USER";


    //POST /api/auth/login
    @SneakyThrows
    @PostMapping("/login")
    public ResponseEntity<?> postApiAuthLogin(@RequestBody RequestApiAuthLogin requestApiAuthLogin, HttpSession httpSession) {
        //пробуем найти пользователя в БД
        User authUser = userService.findUserByLogin(requestApiAuthLogin.getEmail());

        //если не нашли
        if (authUser == null) {
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }

        //если нашли - проверяем пароль и заносим user'а в контекст
        if (userService.isPasswordCorrect(authUser, requestApiAuthLogin.getPassword())) {
            SecurityContextHolder.getContext()
                    .setAuthentication(
                            authenticationProvider.authenticate(
                                    new UsernamePasswordAuthenticationToken(
                                            userService.loadUserByUsername(authUser.getEmail()),
                                            requestApiAuthLogin.getPassword())
                            )
                    );
        }
        else {
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }

        //запоминаем сессию
        userService.addHttpSession(httpSession.getId(), authUser.getId());

        //и заполняем ответ
        return new ResponseEntity<>(userService.getResultUserResponse(authUser), HttpStatus.OK);
    }

    //GET /api/auth/check
    @GetMapping("/check")
    public ResponseEntity<?> authCheck(HttpSession httpSession) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //если не авторизован
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }

        //вытаскиваем пользователя
        User user = userService.findUserByLogin(authentication.getName());

        //если не нашли
        if (user == null) {
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }

        //проверяем сохранён ли идентификатор текущей сессии в списке авторизованных TODO спросить зачем это?
        if (!userService.isHttpSessionSaved(user.getId())) {
            userService.addHttpSession(httpSession.toString(), user.getId());
        }

        //собираем ответ
        return new ResponseEntity<>(userService.getResultUserResponse(user), HttpStatus.OK);
    }

    //POST /api/auth/restore
    @SneakyThrows
    @PostMapping("/restore")
    public ResponseEntity<?> postApiAuthRestore(@RequestBody RequestApiAuthRestore requestBody) {
        String email = requestBody.getEmail();

        System.out.println("email = " + email);

        User user = userService.findUserByLogin(email);

        if (user == null) return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);

        return userService.sendEmail(user) ? new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK) :
                new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
    }

    //POST /api/auth/password
    @SneakyThrows
    @PostMapping("/password")
    public ResponseEntity<?> postApiAuthPassword(@RequestBody RequestApiAuthPassword requestBody) {
        String codeRestore = requestBody.getCode();
        String password = requestBody.getPassword();
        String codeCaptcha = requestBody.getCaptcha();
        String captchaSecret = requestBody.getCaptchaSecret();

        boolean isCodeCorrect = false;
        boolean isPasswordCorrect = true;
        boolean isCaptchaCorrect = false;

        //test code
        User user = userService.getUserByCode(codeRestore);
        if (user != null) isCodeCorrect = true;

        //test password
        if (password.length() < 6) isPasswordCorrect = false;

        //test captcha
        if (captchaCodeService.getCaptchaCodeByCodeAndSecret(codeCaptcha, captchaSecret) != null) {
            isCaptchaCorrect = true;
        }

        if (isCodeCorrect && isPasswordCorrect && isCaptchaCorrect) {
            user.setPassword(password);
            return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultFalseErrorsResponse(
                new ResultResponse(false),
                new ErrorsResponse(isCodeCorrect, isPasswordCorrect,isCaptchaCorrect, false, false)
        ));
    }

    //POST /api/auth/register
    @SneakyThrows
    @PostMapping("/register")
    public ResponseEntity<?> postApiAuthRegister(@RequestBody RequestApiAuthRegister requestBody) {
        boolean isPasswordCorrect = true;
        boolean isCaptchaCorrect = false;

        String email = requestBody.getEmail();
        String name = requestBody.getName();
        String password = requestBody.getPassword();
        String captchaCode = requestBody.getCaptcha();
        String captchaSecret = requestBody.getCaptchaSecret();

        //проверяем email
        boolean isEmailExist = userService.findUserByLogin(email) != null;

        //проверяем name
        boolean isNameWrong = !userService.isCorrectUserName(name);

        //проверяем password
        if (password.length() < 6) {
            isPasswordCorrect = false;
        }

        //проверяем captcha
        if (captchaCodeService.getCaptchaCodeByCodeAndSecret(captchaCode, captchaSecret) != null) {
            isCaptchaCorrect = true;
        }

        //собираем ответ
        if (!isEmailExist && !isNameWrong && isPasswordCorrect && isCaptchaCorrect) {
            //создаем new User и отправляем true
            User user = new User(email, name, password);
            boolean isUserSaved = userService.saveUser(user);
            //собираем ответ
            return new ResponseEntity<>(new ResultResponse(isUserSaved), HttpStatus.OK);
        }

        //есть ошибки - собираем сообщение об ошибках
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultFalseErrorsResponse(
                new ResultResponse(false),
                new ErrorsResponse(false, isPasswordCorrect, isCaptchaCorrect, isEmailExist, isNameWrong))
                );
    }

    //GET /api/auth/captcha
    @SneakyThrows
    @GetMapping("/captcha")
    public ResponseEntity<?> getApiAuthCaptcha() {
        //TODO Время устаревания должно быть задано в конфигурации приложения (по умолчанию, 1 час)
        //создадим новую капчу
        JSONObject newCaptcha = captchaCodeService.createNewCaptcha();

        //собираем ответ
        return new ResponseEntity<>(
                new ApiAuthCaptchaResponse(
                        newCaptcha.get("secretCode").toString(),
                        "data:image/png;base64, " + newCaptcha.get("base64").toString()
                ), HttpStatus.OK);
    }

    //GET /api/auth/logout
    @SneakyThrows
    @Secured(USER)
    @GetMapping("/logout")
    public ResponseEntity<?> getApiAuthLogout() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        boolean result = false;

        User user = userService.findUserByLogin(securityContext.getAuthentication().getName());

        if (user != null) {
            Set<GrantedAuthority> grantedAuthority = new HashSet<>();
            grantedAuthority.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));

            securityContext.setAuthentication(
                    new AnonymousAuthenticationToken(
                            String.valueOf(System.currentTimeMillis()),
                            new org.springframework.security.core.userdetails.User(
                                    "anonymous",
                                    "anonymous",
                                    grantedAuthority
                            ),
                            grantedAuthority
                    ));

            userService.deleteHttpSession(user.getId());
            result = true;
        }

//        HttpHeaders headers = new HttpHeaders();
//        headers.setLocation(URI.create("/"));

        return ResponseEntity.status(HttpStatus.OK)
//                .status(HttpStatus.FOUND)
//                .headers(headers)
                .body(new ResultResponse(result));
    }
}