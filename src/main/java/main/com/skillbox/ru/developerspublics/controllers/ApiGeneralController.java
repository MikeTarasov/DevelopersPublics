package main.com.skillbox.ru.developerspublics.controllers;


import main.com.skillbox.ru.developerspublics.api.request.RequestApiComment;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiModeration;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiSettings;
import main.com.skillbox.ru.developerspublics.service.GlobalSettingService;
import main.com.skillbox.ru.developerspublics.service.PostCommentService;
import main.com.skillbox.ru.developerspublics.service.PostService;
import main.com.skillbox.ru.developerspublics.service.TagService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


@RestController
public class ApiGeneralController {

  private final GlobalSettingService globalSettingService;
  private final TagService tagService;
  private final PostService postService;
  private final UserService userService;
  private final PostCommentService postCommentService;

  private final String USER = "ROLE_USER";
  private final String MODERATOR = "ROLE_MODERATOR";

  @Autowired
  public ApiGeneralController(
      GlobalSettingService globalSettingService,
      TagService tagService, PostService postService,
      UserService userService,
      PostCommentService postCommentService) {
    this.globalSettingService = globalSettingService;
    this.tagService = tagService;
    this.postService = postService;
    this.userService = userService;
    this.postCommentService = postCommentService;
  }


  @GetMapping("/api/init")
  public ResponseEntity<?> getApiInit() {
    return globalSettingService.getApiInit();
  }


  @Secured(USER)
  @PostMapping("/api/comment")
  public ResponseEntity<?> postApiComment(@RequestBody RequestApiComment requestBody) {
    return postCommentService.postApiComment(requestBody);
  }


  @GetMapping("/api/tag")
  public ResponseEntity<?> getApiTag(@RequestParam(name = "query", required = false) String query) {
    return tagService.getApiTag(query);
  }


  @Secured(MODERATOR)
  @PostMapping("/api/moderation")
  public ResponseEntity<?> postApiModeration(@RequestBody RequestApiModeration requestBody) {
    return postService.postApiModeration(requestBody);
  }


  @GetMapping("/api/calendar")
  public ResponseEntity<?> getApiCalendar(
      @RequestParam(name = "year", required = false) Integer year) {
    return postService.getApiCalendar(year);
  }


  @Secured(USER)
  @PostMapping(value = "/api/profile/my", consumes = {"multipart/form-data", "application/json"})
  public ResponseEntity<?> postApiProfileMy(@RequestBody(required = false) String requestBody,
      @RequestPart(value = "photo", required = false) MultipartFile avatar,
      @RequestPart(value = "email", required = false) String emailMP,
      @RequestPart(value = "name", required = false) String nameMP,
      @RequestPart(value = "password", required = false) String passwordMP,
      @RequestPart(value = "removePhoto", required = false) String removePhotoMP) {
    return userService
        .postApiProfileMy(requestBody, avatar, emailMP, nameMP, passwordMP, removePhotoMP);
  }


  @Secured(USER)
  @PostMapping(value = "/api/image", consumes = {"multipart/form-data"})
  public @ResponseBody
  ResponseEntity<?> postApiImage(@RequestPart("image") MultipartFile image) {
    return userService.postApiImage(image);
  }


  @Secured(USER)
  @GetMapping("/api/statistics/my")
  public ResponseEntity<?> getApiStatisticsMy() {
    return postService.getApiStatisticsMy();
  }


  @GetMapping("/api/statistics/all")
  public ResponseEntity<?> getApiStatisticsAll() {
    return postService.getApiStatisticsAll();
  }


  @GetMapping("/api/settings")
  public ResponseEntity<?> getApiSettings() {
    return globalSettingService.getApiSettings();
  }


  @Secured(MODERATOR)
  @PutMapping("/api/settings")
  public ResponseEntity<?> putApiSettings(@RequestBody RequestApiSettings requestBody) {
    return globalSettingService.putApiSettings(requestBody);
  }


  @GetMapping("/{PATH}/{A}/{B}/{C}/{FILENAME}")
  @ResponseBody
  public ResponseEntity<?> getAvatar(@PathVariable("PATH") String path,
      @PathVariable("A") String a,
      @PathVariable("B") String b,
      @PathVariable("C") String c,
      @PathVariable("FILENAME") String name) {
    return userService.getAvatar(path, a, b, c, name);
  }


  @GetMapping("/login/change-password/{HASH}")
  public ModelAndView getLoginChangePassword(@PathVariable("HASH") String hash,
      ModelMap model) {
    model.addAttribute("HASH", hash);
    return new ModelAndView("forward:/", model);
  }
}