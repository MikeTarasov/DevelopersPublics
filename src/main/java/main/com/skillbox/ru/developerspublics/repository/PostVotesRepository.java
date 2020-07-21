package main.com.skillbox.ru.developerspublics.repository;

import main.com.skillbox.ru.developerspublics.model.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVotesRepository extends JpaRepository<PostVote, Integer> {
}
