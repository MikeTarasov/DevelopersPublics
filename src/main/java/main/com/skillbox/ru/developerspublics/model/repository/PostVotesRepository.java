package main.com.skillbox.ru.developerspublics.model.repository;

import java.util.List;
import main.com.skillbox.ru.developerspublics.model.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVotesRepository extends JpaRepository<PostVote, Integer> {

    PostVote findByPostIdAndUserId(int postId, int userId);

    List<PostVote> findByPostId(int postId);
}
