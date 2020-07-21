package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.model.entity.PostComment;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.repository.PostCommentsRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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

    public PostComment getInitPostCommentById(int id) {
        Optional<PostComment> optionalPostComment = postCommentsRepository.findById(id);
        if (optionalPostComment.isPresent()) {
            PostComment postComment = optionalPostComment.get();
            initPostComment(postComment);
            return postComment;
        }
        return null;
    }

    public PostComment getPostCommentById(int id) {
        return postCommentsRepository.findById(id).get();
    }

    public JSONObject postCommentToJSON(PostComment postComment) {
        JSONObject jsonObject = new JSONObject();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        jsonObject.put("id", postComment.getId());
        jsonObject.put("time", dateFormat.format(postComment.getTime())); //TODO  "Вчера, 17:32"
        jsonObject.put("text", postComment.getText());

        JSONObject user = new JSONObject();
        user.put("id", postComment.getCommentUser().getId());
        user.put("name", postComment.getCommentUser().getName());
        user.put("photo", postComment.getCommentUser().getPhoto());

        jsonObject.put("user", user);
        return jsonObject;
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


}
