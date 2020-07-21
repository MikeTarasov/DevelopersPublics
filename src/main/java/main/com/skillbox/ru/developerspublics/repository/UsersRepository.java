package main.com.skillbox.ru.developerspublics.repository;

import main.com.skillbox.ru.developerspublics.model.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends CrudRepository<User, Integer> {
}
