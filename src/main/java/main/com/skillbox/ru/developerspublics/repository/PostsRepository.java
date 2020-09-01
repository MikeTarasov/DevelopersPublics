package main.com.skillbox.ru.developerspublics.repository;

import main.com.skillbox.ru.developerspublics.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostsRepository extends JpaRepository<Post, Integer> {
    Post findByTitle(String title);

    List<Post> findByIsActiveAndModerationStatus(int isActive, String moderationStatus);

    List<Post> findByUserId(int userId);
}
