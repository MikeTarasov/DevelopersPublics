package main.java.main.com.skillbox.ru.developerspublics.rest;

//import lombok.*;
//import main.model.*;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

//@AllArgsConstructor
@Controller
public class DefaultController
{
//    @Autowired
//    private CaptchaCodesRepository captchaCodesRepository;
//    @Autowired
//    private GlobalSettingsRepository globalSettingsRepository;
//    @Autowired
//    private PostCommentsRepository postCommentsRepository;
//    @Autowired
//    private PostsRepository postsRepository;
//    @Autowired
//    private PostVotesRepository postVotesRepository;
//    @Autowired
//    private TagsRepository tagsRepository;
//    @Autowired
//    private TagToPostsRepository tagToPostsRepository;
//    @Autowired
//    private UsersRepository usersRepository;


    @RequestMapping("/")
    public String index() {
        return "index";
    }
}
