package main.com.skillbox.ru.developerspublics.service;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.response.ResultResponse;
import main.com.skillbox.ru.developerspublics.api.response.ResultUserResponse;
import main.com.skillbox.ru.developerspublics.api.response.UserResponse;
import main.com.skillbox.ru.developerspublics.model.Role;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.repository.UsersRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;


@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public JavaMailSender emailSender;

    private final HashMap<String, Integer> httpSession = new HashMap<>(); //<sessionId, userId>

    private final String rootPage = "localhost:8080";


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findUserByLogin(email);

        if (user == null) throw new UsernameNotFoundException("User not found");

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

//    public List<User> allUsers() {
//        return new ArrayList<>(userRepository.findAll());
//    }

    public boolean isPasswordCorrect(User user, String password) {
        return bCryptPasswordEncoder.matches(password, user.getPassword());
    }

    public boolean saveUser(User user) {
        //ищем пользователя в БД
        User userFromDB = null;
        if (userRepository.findById(user.getId()).isPresent()) {
            userFromDB = userRepository.findById(user.getId()).get();
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

    public String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    public void addHttpSession(String sessionId, Integer userId) {
        httpSession.put(sessionId, userId);
    }

    public boolean isHttpSessionSaved(int userId) {
        return httpSession.containsValue(userId);
    }

    public void deleteHttpSession(int userId) {
        for (String key : httpSession.keySet()) {
            if (httpSession.get(key) == userId) {
                httpSession.remove(key);
                break;
            }
        }
    }

    public int getModerationCount(User user) {
        return user.getIsModerator() == 1 ? userRepository.getModerationCount() : 0;
    }

    public boolean sendEmail(User user) {
        boolean result = true;
        try {
            String hash = bCryptPasswordEncoder.encode(Long.toString(System.currentTimeMillis()))
                    .substring(10).toLowerCase();

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

    @SneakyThrows
    public void removePhoto(User user) {
        String path = user.getPhoto();
        user.setPhoto("");
        userRepository.save(user);
        File avatar = new File(path);
        if (avatar.delete()) System.out.println("avatar deleted");
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

        String path = getAvatarPath(hash[0], hash[1], hash[2]);
        String name = hash[3] + ".jpg";

        user.setPhoto(path + name);
        userRepository.save(user);
        changeUserPhoto(path, name, inputStream);

        return path;
    }

    @SneakyThrows
    public Resource getAvatar(String a, String b, String c, String name) {
        return new FileSystemResource(getAvatarPath(a, b, c) + name);
    }

    public String getAvatarPath(String a, String b, String c) {
        return File.separator + "upload" + File.separator + a +
                File.separator + b + File.separator + c + File.separator;
    }

    public ResultUserResponse getResultUserResponse(User user) {
        return new ResultUserResponse(
                new ResultResponse(true),
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
        return userRepository.findByCode(code);
    }
}
