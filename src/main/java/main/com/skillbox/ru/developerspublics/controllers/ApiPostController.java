package main.com.skillbox.ru.developerspublics.controllers;


import main.com.skillbox.ru.developerspublics.api.request.RequestApiPostLike;
import main.com.skillbox.ru.developerspublics.api.request.RequestPostPutApiPost;
import main.com.skillbox.ru.developerspublics.service.PostService;
import main.com.skillbox.ru.developerspublics.service.PostVoteService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/post")
public class ApiPostController {

  private final UserService userService;
  private final PostService postService;
  private final PostVoteService postVoteService;

  private final String USER = "ROLE_USER";
  private final String MODERATOR = "ROLE_MODERATOR";

  @Autowired
  public ApiPostController(UserService userService,
      PostService postService,
      PostVoteService postVoteService) {
    this.userService = userService;
    this.postService = postService;
    this.postVoteService = postVoteService;
  }


  @GetMapping("")
  public ResponseEntity<?> getApiPost(@RequestParam(name = "offset") int offset,
      @RequestParam(name = "limit") int limit,
      @RequestParam(name = "mode") String mode) {
    return postService.getApiPost(offset, limit, mode);
  }


  @GetMapping("/search")
  public ResponseEntity<?> getApiPostSearch(@RequestParam(name = "offset") int offset,
      @RequestParam(name = "limit") int limit,
      @RequestParam(name = "query") String query) {
    return postService.getApiPostSearch(offset, limit, query);
  }


  @GetMapping("/{ID}")
  public ResponseEntity<?> getApiPostId(@PathVariable(name = "ID") int id) {
    return postService.getApiPostId(id);
  }


  @GetMapping("/byDate")
  public ResponseEntity<?> getApiPostByDate(@RequestParam(name = "offset") int offset,
      @RequestParam(name = "limit") int limit,
      @RequestParam(name = "date") String date) {
    return postService.getApiPostByDate(offset, limit, date);
  }


  @GetMapping("/byTag")
  public ResponseEntity<?> getApiPostByTag(@RequestParam(name = "offset") int offset,
      @RequestParam(name = "limit") int limit,
      @RequestParam(name = "tag") String tagName) {
    return postService.getApiPostByTag(offset, limit, tagName);
  }


  @Secured(MODERATOR)
  @GetMapping("/moderation")
  public ResponseEntity<?> getApiPostModeration(@RequestParam(name = "offset") int offset,
      @RequestParam(name = "limit") int limit,
      @RequestParam(name = "status") String status) {
    return postService.getApiPostModeration(offset, limit, status);
  }


  @Secured(USER)
  @GetMapping("/my")
  public ResponseEntity<?> getApiPostMy(@RequestParam(name = "offset") int offset,
      @RequestParam(name = "limit") int limit,
      @RequestParam(name = "status") String status) {
    return postService.getApiPostMy(offset, limit, status);
  }


  @Secured(USER)
  @PostMapping("")
  public ResponseEntity<?> postApiPost(@RequestBody RequestPostPutApiPost requestBody) {
    return postService.postApiPost(requestBody);
  }


  @Secured(USER)
  @PutMapping("/{ID}")
  public ResponseEntity<?> putApiPostId(@RequestBody RequestPostPutApiPost requestBody,
      @PathVariable(name = "ID") int postId) {
    if (userService
        .findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName())
        == null) {
      return ResponseEntity.status(401).body(null);
    }
    return postService.putApiPost(requestBody, postId);
  }


  @PostMapping("/like")
  public ResponseEntity<?> postApiPostLike(@RequestBody RequestApiPostLike requestBody) {
    return postVoteService.postApiPostLike(requestBody);
  }


  @PostMapping("/dislike")
  public ResponseEntity<?> postApiPostDislike(@RequestBody RequestApiPostLike requestBody) {
    return postVoteService.postApiPostDislike(requestBody);
  }
}