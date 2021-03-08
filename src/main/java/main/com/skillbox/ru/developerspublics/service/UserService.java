package main.com.skillbox.ru.developerspublics.service;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthLogin;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthPassword;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRegister;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRestore;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.model.Role;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.PostCommentsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.PostVotesRepository;
import main.com.skillbox.ru.developerspublics.model.repository.PostsRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Service
public class UserService implements UserDetailsService {

  private final UsersRepository userRepository;
  private final PostsRepository postsRepository;
  private final PostCommentsRepository postCommentsRepository;
  private final PostVotesRepository postVotesRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final JavaMailSender emailSender;
  private final CaptchaCodeService captchaCodeService;
  private final UploadsService uploadsService;

  @Value("${blog.host}")
  private String rootPage;

  @Value("${uploads.path}")
  private String uploadsPath;

  @Value("${avatar.path}")
  private String avatarPath;

  @Value("${uploads.home}")
  private String uploadsHome;

  @Value("${avatar.width}")
  private int avatarWidth;

  @Value("${avatar.height}")
  private int avatarHeight;

  @Value(("${uploads.width}"))
  private float uploadsMaxWidth;

  @Autowired
  public UserService(
      UsersRepository userRepository,
      PostsRepository postsRepository,
      PostCommentsRepository postCommentsRepository,
      PostVotesRepository postVotesRepository,
      JavaMailSender emailSender,
      CaptchaCodeService captchaCodeService,
      UploadsService uploadsService) {
    this.userRepository = userRepository;
    this.postsRepository = postsRepository;
    this.postVotesRepository = postVotesRepository;
    this.postCommentsRepository = postCommentsRepository;
    this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
    this.emailSender = emailSender;
    this.captchaCodeService = captchaCodeService;
    this.uploadsService = uploadsService;
  }


  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = findUserByLogin(email);
    if (user == null) {
      return null;
    }
    Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
    for (Role role : user.getRoles()) {
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


  public User findUserById(int id) {
    return userRepository.findById(id).orElseGet(User::new);
  }


  private User findUserByCode(String code) {
    return userRepository.findUserByCode(code);
  }


  public int getModerationCount(User user) {
    return user.getIsModerator() == 1 ? postsRepository.getModerationCount() : 0;
  }


  private ResultUserResponse getResultUserResponse(User user) {
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


  private boolean isCorrectUserName(String name) {
    return name.length() > 3 && name.length() < 30;
  }


  public boolean isPasswordCorrect(User user, String password) {
    return bCryptPasswordEncoder.matches(password, user.getPassword());
  }


  public String encodePassword(String password) {
    return bCryptPasswordEncoder.encode(password);
  }


  @Transactional
  public boolean saveUser(User user) {
    //ищем пользователя в БД
    //если уже есть - сохранять нечего
    if (userRepository.findUserByEmail(user.getEmail()) != null) {
      return false;
    }
    //если нет - задаем роль, кодируем пароль и сохраняем в репозиторий

    //  first user = moderator
    if (userRepository.count() == 0) {
      user.setIsModerator(1);
    }
    //  /first user = moderator

    user.setRoles();
    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    userRepository.save(user);
    return true;
  }


  @Transactional
  public void deleteUser(User user) {
    User dbUser = userRepository.findUserByEmail(user.getEmail());
    if (dbUser != null) {
      // delete comments
      if (dbUser.getUserPostComments() != null && dbUser.getUserPostComments().size() != 0) {
        dbUser.getUserPostComments().forEach(postCommentsRepository::delete);
      }
      // delete post votes
      if (dbUser.getUserPostVotes() != null && dbUser.getUserPostVotes().size() != 0) {
        dbUser.getUserPostVotes().forEach(postVotesRepository::delete);
      }
      // delete user
      userRepository.delete(dbUser);
    }
  }


  private boolean changeUserName(User user, String newName) {
    boolean isCorrectName = isCorrectUserName(newName);
    if (isCorrectName) {
      user.setName(newName);
      userRepository.save(user);
    }
    return isCorrectName;
  }


  private void changeUserPassword(User user, String newPassword) {
    String password = bCryptPasswordEncoder.encode(newPassword);
    if (!user.getPassword().equals(password)) {
      user.setPassword(password);
      user.setCode(null);
      userRepository.save(user);
    }
  }


  private boolean changeUserEmail(User user, String email) {
    boolean isEmailNotExist = findUserByLogin(email) == null;
    if (isEmailNotExist) {
      user.setEmail(email);
      userRepository.save(user);
    }
    return isEmailNotExist;
  }


  @Transactional
  void removePhoto(User user) {
    String path = user.getPhoto();
    user.setPhoto("");
    userRepository.save(user);
    new File(uploadsHome + path).delete();
    //backup
    uploadsService.deleteImage(path);
  }


  @Transactional
  @SneakyThrows
  void resizeAndSaveImage(String homePath, String path, String name,
                          InputStream inputStream, int imageHeight, int imageWidth) {
    //получаем исходное изображение
    String imageType = name.substring(name.lastIndexOf(".") + 1);
    if (imageType.equals("")) {
      imageType = "jpg";
      name = name + "." + imageType;
    }
    BufferedImage image = ImageIO.read(inputStream);

    //Сначала грубо уменьшаем до smartStep (width = avatarWidth * smartStep),потом плавно уменьшаем до нужного р-ра
    int smartStep = 4;  //оптимальное значение скорость/качество = 4

    //Вычисляем промежуточные размеры
    int width = imageWidth * smartStep;
    int height = imageHeight * smartStep;

    //Получаем промежуточное изображение
    BufferedImage imageXStep = Scalr.resize(image, Scalr.Method.SPEED, Scalr.Mode.FIT_EXACT,
        width, height, (BufferedImageOp) null);
    imageXStep.flush();

    //Задаем окончательные размеры и плавно сжимаем
    BufferedImage newImage = Scalr.resize(imageXStep, Scalr.Method.ULTRA_QUALITY,
        imageWidth, imageHeight, (BufferedImageOp) null);
    newImage.flush();

    //скидываем на сервер
    File folder = new File(homePath + path);
    if (!folder.exists()) {
      folder.mkdirs();
    }
    File file = new File(folder.getAbsolutePath() + File.separator + name);
    ImageIO.write(newImage, imageType, file);

    //закрываем стрим
    inputStream.close();

    //backup
    uploadsService.saveImage(uploadsHome, path + name);
  }


  @Transactional
  public String saveAvatar(User user, InputStream inputStream) {
    //считаем хэш
    String hashString = Long.toString(user.userHashCode());

    //разбиваем хэш на 4 части
    String[] hash = substringHash(hashString);

    String path = String.join(File.separator, "", avatarPath, hash[0], hash[1], hash[2], "");
    String name = user.getId() + ".jpg";

    user.setPhoto(path + name);
    userRepository.save(user);
    resizeAndSaveImage(uploadsHome, path, name, inputStream, avatarHeight, avatarWidth);

    return path + name;
  }


  @Transactional
  @SneakyThrows
  String saveImage(MultipartFile image) {
    StringBuilder name = new StringBuilder(Objects.requireNonNull(image.getOriginalFilename()));
    if (name.toString().equals("")) {
      name.insert(0, "1.jpg");
    }
    String[] hash = substringHash(Integer.toString(image.hashCode()));
    String pathDB = String
            .join(File.separator, "", uploadsPath, hash[0], hash[1], hash[2], "");
    String pathServer = uploadsHome + pathDB;

    //создаем директрорию, если её нет
    File folder = new File(pathServer);
    if (!folder.exists()) {
      folder.mkdirs();
    }
    //защитимся от перезаписи файлов
    while (true) {
      if (!new File(pathServer + name).exists()) {
        break;
      } else {
        name.insert(0, "0");
      }
    }

    BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
    int height = bufferedImage.getHeight();
    int width = bufferedImage.getWidth();

    //большие картинки сжимаем
    if (width > uploadsMaxWidth) {
      float step = width / uploadsMaxWidth;
      width = (int) (width / step);
      height = (int) (height / step);
      bufferedImage.flush();
      resizeAndSaveImage(uploadsHome, pathDB, name.toString(), image.getInputStream(), height,
          width);
    } //маленькие сохраяняем как есть
    else {
      Files.copy(image.getInputStream(), Path.of(pathServer + name));
      //backup
      uploadsService.saveImage(uploadsHome, pathDB + name);
    }

    return pathDB + name;
  }


  private String[] substringHash(String hashString) {
    String[] hash = new String[4];
    hash[0] = hashString.substring(0, 2);
    hash[1] = hashString.substring(2, 4);
    hash[2] = hashString.substring(4, 6);
    hash[3] = hashString.substring(6);
    return hash;
  }


  @Transactional
  boolean sendEmail(User user) {
    boolean result = true;
    try {
      String hash = bCryptPasswordEncoder.encode(Long.toString(System.currentTimeMillis()))
              .substring(10).toLowerCase().replaceAll("\\W", "");

      user.setCode(hash);
      userRepository.save(user);

      MimeMessage message = emailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      String htmlMsg = "<h3>Здравствуйте, " + user.getName() + "!</h3>" +
          "<p><br>&nbsp;&nbsp;&nbsp;&nbsp;От Вашего имени подана заявка на смену пароля на сайте "
          + rootPage + ".<br>" +
          "Для подтверждения смены пароля перейдите по ссылке " +
          "<a href=\"http://" + rootPage + "/login/change-password/" + hash
          + "\">СМЕНИТЬ ПАРОЛЬ</a>" +
          "<br><br>&nbsp;&nbsp;&nbsp;&nbsp;Если вы не инициировали это действие, возможно, " +
          "ваша учетная запись была взломана.<br>" +
          "Пожалуйста, свяжитесь с администрацией сайта " + rootPage + "<br><br>" +
          "С уважением,<br>" +
          "администрация сайта " + rootPage + "</p>";

      message.setContent(htmlMsg, "text/html; charset=utf-8");

      helper.setTo(user.getEmail());

      helper.setSubject("Восстановление пароля на сайте " + rootPage);

      emailSender.send(message);
    } catch (Exception e) {
      e.printStackTrace();
      result = false;
    }
    return result;
  }


  public void authUser(String email, String password) {
    UserDetails user = loadUserByUsername(email);
    SecurityContextHolder
        .getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities()));
  }


  public ResponseEntity<?> getAvatar(String path, String a, String b, String c, String name) {
    String pathDB = String.join(File.separator, "", path, a, b, c, "");
    String pathServer = uploadsHome + pathDB + name;

    Resource file = new FileSystemResource(pathServer);
    if (file.exists()) {
      return ResponseEntity.ok().body(file);
    }

    // <backup>
    if (uploadsService.restoreImage(uploadsHome, pathDB, name)) {
      return ResponseEntity.ok().body(new FileSystemResource(pathServer));
    }
    // </backup>

    return ResponseEntity.notFound().build();
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
    } else {
      return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
    }

    //и заполняем ответ
    return new ResponseEntity<>(getResultUserResponse(authUser), HttpStatus.OK);
  }


  public ResponseEntity<?> getApiAuthCheck() {
    //проверяем сохранён ли идентификатор текущей сессии в списке авторизованных
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    //если не аутентифицирован
    if (authentication == null) {
      return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
    }
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
    if (user == null) {
      return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
    }
    //пробуем отправить письмо - результат учитываем в ответе
    return new ResponseEntity<>(new ResultResponse(sendEmail(user)), HttpStatus.OK);
  }


  public ResponseEntity<?> postApiAuthPassword(RequestApiAuthPassword requestBody) {
    //test code
    boolean isCodeCorrect = false;
    User user = findUserByCode(requestBody.getCode());
    if (user != null) {
      isCodeCorrect = true;
    }

    //test password
    boolean isPasswordCorrect = true;
    String password = requestBody.getPassword();
    if (password.length() < 6) {
      isPasswordCorrect = false;
    }

    //test captcha
    boolean isCaptchaCorrect = false;
    if (captchaCodeService.findCaptchaCodeByCodeAndSecret(
        requestBody.getCaptcha(), requestBody.getCaptchaSecret()) != null) {
      isCaptchaCorrect = true;
    }

    if (isCodeCorrect && isPasswordCorrect && isCaptchaCorrect) {
      changeUserPassword(user, password);
      return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body(new ResultFalseErrorsResponse(new ErrorsResponse(
            isCodeCorrect, isPasswordCorrect, isCaptchaCorrect, false, false))
        );
  }


  public ResponseEntity<?> postApiAuthRegister(RequestApiAuthRegister requestBody) {
    //проверяем email
    String email = requestBody.getEmail();
    boolean isEmailExist = findUserByLogin(email) != null;

    //проверяем name
    String name = requestBody.getName();
    boolean isNameWrong = !isCorrectUserName(name);

    //проверяем password
    String password = requestBody.getPassword();
    boolean isPasswordCorrect = true;
    if (password.length() < 6) {
      isPasswordCorrect = false;
    }

    //проверяем captcha
    boolean isCaptchaCorrect = false;
    if (captchaCodeService
        .findCaptchaCodeByCodeAndSecret(requestBody.getCaptcha(), requestBody.getCaptchaSecret())
        != null) {
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
            true, isPasswordCorrect, isCaptchaCorrect, isEmailExist, isNameWrong))
        );
  }


  public ResponseEntity<?> getApiAuthLogout() {
    SecurityContext securityContext = SecurityContextHolder.getContext();

    User user = findUserByLogin(securityContext.getAuthentication().getName());

    boolean result = false;
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


  @Transactional
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
      if (request.get("email") != null) {
        email = request.get("email").toString();
      }
      if (request.get("name") != null) {
        name = request.get("name").toString();
      }
      if (request.get("password") != null) {
        password = request.get("password").toString();
      }
      if (request.get("removePhoto") != null) {
        removePhoto = request.get("removePhoto").toString();
      }
    }

    //получаем user
    User user = findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
    ErrorsResponse.ErrorsResponseBuilder errorsBuilder = ErrorsResponse.builder();

    if (user == null) {
      return ResponseEntity.status(HttpStatus.OK)
          .body(
              new ResultFalseErrorsResponse(
                  errorsBuilder.user("Пользователь не найден!").build()));
    }

    //проверяем изменение имени
    if (!user.getName().equals(name)) {
      if (!changeUserName(user, name)) {
        errorsBuilder.name("Имя указано неверно");
      }
    }

    //проверяем изменение e-mail
    if (!user.getEmail().equals(email)) {
      if (!changeUserEmail(user, email)) {
        errorsBuilder.email("Этот e-mail уже зарегистрирован");
      }
    }

    //проверяем изменение пароля
    if (password != null) {
      if (password.length() >= 6) {
        changeUserPassword(user, password);
      } else {
        errorsBuilder.password("Пароль короче 6-ти символов");
      }
    }

    if (removePhoto != null) {
      //удаление фото
      if (removePhoto.equals("1")) {
        removePhoto(user);
      }
      //изменение фото
      else if (removePhoto.equals("0")) {
        if (avatar.getSize() <= 5 * 1024 * 1024) {
          saveAvatar(user, avatar.getInputStream());
        } else {
          errorsBuilder.photo("Фото слишком большое, нужно не более 5 Мб");
        }
      }
    }

    if (!errorsBuilder.build().equals(new ErrorsResponse())) {
      return ResponseEntity.status(HttpStatus.OK)
          .body(new ResultFalseErrorsResponse(errorsBuilder.build()));
    }
    return ResponseEntity.status(HttpStatus.OK).body(new ResultResponse(true));
  }


  @Transactional
  public ResponseEntity<?> postApiImage(MultipartFile image) {
    return ResponseEntity.status(HttpStatus.OK).body(saveImage(image));
  }
}