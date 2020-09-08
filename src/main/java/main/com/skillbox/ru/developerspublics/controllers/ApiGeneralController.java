package main.com.skillbox.ru.developerspublics.controllers;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiComment;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiModeration;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiSettings;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


@RestController
public class ApiGeneralController
{
    @Autowired
    private GlobalSettingService globalSettingService;

    @Autowired
    private TagService tagService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostCommentService postCommentService;

    private final String USER = "ROLE_USER";
    private final String MODERATOR = "ROLE_MODERATOR";


    //GET /api/init/
    @GetMapping("/api/init")
    public ResponseEntity<BlogInfo> getApiInit() {
        return globalSettingService.getApiInit();
    }


    //POST /api/comment/
    @SneakyThrows
    @Secured(USER)
    @PostMapping("/api/comment")
    public ResponseEntity<?> postApiComment(@RequestBody RequestApiComment requestBody) {
        return postCommentService.postApiComment(requestBody);
    }


    //GET /api/tag/
    @SneakyThrows
    @GetMapping("/api/tag")
    public ResponseEntity<?> getApiTag(@RequestParam(name = "query", required = false) String query) {
        return tagService.getApiTag(query);
    }


    //POST /api/moderation
    @SneakyThrows
    @Secured(MODERATOR)
    @PostMapping("/api/moderation")
    public ResponseEntity<?> postApiModeration(@RequestBody RequestApiModeration requestBody) {
        return postService.postApiModeration(requestBody);
    }


    //GET /api/calendar
    @SneakyThrows
    @GetMapping("/api/calendar")
    public ResponseEntity<?> getApiCalendar(@RequestParam(name = "year", required = false) Integer year) {
        return postService.getApiCalendar(year);
    }


    //POST /api/profile/my
    @SneakyThrows
    @Secured(USER)
    @PostMapping(value = "/api/profile/my", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<?> postApiProfileMy(@RequestBody(required = false) String requestBody,
                                       @RequestPart(value = "photo", required = false) MultipartFile avatar,
                                       @RequestPart(value = "email", required = false) String emailMP,
                                       @RequestPart(value = "name", required = false) String nameMP,
                                       @RequestPart(value = "password", required = false) String passwordMP,
                                       @RequestPart(value = "removePhoto", required = false) String removePhotoMP) {
        return userService.postApiProfileMy(requestBody, avatar, emailMP, nameMP, passwordMP, removePhotoMP);
    }


    //POST /api/image
    @SneakyThrows
    @Secured(USER)
    @PostMapping(value = "/api/image", consumes = {"multipart/form-data"})
    public @ResponseBody ResponseEntity<?> postApiImage(@RequestPart("image") MultipartFile avatar) {
        return userService.postApiImage(avatar);
    }


    //GET /api/statistics/my
    @Secured(USER)
    @GetMapping("/api/statistics/my")
    public ResponseEntity<?> getApiStatisticsMy() {
        return postService.getApiStatisticsMy();
    }


    //GET /api/statistics/all
    @SneakyThrows
    @GetMapping("/api/statistics/all")
    public ResponseEntity<?> getApiStatisticsAll() {
        return postService.getApiStatisticsAll();
    }


    //GET /api/settings
    @GetMapping("/api/settings")
    public ResponseEntity<?> getApiSettings() {
        return globalSettingService.getApiSettings();
    }


    //PUT /api/settings
    @SneakyThrows
    @Secured(MODERATOR)
    @PutMapping("/api/settings")
    public ResponseEntity<?> postApiSettings(@RequestBody RequestApiSettings requestBody) {
        return globalSettingService.postApiSettings(requestBody);
    }


    @SneakyThrows
    @GetMapping("/upload/{A}/{B}/{C}/{FILENAME}")
    @ResponseBody
    public ResponseEntity<?> getAvatar(@PathVariable("A") String a,
                                @PathVariable("B") String b,
                                @PathVariable("C") String c,
                                @PathVariable("FILENAME") String name) {
        return userService.getAvatar(a, b, c, name);
    }


    @GetMapping("/login/change-password/{HASH}")
    public ModelAndView getLoginChangePassword(@PathVariable("HASH") String hash,
                                               ModelMap model) {
        model.addAttribute("HASH", hash);
        return new ModelAndView("forward:/", model);
    }
}
