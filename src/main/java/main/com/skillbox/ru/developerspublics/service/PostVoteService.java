package main.com.skillbox.ru.developerspublics.service;

import java.time.Instant;
import java.util.List;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiPostLike;
import main.com.skillbox.ru.developerspublics.api.response.ResultResponse;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.PostVote;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.PostVotesRepository;
import main.com.skillbox.ru.developerspublics.model.repository.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class PostVoteService {

  private final PostsRepository postsRepository;
  private final PostVotesRepository postVotesRepository;
  private final UserService userService;

  @Autowired
  public PostVoteService(
      PostsRepository postsRepository,
      PostVotesRepository postVotesRepository,
      UserService userService) {
    this.postsRepository = postsRepository;
    this.postVotesRepository = postVotesRepository;
    this.userService = userService;
  }


  public List<PostVote> findPostVotesByPostId(int postId) {
    return postVotesRepository.findByPostId(postId);
  }

  public void delete(PostVote postVote) {
    postVotesRepository.delete(postVote);
  }


  public void deletePostVote(PostVote postVote) {
    postVotesRepository.delete(postVote);
  }


  private boolean setLikeDislike(int postId, int userId, int value) {
    Post post;
    if (postsRepository.findById(postId).isPresent()) {
      post = postsRepository.findById(postId).get();
    } else {
      return false;
    }

    //запрещаем ставить оценки на свои посты
    if (post.getUserId() == userId) {
      return false;
    }

    //ищем в БД
    PostVote postVote = postVotesRepository.findByPostIdAndUserId(postId, userId);
    //если не нашли -> первая оценка -> создаем и запоминаем
    if (postVote == null) {
      postVote = new PostVote();
      postVote.setPostId(postId);
      postVote.setUserId(userId);
    }   //если нашли - одинаковые оценки - не делаем, противоположные меняем местами
    else if (postVote.getValue() == value) {
      return false;
    }

    postVote.setTime(Instant.now().toEpochMilli());
    postVote.setValue(value);
    postVotesRepository.save(postVote);
    return true;
  }


  public ResponseEntity<?> postApiPostLike(RequestApiPostLike requestBody) {
    return postApiPostLikeDislike(requestBody, 1);
  }


  public ResponseEntity<?> postApiPostDislike(RequestApiPostLike requestBody) {
    return postApiPostLikeDislike(requestBody, -1);
  }


  private ResponseEntity<?> postApiPostLikeDislike(RequestApiPostLike requestBody, int value) {
    //проверяем аутентификацию
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return ResponseEntity.status(401).body(null);
    }
    //из контекста достаем пользователя
    User user = userService.findUserByLogin(authentication.getName());
    // === @Secured(USER) ===
    if (user == null) {
      return ResponseEntity.status(401).body(null);
    }

    //пробуем поставить оценку - результат помещаем в ответ
    return ResponseEntity.status(HttpStatus.OK)
        .body(new ResultResponse(setLikeDislike(requestBody.getPostId(), user.getId(), value)));
  }
}