package com.skillbox.ru.developerspublics.test;

import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiModeration;
import main.com.skillbox.ru.developerspublics.api.request.RequestPostPutApiPost;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.model.entity.GlobalSetting;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.repository.GlobalSettingsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.PostsRepository;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsPostService {
    private final GlobalSettingsRepository globalSettingsRepository;
    private final PostsRepository postsRepository;
    private final PostService postService;
    private final TagService tagService;
    private final UserService userService;
    private final String sipValue;
    private final GlobalSetting sip;
    private final String password = "testPassword";
    private final User user = new User("test@test.test", "test", password);
    private Post post;
    private final String tagName = "testTag";
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private String date;
    private final String title = "testTitle testTitle testTitle";
    private final String text = "testText testText testText testText testText testText testText testText testText testText";

    @Autowired
    public UnitTestsPostService(GlobalSettingsRepository globalSettingsRepository,
                                PostsRepository postsRepository,
                                PostService postService,
                                TagService tagService,
                                UserService userService) {
        this.globalSettingsRepository = globalSettingsRepository;
        this.postsRepository = postsRepository;
        this.postService = postService;
        this.tagService = tagService;
        this.userService = userService;
        sip = globalSettingsRepository
                .findGlobalSettingByCode(GlobalSettingsCodes.STATISTICS_IS_PUBLIC.toString());
        sipValue = sip.getValue();
    }

    private void setNoValue() {
        sip.setValue(GlobalSettingsValues.NO.toString());
        globalSettingsRepository.save(sip);
    }

    private void setYesValue() {
        sip.setValue(GlobalSettingsValues.YES.toString());
        globalSettingsRepository.save(sip);
    }

    private void restoreValue() {
        sip.setValue(sipValue);
        globalSettingsRepository.save(sip);
    }

    private void saveUser() {
        if (userService.findUserByLogin(user.getEmail()) != null) userService.deleteUser(user);
        userService.saveUser(user);
    }

    private void authUser() {
        saveUser();
        userService.authUser(user.getEmail(), password);
    }

    private void authModerator() {
        user.setIsModerator(1);
        authUser();
    }

    private void deleteUser() {
        userService.getApiAuthLogout();
        userService.deleteUser(user);
    }

    private void initPost() {
        post = postsRepository.findByTitle(title);
        if (post != null) postsRepository.delete(post);
        post = new Post();
        post.setIsActive(1);
        post.setModerationStatus(ModerationStatuses.ACCEPTED.toString());
        post.setUserId(user.getId());
        post.setTime(System.currentTimeMillis());
        post.setTitle(title);
        post.setText(text);
        post.setViewCount(0);
        postsRepository.save(post);
    }

    private void deletePost() {
        postService.deletePost(post);
    }

    private void saveTag() { tagService.saveTag(tagName, post.getId()); }


    @Test
    @Transactional
    public void testPostApiPostOK() {
        authUser();

        RequestPostPutApiPost requestBody = new RequestPostPutApiPost(
                System.currentTimeMillis(),
                1,
                title,
                Collections.singletonList("TESTTAG"),
                text
        );

        ResponseEntity<?> response = postService.postApiPost(requestBody);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());
        post = postsRepository.findByTitle(title);
        Assert.assertNotNull(post);

        deletePost();
        deleteUser();
    }


    @Test
    @Transactional
    public void testPutApiPostUnAuth() {
        saveUser();
        initPost();

        RequestPostPutApiPost requestBody = new RequestPostPutApiPost(
                System.currentTimeMillis(),
                1,
                title,
                Collections.singletonList("TESTTAG"),
                text
        );

        ResponseEntity<?> response = postService.putApiPost(requestBody, post.getId());
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());

        deletePost();
        userService.deleteUser(user);
    }

    @Test
    @Transactional
    public void testPutApiPostBadTitle() {
        authUser();
        initPost();

        RequestPostPutApiPost requestBody = new RequestPostPutApiPost(
                System.currentTimeMillis(),
                1,
                "12",
                Collections.singletonList("TESTTAG"),
                text
        );

        ResponseEntity<?> response = postService.putApiPost(requestBody, post.getId());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ResultFalseErrorsResponse rfer = (ResultFalseErrorsResponse) response.getBody();
        Assert.assertEquals(new ResultFalseErrorsResponse(
                ErrorsResponse.builder().title("Заголовок не установлен").build()),
                rfer);

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testPutApiPostShortText() {
        authUser();
        initPost();

        RequestPostPutApiPost requestBody = new RequestPostPutApiPost(
                System.currentTimeMillis(),
                1,
                title,
                Collections.singletonList("TESTTAG"),
                "text"
        );

        ResponseEntity<?> response = postService.putApiPost(requestBody, post.getId());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ResultFalseErrorsResponse rfer = (ResultFalseErrorsResponse) response.getBody();
        Assert.assertEquals(new ResultFalseErrorsResponse(
                ErrorsResponse.builder().text("Текст публикации слишком короткий").build()),
                rfer);

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testPutApiPostLongText() {
        authUser();
        initPost();

        RequestPostPutApiPost requestBody = new RequestPostPutApiPost(
                System.currentTimeMillis(),
                1,
                title,
                Collections.singletonList("TESTTAG"),
                "0123456789".repeat(1677722)
        );

        ResponseEntity<?> response = postService.putApiPost(requestBody, post.getId());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ResultFalseErrorsResponse rfer = (ResultFalseErrorsResponse) response.getBody();
        Assert.assertEquals(new ResultFalseErrorsResponse(
                ErrorsResponse.builder().text("Текст публикации слишком длинный").build()),
                rfer);

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testPutApiPostBadPostId() {
        authUser();
        initPost();

        RequestPostPutApiPost requestBody = new RequestPostPutApiPost(
                System.currentTimeMillis(),
                1,
                title,
                Collections.singletonList("TESTTAG"),
                text
        );

        ResponseEntity<?> response = postService.putApiPost(requestBody, 0);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());;
        Assert.assertEquals(new ResultFalseErrorsResponse(
                ErrorsResponse.builder().text("Не удалось сохранить пост").build()),
                response.getBody());

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testPutApiPostOK() {
        authUser();
        initPost();

        RequestPostPutApiPost requestBody = new RequestPostPutApiPost(
                System.currentTimeMillis(),
                1,
                title,
                Collections.singletonList("TESTTAG"),
                text + "1"
        );

        ResponseEntity<?> response = postService.putApiPost(requestBody, post.getId());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultResponse(true), response.getBody());
        Assert.assertNotEquals(text, postsRepository.findByTitle(title).getText());

        deletePost();
        deleteUser();
    }


    @Test
    @Transactional
    public void testGetApiPostMode1() {
        saveUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPost(0, 10, "");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        userService.deleteUser(user);
    }

    @Test
    @Transactional
    public void testGetApiPostMode2() {
        saveUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPost(0, 10, "popular");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        userService.deleteUser(user);
    }

    @Test
    @Transactional
    public void testGetApiPostMode3() {
        saveUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPost(0, 10, "best");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        userService.deleteUser(user);
    }

    @Test
    @Transactional
    public void testGetApiPostMode4() {
        saveUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPost(0, 10, "early");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        userService.deleteUser(user);
    }


    @Test
    @Transactional
    public void testGetApiPostSearchNullQuery() {
        ResponseEntity<?> response = postService.getApiPostSearch(0, 10, null);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertNull(response.getBody());
    }

    @Test
    @Transactional
    public void testGetApiPostSearchEmptyQuery() {
        saveUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPostSearch(0, 10, "");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        userService.deleteUser(user);
    }

    @Test
    @Transactional
    public void testGetApiPostSearchTitleQuery() {
        SecurityContextHolder.clearContext();
        saveUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPostSearch(0, 10, "Text");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        userService.deleteUser(user);
    }

    @Test
    @Transactional
    @SneakyThrows
    public void testGetApiPostSearchTextQuery() {
        saveUser();
        initPost();
        Thread.sleep(500);

        ResponseEntity<?> response = postService.getApiPostSearch(0, 10, "Title");
        Thread.sleep(500);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        userService.deleteUser(user);
    }


    @Test
    @Transactional
    public void testGetApiPostIdPostNotFound() {
        ResponseEntity<?> response = postService.getApiPostId(0);
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assert.assertNull(response.getBody());
    }

    @Test
    @Transactional
    public void testGetApiPostIdPostUnAuth() {
        SecurityContextHolder.clearContext();
        saveUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPostId(post.getId());
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());

        deletePost();
        userService.deleteUser(user);
    }

    @Test
    @Transactional
    public void testGetApiPostIdPostAuthor() {
        authUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPostId(post.getId());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        PostByIdResponse pbir = (PostByIdResponse) response.getBody();
        Assert.assertNotNull(pbir);
        Assert.assertEquals(postService.postByIdToJSON(post), pbir);

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiPostIdPostModerator() {
        authModerator();
        initPost();
        post.setUserId(0);
        postsRepository.save(post);

        ResponseEntity<?> response = postService.getApiPostId(post.getId());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        PostByIdResponse pbir = (PostByIdResponse) response.getBody();
        Assert.assertNotNull(pbir);
        Assert.assertEquals(postService.postByIdToJSON(post), pbir);

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiPostIdPostAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("anonymous",
                "anonymous", Collections.singleton(new SimpleGrantedAuthority("anonymous"))));
        saveUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPostId(post.getId());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        PostByIdResponse pbir = (PostByIdResponse) response.getBody();
        Assert.assertNotNull(pbir);
        Assert.assertEquals(postService.postByIdToJSON(post), pbir);
        Assert.assertNotEquals(0, post.getViewCount());

        deletePost();
        userService.deleteUser(user);
    }

    @Test
    @Transactional
    public void testGetApiPostIdPostAnonymousNonActivePost() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("anonymous",
                "anonymous", Collections.singleton(new SimpleGrantedAuthority("anonymous"))));
        saveUser();
        initPost();
        post.setIsActive(0);
        postsRepository.save(post);

        ResponseEntity<?> response = postService.getApiPostId(post.getId());
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assert.assertNull(response.getBody());

        deletePost();
        userService.deleteUser(user);
    }

    @Test
    @Transactional
    public void testGetApiPostByDateToday() {
        authUser();
        initPost();

        date = dateFormat.format(post.getTime());
        ResponseEntity<?> response = postService.getApiPostByDate(0, 10, date);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiPostByDateZeroDate() {
        authUser();
        initPost();

        date = dateFormat.format(new Date(0));
        ResponseEntity<?> response = postService.getApiPostByDate(0, 10, date);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertEquals(0, apr.getCount());

        deletePost();
        deleteUser();
    }


    @Test
    @Transactional
    public void testGetApiPostByTagNoTag() {
        ResponseEntity<?> response = postService.getApiPostByTag(0, 10, "");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertEquals(0, apr.getCount());
    }

    @Test
    @Transactional
    public void testGetApiPostByTag200() {
        authUser();
        initPost();
        saveTag();

        ResponseEntity<?> response = postService.getApiPostByTag(0, 10, tagName);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiPostModerationNullAuth() {
        SecurityContextHolder.clearContext();

        ResponseEntity<?> response = postService.getApiPostModeration(0, 10, "");
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());
    }

    @Test
    @Transactional
    public void testGetApiPostModerationNotModerator() {
        authUser();

        ResponseEntity<?> response = postService.getApiPostModeration(0, 10, "");
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());

        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiPostModeration200New() {
        authModerator();
        initPost();
        String status = ModerationStatuses.NEW.getStatus();
        post.setModerationStatus(status);
        postsRepository.save(post);

        ResponseEntity<?> response = postService.getApiPostModeration(0, 10, status);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiPostModeration200Declined() {
        authModerator();
        initPost();
        post.setModeratorId(user.getId());
        String status = ModerationStatuses.DECLINED.getStatus();
        post.setModerationStatus(status);
        postsRepository.save(post);

        ResponseEntity<?> response = postService.getApiPostModeration(0, 10, status);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deletePost();
        deleteUser();
    }


    @Test
    @Transactional
    public void testGetApiPostMy401() {
        SecurityContextHolder.clearContext();

        ResponseEntity<?> response = postService.getApiPostMy(0, 10, "");
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());
    }

    @Test
    @Transactional
    public void testGetApiPostMyBadStatus() {
        authUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPostMy(0, 10, "");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertEquals(0, apr.getCount());

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiPostMy200() {
        authUser();
        initPost();

        ResponseEntity<?> response = postService.getApiPostMy(0, 10, "published");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertEquals(1, apr.getCount());

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testPostApiModerationUnAuthorised() {
        SecurityContextHolder.clearContext();
        RequestApiModeration request = new RequestApiModeration();
        ResponseEntity<?> response = postService.postApiModeration(request);
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());
    }

    @Test
    @Transactional
    public void testPostApiModerationBadRequest() {
        authUser();

        RequestApiModeration request = new RequestApiModeration();
        ResponseEntity<?> response = postService.postApiModeration(request);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertNull(response.getBody());

        deleteUser();
    }

    @Test
    @Transactional
    public void testPostApiModerationBadStatus() {
        authModerator();

        RequestApiModeration request = new RequestApiModeration();
        request.setDecision("");
        ResponseEntity<?> response = postService.postApiModeration(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ResultResponse result = (ResultResponse) response.getBody();
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isResult());

        deleteUser();
    }

    @Test
    @Transactional
    public void testPostApiModerationNotModerator() {
        authUser();
        initPost();

        RequestApiModeration request = new RequestApiModeration();
        request.setDecision("accept");
        ResponseEntity<?> response = postService.postApiModeration(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ResultResponse result = (ResultResponse) response.getBody();
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isResult());

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testPostApiModerationNullPostId() {
        authModerator();

        RequestApiModeration request = new RequestApiModeration();
        request.setDecision("accept");
        ResponseEntity<?> response = postService.postApiModeration(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ResultResponse result = (ResultResponse) response.getBody();
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isResult());

        deleteUser();
    }

    @Test
    @Transactional
    public void testPostApiModeration200() {
        authModerator();
        initPost();

        RequestApiModeration request = new RequestApiModeration();
        request.setPostId(post.getId());
        request.setDecision("accept");
        ResponseEntity<?> response = postService.postApiModeration(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ResultResponse result = (ResultResponse) response.getBody();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isResult());

        deletePost();
        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiCalendar() {
        ResponseEntity<?> response = postService.getApiCalendar(0);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());

        ApiCalendarResponse acr = (ApiCalendarResponse) response.getBody();
        Assert.assertNotNull(acr.getPosts());
        Assert.assertNotNull(acr.getYears());
    }

    @Test
    @Transactional
    public void testGetApiStatisticsMy401() {
        ResponseEntity<?> response = postService.getApiStatisticsMy();
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());
    }

    @Test
    @Transactional
    public void testGetApiStatisticsMy200() {
        authUser();

        ResponseEntity<?> response = postService.getApiStatisticsMy();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());

        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiStatisticsAll401() {
        setNoValue();

        ResponseEntity<?> response = postService.getApiStatisticsAll();
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());

        restoreValue();
    }

    @Test
    @Transactional
    public void testGetApiStatisticsAll200Anonymous() {
        setYesValue();

        ResponseEntity<?> response = postService.getApiStatisticsAll();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());

        restoreValue();
    }

    @Test
    @Transactional
    public void testGetApiStatisticsAll200User() {
        setNoValue();
        authUser();

        ResponseEntity<?> response = postService.getApiStatisticsAll();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());

        restoreValue();
        deleteUser();
    }
}