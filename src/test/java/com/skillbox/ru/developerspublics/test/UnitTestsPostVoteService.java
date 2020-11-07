package com.skillbox.ru.developerspublics.test;


import java.util.ArrayList;
import java.util.Collections;
import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiPostLike;
import main.com.skillbox.ru.developerspublics.api.response.ResultResponse;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.service.PostService;
import main.com.skillbox.ru.developerspublics.service.PostVoteService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsPostVoteService {

    private final PostService postService;
    private final PostVoteService postVoteService;
    private final UserService userService;
    private final String password = "test";
    private final User user = new User("test@test.test", "test", password);
    private Post post;


    @Autowired
    public UnitTestsPostVoteService(PostService postService,
                                    PostVoteService postVoteService,
                                    UserService userService) {
        this.postService = postService;
        this.postVoteService = postVoteService;
        this.userService = userService;
    }

    @Transactional
    private void saveUser() {
        if (userService.findUserByLogin(user.getEmail()) != null) userService.deleteUser(user);
        userService.saveUser(user);
    }

    @Transactional
    private void savePost(int userId) {
        String title = "1234567890";
        postService.savePost(
                System.currentTimeMillis(),
                1,
                title,
                "123456789012345678901234567890123456789012345678901234567890",
                userId,
                new ArrayList<>(Collections.singleton("TESTTAG")));
        post = postService.findPostByTitle(title);
    }

    private void authUser() {
        saveUser();
        userService.authUser(user.getEmail(), password);
    }

    private void clearAll() {
        userService.deleteUser(user);
        postService.deletePost(post);
    }


    @Test
    @Transactional
    public void testPostApiPostLikeNoName() {
        RequestApiPostLike request = new RequestApiPostLike();

        ResponseEntity<?> response = postVoteService.postApiPostLike(request);
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());
    }

    @Test
    @Transactional
    public void testPostApiPostLikeAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "anonymous",
                "anonymous",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        ));

        RequestApiPostLike request = new RequestApiPostLike();

        ResponseEntity<?> response = postVoteService.postApiPostLike(request);
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());

        SecurityContextHolder.clearContext();
    }

    @Test
    @Transactional
    public void testPostApiPostLikeMyPost() {
        authUser();
        savePost(user.getId());

        RequestApiPostLike request = new RequestApiPostLike(post.getId());

        ResponseEntity<?> response = postVoteService.postApiPostLike(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(false), response.getBody());

        clearAll();
    }

    @Test
    @Transactional
    public void testPostApiPostLikeNotMyPost() {
        authUser();
        savePost(6);

        RequestApiPostLike request = new RequestApiPostLike(post.getId());

        ResponseEntity<?> response = postVoteService.postApiPostLike(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());

        clearAll();
    }

    @Test
    @Transactional
    public void testPostApiPostLikeNotMyPostLikeLike() {
        authUser();
        savePost(6);

        RequestApiPostLike request = new RequestApiPostLike(post.getId());

        ResponseEntity<?> response = postVoteService.postApiPostLike(request);
        response = postVoteService.postApiPostLike(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(false), response.getBody());

        clearAll();
    }

    @Test
    @Transactional
    public void testPostApiPostLikeNotMyPostLikeDislike() {
        authUser();
        savePost(6);

        RequestApiPostLike request = new RequestApiPostLike(post.getId());

        ResponseEntity<?> response = postVoteService.postApiPostLike(request);
        response = postVoteService.postApiPostDislike(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());

        clearAll();
    }
}