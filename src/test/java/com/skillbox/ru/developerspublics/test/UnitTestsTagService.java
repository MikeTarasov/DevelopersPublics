package com.skillbox.ru.developerspublics.test;


import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.response.TagResponse;
import main.com.skillbox.ru.developerspublics.api.response.TagsListResponse;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.Tag;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.repository.PostsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.UsersRepository;
import main.com.skillbox.ru.developerspublics.service.PostService;
import main.com.skillbox.ru.developerspublics.service.TagService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsTagService {

    private final PostService postService;
    private final TagService tagService;
    private final UserService userService;
    private final UsersRepository usersRepository;
    private final PostsRepository postsRepository;
    private User user;
    private Post post;
    private final String tagName = "TESTTAG";

    @Autowired
    public UnitTestsTagService(PostsRepository postsRepository,
        PostService postService,
        UserService userService,
        TagService tagService,
        UsersRepository usersRepository) {
        this.postService = postService;
        this.tagService = tagService;
        this.userService = userService;
        this.usersRepository = usersRepository;
        this.postsRepository = postsRepository;
    }


    private void init() {
        String email = "test@test.test";
        user = usersRepository.findUserByEmail(email);
        if (user != null) {
            userService.deleteUser(user);
        }
        user = new User(email, "testUser", "testPassword");
        user.setIsModerator(1);
        usersRepository.save(user);
        user = usersRepository.findUserByEmail(user.getEmail());

        String title = "1234567890";
        post = postsRepository.findByTitle(title);
        if (post != null) {
            postService.deletePost(post);
        }
        post = new Post();
        post.setIsActive(1);
        post.setModerationStatus(ModerationStatuses.ACCEPTED.toString());
        post.setUserId(user.getId());
        post.setTime(System.currentTimeMillis());
        post.setTitle(title);
        post.setText(
            "testText testText testText testText testText testText testText testText testText testText");
        post.setViewCount(0);
        postsRepository.save(post);
        post = postsRepository.findByTitle(title);

        Tag tag = tagService.findTagByName(tagName);
        if (tag != null) {
            tagService.deleteTag(tag);
        }
        tagService.saveTag(tagName, post.getId());
        tagService.setWeights();
    }

    private void cleanDB() {
        postService.deletePost(post);
        userService.deleteUser(user);
    }

    @Test
    public void testGetApiTagAllTags() {
        init();

        ResponseEntity<?> response = tagService.getApiTag("");
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        TagsListResponse tlr = (TagsListResponse) response.getBody();
        Assert.assertNotNull(tlr);
        Assert.assertNotEquals(0, tlr.getTags().size());

        cleanDB();
    }

    @Test
    public void testGetApiTagTestTag() {
        init();

        ResponseEntity<?> response = tagService.getApiTag("TESTTAG");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        TagsListResponse tlr = (TagsListResponse) response.getBody();
        Assert.assertNotNull(tlr);
        TagResponse tr = tlr.getTags().get(0);
        Assert.assertEquals(tagName, tr.getName());

        cleanDB();
    }
}