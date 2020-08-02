package main.com.skillbox.ru.developerspublics.service;


import main.com.skillbox.ru.developerspublics.model.Role;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.repository.UsersRepository;
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

import javax.mail.internet.MimeMessage;
import java.util.*;


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
}
