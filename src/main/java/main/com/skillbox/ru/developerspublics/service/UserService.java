package main.com.skillbox.ru.developerspublics.service;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthLogin;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthPassword;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRegister;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRestore;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.config.AuthenticationProviderImpl;
import main.com.skillbox.ru.developerspublics.model.Role;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.UsersRepository;
import org.imgscalr.Scalr;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;


@Service
public class UserService implements UserDetailsService {
    private final UsersRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JavaMailSender emailSender;
    private final AuthenticationProviderImpl authenticationProvider;
    private final CaptchaCodeService captchaCodeService;

    @Value("${blog.host}")
    private String rootPage;

    @Value("${uploads.path}")
    private String uploadsPath;

    @Value("${uploads.home}")
    private String uploadsHome;

    @Autowired
    public UserService (UsersRepository userRepository,
                        BCryptPasswordEncoder bCryptPasswordEncoder,
                        JavaMailSender emailSender,
                        AuthenticationProviderImpl authenticationProvider,
                        CaptchaCodeService captchaCodeService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailSender = emailSender;
        this.authenticationProvider = authenticationProvider;
        this.captchaCodeService = captchaCodeService;
    }


    @Override
    @SneakyThrows
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findUserByLogin(email);

        if (user == null) return null;
//            throw new UsernameNotFoundException("User not found");

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : user.getRoles()){
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getAuthority()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                grantedAuthorities
        );
    }


    public User findUserByLogin(String email) {
        return userRepository.findUserByEmail(email);
    }


    public User getUserById(int id) {
        return userRepository.findById(id).orElseGet(User::new);
    }


    public boolean isPasswordCorrect(User user, String password) {
        return bCryptPasswordEncoder.matches(password, user.getPassword());
    }


    public boolean saveUser(User user) {
        //ищем пользователя в БД
        User userFromDB = null;
        if (userRepository.findUserByEmail(user.getEmail()) != null) {
            System.out.println(user.getId());
            userFromDB = userRepository.findUserByEmail(user.getEmail());
        }

        //если уже есть - сохранять нечего
        if (userFromDB != null) {
            return false;
        }
        //если нет - задаем роль, кодируем пароль и сохраняем в репозиторий
        user.setRoles();
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }


    public String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }


    public int getModerationCount(User user) {
        return user.getIsModerator() == 1 ? userRepository.getModerationCount() : 0;
    }


    public boolean sendEmail(User user) {
        boolean result = true;
        try {
            String hash = bCryptPasswordEncoder.encode(Long.toString(System.currentTimeMillis()))
                    .substring(10).toLowerCase().replaceAll("\\W", "");

            user.setCode(hash);
            userRepository.save(user);

            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String htmlMsg = "<h3>Здравствуйте, " + user.getName() + "!</h3>" +
                    "<p><br>&nbsp;&nbsp;&nbsp;&nbsp;От Вашего имени подана заявка на смену пароля на сайте developerspublics.ru.<br>" +
                    "Для подтверждения смены пароля перейдите по ссылке " +
                    "<a href=\"http://" + rootPage + "/login/change-password/"+hash+"\">СМЕНИТЬ ПАРОЛЬ</a>" +
                    "<br><br>&nbsp;&nbsp;&nbsp;&nbsp;Если вы не инициировали это действие, возможно, ваша учетная запись была взломана.<br>" +
                    "Пожалуйста, свяжитесь с администрацией сайта developerspublics.ru<br><br>" +
                    "С уважением,<br>" +
                    "администрация сайта developerspublics.ru</p>";

            message.setContent(htmlMsg, "text/html; charset=utf-8");

            helper.setTo(user.getEmail());

            helper.setSubject("Восстановление пароля на сайте developerspublics.ru");

            emailSender.send(message);
        }
        catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }


    public boolean changeUserName(User user, String newName) {
        boolean isCorrectName = isCorrectUserName(newName);
        if (isCorrectName) {
            user.setName(newName);
            userRepository.save(user);
        }
        return isCorrectName;
    }


    public boolean isCorrectUserName(String name) {
        return name.length() > 3 && name.length() < 30;
    }


    public void changeUserPassword(User user, String newPassword) {
        String password = bCryptPasswordEncoder.encode(newPassword);
        if (!user.getPassword().equals(password)) {
            user.setPassword(password);
            user.setCode(null);
            userRepository.save(user);
        }
    }


    public boolean changeUserEmail(User user, String email) {
        boolean isEmailNotExist = findUserByLogin(email) == null;
        if (isEmailNotExist) {
            user.setEmail(email);
            userRepository.save(user);
        }
        return isEmailNotExist;
    }


    public void removePhoto(User user) {
        String path = user.getPhoto();
        user.setPhoto("");
        userRepository.save(user);
        File avatar = new File(path);
        avatar.delete();
    }


    @SneakyThrows
    private void changeUserPhoto(String path, String name, InputStream inputStream) {
        //сжимаем до 36*36 пикс
        int newWidth = 36;
        int newHeight = 36;
        //получаем исходное изображение
        BufferedImage image = ImageIO.read(inputStream);

        //Сначала грубо уменьшаем до smartStep (width = newWidth * smartStep),потом плавно уменьшаем до нужного р-ра
        int smartStep = 4;  //оптимальное значение скорость/качество = 4

        //Вычисляем промежуточные размеры
        int width = newWidth * smartStep;
        int height = newHeight * smartStep;

        //Получаем промежуточное изображение
        BufferedImage imageXStep = Scalr.resize(image, Scalr.Method.SPEED, Scalr.Mode.FIT_EXACT,
                width, height, null);
        imageXStep.flush();

        //Задаем окончательные размеры и плавно сжимаем
        BufferedImage newImage = Scalr.resize(imageXStep, Scalr.Method.ULTRA_QUALITY,
                newWidth, newHeight, null);
        newImage.flush();

        //скидываем на сервер
        File folder = new File(path);
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder.getAbsolutePath() + File.separator + name);
        ImageIO.write(newImage, "jpg", file);

        //закрываем стрим
        inputStream.close();
    }


    public String saveAvatar(User user, InputStream inputStream) { //TODO защита от дубля хеша!!!!!
        //считаем хэш
        String hashString = Long.toString(user.userHashCode());

        //разбиваем хэш на 4 части
        String[] hash = new String[4];
        hash[0] = hashString.substring(0, 2);
        hash[1] = hashString.substring(2, 4);
        hash[2] = hashString.substring(4, 6);
        hash[3] = hashString.substring(6);

        String path = File.separator + getAvatarPath(hash[0], hash[1], hash[2]);
        String name = hash[3] + ".jpg";

        user.setPhoto(path + name);
        userRepository.save(user);
        changeUserPhoto(uploadsHome + path, name, inputStream);

        return path;
    }


    public ResponseEntity<?> getAvatar(String a, String b, String c, String name) {
        System.out.println(uploadsHome + getAvatarPath(a, b, c) + name);

        Resource file = new FileSystemResource(uploadsHome + getAvatarPath(a, b, c) + name);

        if (file.exists()) return ResponseEntity.ok().body(file);

        return ResponseEntity.notFound().build();
    }


    public String getAvatarPath(String a, String b, String c) {
        return String.join(File.separator, uploadsPath, a, b, c) + File.separator;
    }


    public ResultUserResponse getResultUserResponse(User user) {
        return new ResultUserResponse(
                new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getPhoto(),
                        user.getEmail(),
                        user.getIsModerator() == 1,
                        getModerationCount(user),
                        user.getIsModerator() == 1
                )
        );
    }


    public User getUserByCode(String code) {
        return userRepository.findUserByCode(code);
    }


    public ResponseEntity<?> postApiAuthLogin(RequestApiAuthLogin requestApiAuthLogin) {
        //пробуем найти пользователя в БД
        User authUser = findUserByLogin(requestApiAuthLogin.getEmail());

        //если не нашли
        if (authUser == null) {
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }

        //если нашли - проверяем пароль и заносим user'а в контекст
        if (isPasswordCorrect(authUser, requestApiAuthLogin.getPassword())) {
            authUser(authUser.getEmail(), requestApiAuthLogin.getPassword());
        }
        else {
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }

        //и заполняем ответ
        return new ResponseEntity<>(getResultUserResponse(authUser), HttpStatus.OK);
    }


    public void authUser(String email, String password) {
        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authenticationProvider
                                .authenticate(
                                        new UsernamePasswordAuthenticationToken(loadUserByUsername(email), password)));
    }


    public ResponseEntity<?> authCheck() {
        //проверяем сохранён ли идентификатор текущей сессии в списке авторизованных
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //если не авторизован
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }

        //вытаскиваем пользователя
        User user = findUserByLogin(authentication.getName());

        //если не нашли
        if (user == null) {
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }

        //собираем ответ
        return new ResponseEntity<>(getResultUserResponse(user), HttpStatus.OK);
    }


    public ResponseEntity<?> postApiAuthRestore(RequestApiAuthRestore requestBody) {
        //ищем юзера по введенному е-мэйлу
        User user = findUserByLogin(requestBody.getEmail());
        //если не нашли - выдаем ошибку
        if (user == null) return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        //пробуем отправить письмо - результат учитываем в ответе
        return new ResponseEntity<>(new ResultResponse(sendEmail(user)), HttpStatus.OK);
    }


    public ResponseEntity<?> postApiAuthPassword(RequestApiAuthPassword requestBody) {
        String codeRestore = requestBody.getCode();
        String password = requestBody.getPassword();
        String codeCaptcha = requestBody.getCaptcha();
        String captchaSecret = requestBody.getCaptchaSecret();

        boolean isCodeCorrect = false;
        boolean isPasswordCorrect = true;
        boolean isCaptchaCorrect = false;

        //test code
        User user = getUserByCode(codeRestore);
        if (user != null) isCodeCorrect = true;

        //test password
        if (password.length() < 6) isPasswordCorrect = false;

        //test captcha
        if (captchaCodeService.getCaptchaCodeByCodeAndSecret(codeCaptcha, captchaSecret) != null) {
            isCaptchaCorrect = true;
        }

        if (isCodeCorrect && isPasswordCorrect && isCaptchaCorrect) {
            changeUserPassword(user, password);
            return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
        }


        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultFalseErrorsResponse(new ErrorsResponse(
                        isCodeCorrect, isPasswordCorrect,isCaptchaCorrect, false, false))
                );
    }


    public ResponseEntity<?> postApiAuthRegister(RequestApiAuthRegister requestBody) {
        boolean isPasswordCorrect = true;
        boolean isCaptchaCorrect = false;

        String email = requestBody.getEmail();
        String name = requestBody.getName();
        String password = requestBody.getPassword();
        String captchaCode = requestBody.getCaptcha();
        String captchaSecret = requestBody.getCaptchaSecret();

        //проверяем email
        boolean isEmailExist = findUserByLogin(email) != null;

        //проверяем name
        boolean isNameWrong = !isCorrectUserName(name);

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
            boolean isUserSaved = saveUser(user);
            //собираем ответ
            return new ResponseEntity<>(new ResultResponse(isUserSaved), HttpStatus.OK);
        }

        //есть ошибки - собираем сообщение об ошибках
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultFalseErrorsResponse(new ErrorsResponse(
                        false, isPasswordCorrect, isCaptchaCorrect, isEmailExist, isNameWrong))
                );
    }


    public ResponseEntity<?> getApiAuthLogout() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        boolean result = false;

        User user = findUserByLogin(securityContext.getAuthentication().getName());

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

            result = true;
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultResponse(result));
    }


    @SneakyThrows
    public ResponseEntity<?> postApiProfileMy(String requestBody,
                                              MultipartFile avatar,
                                              String emailMP,
                                              String nameMP,
                                              String passwordMP,
                                              String removePhotoMP) {
        //if consumes = "multipart/form-data"
        String email = emailMP;
        String name = nameMP;
        String password = passwordMP;
        String removePhoto = removePhotoMP;


        //else consumes = "application/json"
        if (requestBody != null) {
            JSONObject request = (JSONObject) new JSONParser().parse(requestBody);
            if (request.get("email") != null) email = request.get("email").toString();
            if (request.get("name") != null) name = request.get("name").toString();
            if (request.get("password") != null) password = request.get("password").toString();
            if (request.get("removePhoto") != null) removePhoto = request.get("removePhoto").toString();
        }

        //получаем user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = findUserByLogin(authentication.getName());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResultFalseErrorsResponse(
                            ErrorsResponse.builder().user("Пользователь не найден!").build()));
        }

        //проверяем изменение имени
        if (!user.getName().equals(name)) {
            if (!changeUserName(user, name))
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResultFalseErrorsResponse(
                                ErrorsResponse.builder().name("Имя указано неверно").build()));
        }

        //проверяем изменение e-mail
        if (!user.getEmail().equals(email)) {
            if (!changeUserEmail(user, email))
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResultFalseErrorsResponse(
                                ErrorsResponse.builder().email("Этот e-mail уже зарегистрирован").build()));
        }

        //проверяем изменение пароля
        if (password != null) {
            if (password.length() >= 6) {
                changeUserPassword(user, password);
            }
            else
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResultFalseErrorsResponse(
                                ErrorsResponse.builder().password("Пароль короче 6-ти символов").build())); //TODO спросить зачем эта проверка?
        }

        if (removePhoto != null) {
            //удаление фото
            if (removePhoto.equals("1")) {
                removePhoto(user);
            }

            //изменение фото
            if (removePhoto.equals("0")) {
                if (avatar.getSize() <= 5*1024*1024) {
                    InputStream inputStream = avatar.getInputStream();
                    saveAvatar(user, inputStream);
                }
                else
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(new ResultFalseErrorsResponse(
                                    ErrorsResponse
                                            .builder()
                                            .photo("Фото слишком большое, нужно не более 5 Мб")
                                            .build()));
            }
        }

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }


    @SneakyThrows
    public ResponseEntity<?> postApiImage(MultipartFile avatar) {
        //получаем пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = findUserByLogin(authentication.getName());

        String path = "";

        if (avatar != null) {
            InputStream inputStream = avatar.getInputStream();
            path = saveAvatar(user, inputStream);
        }

        return ResponseEntity.status(HttpStatus.OK).body(path);
    }
}