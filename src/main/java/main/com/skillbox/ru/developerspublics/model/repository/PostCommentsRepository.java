package main.com.skillbox.ru.developerspublics.model.repository;


import java.util.List;
import main.com.skillbox.ru.developerspublics.model.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentsRepository extends JpaRepository<PostComment, Integer> {

    List<PostComment> findByPostId(int postId);

    PostComment findByTextContaining(String path);
}
