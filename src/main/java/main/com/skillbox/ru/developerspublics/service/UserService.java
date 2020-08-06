package main.com.skillbox.ru.developerspublics.service;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.model.Role;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.repository.UsersRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;


@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public JavaMailSender emailSender;

    private HashMap<String, Integer> httpSession = new HashMap<>(); //<sessionId, userId>


    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = null;
        for (User userDB : userRepository.findAll()) {
            System.out.println(userDB.getEmail() + " <==> " + login);
            if (userDB.getUsername().equals(login)) {
                user = userDB;
            }
        }

        System.out.println("\t user == " + user);
        if (user == null) throw new UsernameNotFoundException("User not found");

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : user.getRoles()){
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getAuthority()));
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                grantedAuthorities
        );

        System.out.println("userDetails " + userDetails);
        return userDetails;
    }

    public User findUserByLogin(String login) {
        System.out.println("findUserByName ==> " + login);
        //ищем в БД
        for (User user : userRepository.findAll()) {
            if (user.getEmail().equals(login)) {
                return user;
            }
        }
        //если не нашли - возвращаем null
        return null;
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElseGet(User::new);
    }

    public List<User> allUsers() {
        System.out.println("allUsers");
        List<User> users = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            users.add(user);
        }
        return users;
    }

    public boolean isPasswordCorrect(User user, String password) {
        System.out.println("isPasswordCorrect");
        return bCryptPasswordEncoder.matches(password, user.getPassword());
    }

    public boolean saveUser(User user) {
        System.out.print("saveUser");
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

    public Object encodePassword(String password) {
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
        int moderationCount = 0;
        if (user.getIsModerator() == 1) {
            for (Post post : postService.getPosts()) {
                if (post.getModerationStatus().equals(ModerationStatuses.NEW.toString())) {
                    moderationCount++;
                }
            }
        }
        return moderationCount;
    }

    public boolean sendEmail(User user) {
        boolean result = true;
        try {
            String hash = Integer.toString(user.hashCode());

            user.setCode(hash);

            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

            helper.setTo(user.getEmail());

            helper.setSubject("Восстановление пароля на сайте developerspublics.ru");

            String htmlMsg = "<h3>Здравствуйте, " + user.getName() + "!</h3>\n" +
                    "\tОт Вашего имени подана заявка на смену пароля на сайте developerspublics.ru.\n" +
                    "Для подтверждения смены пароля перейдите по ссылке http://localhost/login/change-password/"
                    + hash + "\nЕсли вы не инициировали это действие, возможно, ваша учетная запись была " +
                    "взломана. \nПожалуйста, свяжитесь с администрацией сайта developerspublics.ru\n\nС уважением,\n" +
                    "администрация сайта developerspublics.ru";

            message.setContent(htmlMsg, "text/html");

            this.emailSender.send(message);
        }
        catch (Exception e) {
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
        return name.replaceAll("[0-9a-zA-Zа-яА-ЯёЁ]", "").equals("") &&
                !name.equals("") && name.length() > 3;
    }

    public boolean isEmailExist(String email) {
        boolean isEmailExist = false;
        for (User user : userRepository.findAll()) {
            if (user.getEmail().equals(email)) {
                isEmailExist = true;
                break;
            }
        }
        return isEmailExist;
    }

    public void changeUserPassword(User user, String newPassword) {
        String password = bCryptPasswordEncoder.encode(newPassword);
        if (!user.getPassword().equals(password)) {
            user.setPassword(password);
            userRepository.save(user);
        }
    }

    public boolean changeUserEmail(User user, String email) {
        boolean isEmailNotExist = !isEmailExist(email);
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
        //TODO удалить файл с сервера!
        File avatar = new File(new URI("localhost:8080" + path));
        if (avatar.delete()) System.out.println("avatar deleted");
    }

    @SneakyThrows
    public void changeUserPhoto(User user, InputStream inputStream) {
        String path = "localhost:8080" + user.getPhoto();
        //подключаемся к серверу
        HttpURLConnection httpConn = (HttpURLConnection) new URL(path).openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        //открываем стрим
        OutputStream outputStream = httpConn.getOutputStream();

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
        BufferedImage imageXStep = Scalr.resize(image, Scalr.Method.SPEED,
                width, height, null);

        //Задаем окончательные размеры и плавно сжимаем
        BufferedImage newImage = Scalr.resize(imageXStep, Scalr.Method.ULTRA_QUALITY,
                newWidth, newHeight, null);

        //Записываем результат в файл
        ImageIO.write(newImage, "jpg", outputStream);

        //сохраняемся и разрываем соединение
        outputStream.flush();
        inputStream.close();
        httpConn.disconnect();
    }

    public String saveAvatar(User user, InputStream inputStream) { //TODO
        //считаем хэш
        String hashString = Integer.toString(user.hashCode());

        //разбиваем хэш на 4 части
        String[] hash = new String[4];
        hash[0] = hashString.substring(0, 1);
        hash[1] = hashString.substring(2, 3);
        hash[2] = hashString.substring(4, 5);
        hash[3] = hashString.substring(6);

        String path = "/upload/" + hash[0] + "/" + hash[1] + "/" + hash[2] + "/" + hash[3] + ".jpg";

        user.setPhoto(path);
        userRepository.save(user);
        changeUserPhoto(user, inputStream);

        return path;
    }
}
