package main.com.skillbox.ru.developerspublics.repository;

import main.com.skillbox.ru.developerspublics.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {
    User findUserByEmail(String email);

    @Query(value = "SELECT COUNT(*) FROM posts WHERE moderation_status='NEW'", nativeQuery = true)
    int getModerationCount();

    User findByCode(String code);
}
