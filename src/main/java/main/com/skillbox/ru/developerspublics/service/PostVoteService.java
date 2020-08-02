package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.model.entity.PostVote;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.repository.PostVotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Service
public class PostVoteService {
    @Autowired
    private PostVotesRepository postVotesRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    public List<PostVote> getInitPostVotes() {
        List<PostVote> postVotes = new ArrayList<>();
        for (PostVote postVoteDB : postVotesRepository.findAll()) {
            initPostVote(postVoteDB);
            postVotes.add(postVoteDB);
        }
        return postVotes;
    }

    public List<PostVote> getPostVotes() {
        return new ArrayList<>(postVotesRepository.findAll());
    }

    public PostVote getPostVoteById(int id) {
        return postVotesRepository.findById(id).orElseThrow();
    }

    private PostVote getPostVoteByPostUser(int postId, int userId) {
        for (PostVote postVote : getPostVotes()) {
            if (postVote.getPostId() == postId && postVote.getUserId() == userId) {
                return postVote;
            }
        }
        return null;
    }

    private void initPostVote(PostVote postVote) {
        postVote.setUserVote(getUserVote(postVote));
        postVote.setPostVote(getPostVote(postVote));
    }

    private User getUserVote(PostVote postVote) {
        return userService.getUserById(postVote.getUserId());
    }

    private Post getPostVote(PostVote postVote) {
        return postService.getPostById(postVote.getPostId());
    }

    public boolean setLikeDislike(int postId, int userId, int value) {
        //ищем в БД
        PostVote postVote = getPostVoteByPostUser(postId, userId);
        //если не нашли -> первая оценка -> создаем и запоминаем
        if (postVote == null) {
            postVote = new PostVote();
            postVote.setPostId(postId);
            postVote.setUserId(userId);

        }   //если нашли - одинаковые оценки - не делаем, противоположные меняем местами
        else if (postVote.getValue() == value) return false;

        postVote.setTime(Instant.now().toEpochMilli());
        postVote.setValue(value);
        postVotesRepository.save(postVote);
        return true;
    }
}
