package main.com.skillbox.ru.developerspublics.model.repository;

import main.com.skillbox.ru.developerspublics.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PostsRepository extends JpaRepository<Post, Integer> {
    Post findByTitle(String title);

    List<Post> findByIsActiveAndModerationStatus(int isActive, String moderationStatus);

    List<Post> findByUserId(int userId);

    List<Post> findByModerationStatus(String moderationStatus);

    @Query(value = "SELECT COUNT(*) FROM posts WHERE is_active=1 AND moderation_status='ACCEPTED'" +
            "AND time<= :time", nativeQuery = true)
    int countActivePosts(@Param("time") Date time);

//    @Query(value = "SELECT * FROM posts WHERE id= :postId", nativeQuery = true)
//    Post getPostByPostId(@Param("postId") int postId);
}
