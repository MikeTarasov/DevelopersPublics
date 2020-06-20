package main.com.skillbox.ru.developerspublics.config;

import lombok.Data;
import main.com.skillbox.ru.developerspublics.model.Role;
import main.com.skillbox.ru.developerspublics.model.User;
import main.com.skillbox.ru.developerspublics.model.enums.Roles;
import main.com.skillbox.ru.developerspublics.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Data
public class UserService implements UserDetailsService
{
    @Autowired
    private UsersRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = null;
        for (User user1 : userRepository.findAll()) {
            if (user1.getName().equals(userName)) {
                user = user1;
                break;
            }
        }

        if (user == null) {
            //создаем гостевой аккаунт
            user = new User(Roles.GUEST.name());
//            throw new UsernameNotFoundException("User not found");
        }

        return new org.springframework.security.core.userdetails.User(user.getName(), user.getPassword(),
                user.getAuthorities());
    }

//  TODO
    public User findUserById(int userId) {
        Optional<User> userFromDb = userRepository.findById(userId);
        return userFromDb.orElse(new User());
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            users.add(user);
        }
        return users;
    }

    public boolean saveUser(User user) {
        User userFromDB = null;
        if (userRepository.findById(user.getId()).isPresent()) {
            userFromDB = userRepository.findById(user.getId()).get();
        }

        if (userFromDB != null) {
            return false;
        }

        user.setRoles(Collections.singleton(new Role(Roles.GUEST)));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }

    public boolean deleteUser(int userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }
}
