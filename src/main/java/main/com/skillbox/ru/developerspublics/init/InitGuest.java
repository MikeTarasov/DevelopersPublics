package main.com.skillbox.ru.developerspublics.init;


import lombok.Data;
import main.com.skillbox.ru.developerspublics.model.User;
import main.com.skillbox.ru.developerspublics.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class InitGuest
{
    public static void addGuest(UsersRepository usersRepository) {
        for (User user : usersRepository.findAll()) {
            if (user.getName().equals("GUEST")) {
                return;
            }
        }
        usersRepository.save(new User("GUEST"));
    }
}
