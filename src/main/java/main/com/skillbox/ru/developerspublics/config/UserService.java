package main.com.skillbox.ru.developerspublics.config;

import lombok.Data;
import main.com.skillbox.ru.developerspublics.model.Role;
import main.com.skillbox.ru.developerspublics.model.User;
import main.com.skillbox.ru.developerspublics.model.enums.Roles;
import main.com.skillbox.ru.developerspublics.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
@Data
public class UserService implements UserDetailsService
{
    @Autowired
    private UsersRepository userRepository;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private HashMap<String, Integer> httpSessions = new HashMap<>();


    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = null;
        for (User user1 : userRepository.findAll()) {
            if (user1.getName().equals(userName)) {
                user = user1;
                break;
            }
        }

        System.out.println("loadUserByUsername");

        if (user == null) {
            //создаем гостевой аккаунт TODO
            user = new User(Roles.GUEST.name());
//            throw new UsernameNotFoundException("User not found");
        }

        return new org.springframework.security.core.userdetails.User(user.getName(), user.getPassword(),
                user.getAuthorities());
    }


    public User findUserById(int userId) {  //  TODO
        System.out.println("findUserById");
        //ищем в БД
        Optional<User> userFromDb = userRepository.findById(userId);
        //если не нашли - возвращаем гостя
        return userFromDb.orElse(new User(Roles.GUEST.name()));
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
        System.out.println("saveUser");
        //ищем пользователя в БД
        User userFromDB = null;
        if (userRepository.findById(user.getId()).isPresent()) {
            userFromDB = userRepository.findById(user.getId()).get();
        }



        System.out.println(" -> " + user);



        //если уже есть - сохранять нечего
        if (userFromDB != null) {
            return false;
        }
        //если нет - задаем роль, кодируем пароль и сохраняем в репозиторий
        user.setRoles(Collections.singleton(new Role(Roles.USER)));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }

//    public boolean deleteUser(int userId) {
//        if (userRepository.findById(userId).isPresent()) {
//            userRepository.deleteById(userId);
//            return true;
//        }
//        return false;
//    }

    public void addHttpSession(String sessionId, int userId) { //TODO придумать где хранить сессию
        System.out.println(sessionId + " addHttpSession " + userId);
        httpSessions.put(sessionId, userId);
    }
}
