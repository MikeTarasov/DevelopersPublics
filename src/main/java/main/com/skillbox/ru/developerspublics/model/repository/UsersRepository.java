package main.com.skillbox.ru.developerspublics.model.repository;

import main.com.skillbox.ru.developerspublics.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {

    User findUserByEmail(String email);

    User findUserByCode(String code);
}
