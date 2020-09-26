package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.api.request.RequestApiComment;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.model.entity.PostComment;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.PostCommentsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class PostCommentService {
    private final PostCommentsRepository postCommentsRepository;
    private final PostsRepository postsRepository;
    private final UserService userService;


    @Autowired
    public PostCommentService(PostCommentsRepository postCommentsRepository,
                              PostsRepository postsRepository,
                              UserService userService) {
        this.postCommentsRepository = postCommentsRepository;
        this.postsRepository = postsRepository;
        this.userService = userService;
    }


    public List<PostComment> getPostCommentsByPostId(int postId) {
        return postCommentsRepository.findByPostId(postId);
    }


    private PostComment getPostCommentById(int id) {
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


    private User getCommentUser(PostComment postComment) {
        return userService.getUserById(postComment.getUserId());
    }


    @Transactional
    private int saveComment(Integer parentId, int postId, int userId, String text) {
        PostComment postComment = new PostComment();

        if (parentId != null) postComment.setParentId(parentId);
        postComment.setPostId(postId);
        postComment.setUserId(userId);
        postComment.setTime(System.currentTimeMillis());
        postComment.setText(text);

        postCommentsRepository.saveAndFlush(postComment);

        return postComment.getId();
    }


    @Transactional
    public ResponseEntity<?> postApiComment(RequestApiComment requestBody) {
        //test auth
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        //test parenId
        Integer parentId = requestBody.getParentId();
        if (parentId != null) {
            //try to find parent comment
            if (getPostCommentById(parentId) == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        //test postId
        int postId = requestBody.getPostId();
        if (postsRepository.findById(postId).isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        //test text
        String text = requestBody.getText();
        if (text.length() < 3)
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResultFalseErrorsResponse(new ErrorsResponse("error")));

        //ошибок нет -> сохраняем -> собираем ответ
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiCommentTrueResponse(
                        saveComment(
                                parentId,
                                postId,
                                userService.findUserByLogin(authentication.getName()).getId(),
                                text
        )));
    }
}