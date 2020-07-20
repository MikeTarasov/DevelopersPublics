package main.com.skillbox.ru.developerspublics.service;

import lombok.Data;
import main.com.skillbox.ru.developerspublics.model.pojo.PostVote;
import main.com.skillbox.ru.developerspublics.model.pojo.Post;
import main.com.skillbox.ru.developerspublics.model.pojo.User;
import main.com.skillbox.ru.developerspublics.repository.PostVotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
