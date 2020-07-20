package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.model.pojo.PostComment;
import main.com.skillbox.ru.developerspublics.model.pojo.Post;
import main.com.skillbox.ru.developerspublics.model.pojo.User;
import main.com.skillbox.ru.developerspublics.repository.PostCommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class PostCommentService {
    @Autowired
    private PostCommentsRepository postCommentsRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    public List<PostComment> getInitPostComments() {
        List<PostComment> postComments = new ArrayList<>();
        for (PostComment postCommentDB : postCommentsRepository.findAll()) {
            initPostComment(postCommentDB);
            postComments.add(postCommentDB);
        }
        return postComments;
    }

    public List<PostComment> getPostComments() {
        return new ArrayList<>(postCommentsRepository.findAll());
    }

    private void initPostComment(PostComment postComment) {
        postComment.setCommentPost(getCommentPost(postComment));
        postComment.setCommentUser(getCommentUser(postComment));
    }

    private Post getCommentPost(PostComment postComment) {
        return postService.getPostById(postComment.getPostId());
    }

    private User getCommentUser(PostComment postComment) {
        return userService.getUserById(postComment.getUserId());
    }

    public PostComment getPostCommentById(int id) {
        return postCommentsRepository.findById(id).get();
    }
}
