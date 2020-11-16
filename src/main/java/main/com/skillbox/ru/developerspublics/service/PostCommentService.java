package main.com.skillbox.ru.developerspublics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiComment;
import main.com.skillbox.ru.developerspublics.api.response.ApiCommentTrueResponse;
import main.com.skillbox.ru.developerspublics.api.response.ErrorsResponse;
import main.com.skillbox.ru.developerspublics.api.response.PostCommentResponse;
import main.com.skillbox.ru.developerspublics.api.response.ResultFalseErrorsResponse;
import main.com.skillbox.ru.developerspublics.api.response.UserIdNamePhotoResponse;
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


@Service
public class PostCommentService {

  private final PostCommentsRepository postCommentsRepository;
  private final PostsRepository postsRepository;
  private final UserService userService;

  @Autowired
  public PostCommentService(
      PostCommentsRepository postCommentsRepository,
      PostsRepository postsRepository,
      UserService userService) {
    this.postCommentsRepository = postCommentsRepository;
    this.postsRepository = postsRepository;
    this.userService = userService;
  }


  public List<PostComment> getPostCommentsByPostId(int postId) {
    return postCommentsRepository.findByPostId(postId);
  }


  private Optional<PostComment> getPostCommentById(int id) {
    return postCommentsRepository.findById(id);
  }


  public List<PostCommentResponse> getPostCommentResponseList(List<PostComment> postComments) {
    List<PostCommentResponse> list = new ArrayList<>();
    for (PostComment postComment : postComments) {
      User commentUser = findCommentUser(postComment);
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


  private User findCommentUser(PostComment postComment) {
    return userService.findUserById(postComment.getUserId());
  }


  public void deletePostComment(PostComment postComment) {
    postCommentsRepository.delete(postComment);
  }


  @Transactional
  private int saveComment(Integer parentId, int postId, int userId, String text) {
    PostComment postComment = new PostComment();

    if (parentId != null) {
      postComment.setParentId(parentId);
    }
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
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    //test parenId
    Integer parentId = requestBody.getParentId();
    if (parentId != null) {
      //try to find parent comment
      if (getPostCommentById(parentId).isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
      }
    }

    //test postId
    int postId = requestBody.getPostId();
    if (postsRepository.findById(postId).isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    //test text
    String text = requestBody.getText();
    if (text.length() < 3) {
      return ResponseEntity.status(HttpStatus.OK)
          .body(new ResultFalseErrorsResponse(new ErrorsResponse("error")));
    }

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