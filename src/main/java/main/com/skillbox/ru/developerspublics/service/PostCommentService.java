package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.api.request.RequestApiComment;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.model.entity.PostComment;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.PostCommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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


    public List<PostComment> getPostCommentsByPostId(int postId) {
        return postCommentsRepository.findByPostId(postId);
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


    public List<PostCommentResponse> getPostCommentResponseList(List<PostComment> postComments) {
        List<PostCommentResponse> list = new ArrayList<>();
        for (PostComment postComment : postComments) {
            User commentUser = getCommentUser(postComment);
            list.add(new PostCommentResponse(
                    postComment.getId(),
                    postComment.getTimestamp(),
                    postComment.getText(),
                    new UserIdNamePhotoResponse(
                            commentUser.getId(),
                            commentUser.getName(),
                            commentUser.getPhoto()
                    )
            ));
        }
        return list;
    }


    public PostCommentResponse postCommentToJSON(PostComment postComment) {
        User commentUser = getCommentUser(postComment);
        return new PostCommentResponse(
                postComment.getId(),
                postComment.getTimestamp(),
                postComment.getText(),
                new UserIdNamePhotoResponse(
                        commentUser.getId(),
                        commentUser.getName(),
                        commentUser.getPhoto()
                )
        );
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


    public ResponseEntity<?> postApiComment(RequestApiComment requestBody) {
        //получаем запрос
        Integer parentId = requestBody.getParentId();
        int postId = requestBody.getPostId();
        String text = requestBody.getText();

        //выдергиваем из контекста пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        //test parenId
        if (parentId != null) {
            //try to find parent comment
            if (getPostCommentById(parentId) == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        //test postId
        if (postService.getPostById(postId) == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        //test text
        if (text.length() < 3)
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResultFalseErrorsResponse(new ErrorsResponse("text")));

        //ошибок нет - сохраняем
        int commentId = saveComment(parentId, postId, user.getId(), text);

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiCommentTrueResponse(commentId));
    }
}