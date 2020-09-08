package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.api.request.RequestApiPostLike;
import main.com.skillbox.ru.developerspublics.api.response.ResultResponse;
import main.com.skillbox.ru.developerspublics.model.entity.PostVote;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.PostVotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;


@Service
public class PostVoteService {
    @Autowired
    private PostVotesRepository postVotesRepository;
    @Autowired
    private UserService userService;


    public PostVote getPostVoteById(int id) {
        return postVotesRepository.findById(id).orElseThrow();
    }


    public List<PostVote> getPostVotesByPostId(int postId) {
        return postVotesRepository.findByPostId(postId);
    }


    public boolean setLikeDislike(int postId, int userId, int value) {
        //ищем в БД
        PostVote postVote = postVotesRepository.findByPostIdAndUserId(postId, userId);
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


    public ResponseEntity<?> postApiPostLike(RequestApiPostLike requestBody) {
        return postApiPostLikeDislike(requestBody, 1);
    }


    public ResponseEntity<?> postApiPostDislike(RequestApiPostLike requestBody) {
        return postApiPostLikeDislike(requestBody, -1);
    }


    private ResponseEntity<?> postApiPostLikeDislike(RequestApiPostLike requestBody, int value) {
        //из запроса достаем ИД поста
        int postId = requestBody.getPostId();

        //из контекста достаем пользователя
        User user = userService.findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());

        // === @Secured(USER) ===
        if (user == null) return ResponseEntity.status(401).body(null);

        //пробуем поставить оценку - результат помещаем в ответ
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultResponse(setLikeDislike(postId, user.getId(), value)));
    }
}
