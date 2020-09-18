package main.com.skillbox.ru.developerspublics.model.repository;

import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentsRepository extends JpaRepository<PostComment, Integer> {
    List<PostComment> findByPostId(int postId);

    @Query(value = "SELECT * FROM posts WHERE id= :postId", nativeQuery = true)
    Post getPostByPostId(@Param("postId") int postId);
}
