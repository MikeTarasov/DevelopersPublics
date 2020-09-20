package com.skillbox.ru.developerspublics.test;

import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.response.TagResponse;
import main.com.skillbox.ru.developerspublics.api.response.TagsListResponse;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.Tag;
import main.com.skillbox.ru.developerspublics.model.entity.User;
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

import java.util.ArrayList;
import java.util.Collections;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsTagService {
    private final TagService tagService;
    private final UserService userService;
    private final PostService postService;
    private Tag testTag;
    private User user;
    private Post post;


    private final String tagName = "TESTTAG";
    private final String title = "1234567890";

    @Autowired
    public UnitTestsTagService(PostService postService,
                               TagService tagService,
                               UserService userService) {
        this.tagService = tagService;
        this.userService = userService;
        this.postService = postService;
    }

    private void init() {
        user = new User();
        user.setEmail("test@test.test");
        user.setName("testUser");
        user.setIsModerator(0);
        user.setPassword("testPassword");
        user.setRegTime(System.currentTimeMillis());
        userService.saveUser(user);
        user = userService.findUserByLogin(user.getEmail());
        postService.savePost(
                System.currentTimeMillis(),
                1,
                title,
                "123456789012345678901234567890123456789012345678901234567890",
                user.getId(),
                new ArrayList<>(Collections.singleton(tagName)));
        post = postService.getPostByTitle(title);
        testTag = tagService.getTagByName(tagName);
    }

    private void cleanDB() {
        userService.deleteUser(user);
        postService.deletePost(post);
    }

    @Test
    public void testGetApiTagAllTags() {
        init();
        ResponseEntity<?> response = tagService.getApiTag("");

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(response.hasBody());
        cleanDB();
    }

    @Test
    public void testGetApiTagTestTag() {
        init();
        ResponseEntity<?> response = tagService.getApiTag(tagName);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(response.hasBody());
        cleanDB();
    }
}
