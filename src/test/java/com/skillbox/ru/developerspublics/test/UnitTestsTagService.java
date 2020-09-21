package com.skillbox.ru.developerspublics.test;


import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsTagService {
    private final TagService tagService;
    private final UserService userService;
    private final PostService postService;
    private User user;
    private Post post;


    private final String tagName = "TESTTAG12345678946546546";

    @Autowired
    public UnitTestsTagService(PostService postService,
                               TagService tagService,
                               UserService userService) {
        this.tagService = tagService;
        this.userService = userService;
        this.postService = postService;
    }

    @Transactional
    private void init() {
        user = new User("test@test.test", "testUser", "testPassword");
        userService.saveUser(user);
        user = userService.findUserByLogin(user.getEmail());
        String title = "1234567890";
        postService.savePost(
                System.currentTimeMillis(),
                1,
                title,
                "123456789012345678901234567890123456789012345678901234567890",
                user.getId(),
                new ArrayList<>(Collections.singleton(tagName)));
        post = postService.getPostByTitle(title);
        postService.setModerationStatus(post.getId(), ModerationStatuses.ACCEPTED.toString(), user.getId());
    }

    private void cleanDB() {
        userService.deleteUser(user);
        postService.deletePost(post);
    }

    @Test
    @Transactional
    public void testGetApiTagAllTags() {
        init();
        ResponseEntity<?> response = tagService.getApiTag("");

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNotEquals(response.getBody(), "TagsListResponse(tags=[])");

        cleanDB();
    }

    @Test
    @Transactional
    public void testGetApiTagTestTag() {
        init();
        ResponseEntity<?> response = tagService.getApiTag(tagName);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("name=" + tagName));

        cleanDB();
    }
}
