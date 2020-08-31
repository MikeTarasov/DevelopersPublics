package main.com.skillbox.ru.developerspublics.repository;

import main.com.skillbox.ru.developerspublics.model.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostVotesRepository extends JpaRepository<PostVote, Integer> {
    PostVote findByPostIdAndUserId(int postId, int userId);
    List<PostVote> findByPostId(int postId);
}
