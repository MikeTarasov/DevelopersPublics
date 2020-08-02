package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.model.entity.PostComment;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.repository.PostCommentsRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
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
        if (postCommentsRepository.findById(id).isPresent()) return postCommentsRepository.findById(id).get();
        return null;
    }

    public JSONObject postCommentToJSON(PostComment postComment) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", postComment.getId());
        jsonObject.put("timestamp", postComment.getTimestamp());
        jsonObject.put("text", postComment.getText());

        JSONObject user = new JSONObject();
        User commentUser = getCommentUser(postComment);
        user.put("id", commentUser.getId());
        user.put("name", commentUser.getName());
        user.put("photo", commentUser.getPhoto());

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

    public int saveComment(Integer parentId, int postId, int userId, String text) {
        PostComment postComment = new PostComment();

        if (parentId != null) postComment.setParentId(parentId);
        postComment.setPostId(postId);
        postComment.setUserId(userId);
        postComment.setTime(System.currentTimeMillis());
        postComment.setText(text);

        postCommentsRepository.saveAndFlush(postComment);

        return postComment.getId();
    }
}
