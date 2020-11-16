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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsPostService {

    private final GlobalSettingsRepository globalSettingsRepository;
    private final UsersRepository usersRepository;
    private final PostsRepository postsRepository;
    private final PostService postService;
    private final TagService tagService;
    private final UserService userService;
    private final GlobalSettingsValues sipValue;
    private final GlobalSetting sip;
    private final GlobalSettingsValues modStatValue;
    private final GlobalSetting moderationStatus;
    private final String password = "testPassword";
    private User user;
    private Post post;
    private final String tagName = "TESTTAG";
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private String date;
    private final String title = "testTitle testTitle testTitle";
    private final String text = "testText testText testText testText testText testText testText testText testText testText";


    @Autowired
    public UnitTestsPostService(GlobalSettingsRepository globalSettingsRepository,
        UsersRepository usersRepository,
        PostsRepository postsRepository,
        PostService postService,
        TagService tagService,
        UserService userService) {
        this.globalSettingsRepository = globalSettingsRepository;
        this.usersRepository = usersRepository;
        this.postsRepository = postsRepository;
        this.postService = postService;
        this.tagService = tagService;
        this.userService = userService;
        sip = globalSettingsRepository
            .findGlobalSettingByCode(GlobalSettingsCodes.STATISTICS_IS_PUBLIC);
        moderationStatus = globalSettingsRepository
            .findGlobalSettingByCode(GlobalSettingsCodes.POST_PREMODERATION);
        sipValue = sip.getValue();
        modStatValue = moderationStatus.getValue();
    }

    private void setNoValue(GlobalSetting globalSetting) {
        globalSetting.setValue(GlobalSettingsValues.NO);
        globalSettingsRepository.save(globalSetting);
    }

    private void setYesValue(GlobalSetting globalSetting) {
        globalSetting.setValue(GlobalSettingsValues.YES);
        globalSettingsRepository.save(globalSetting);
    }

    private void restoreValue(GlobalSetting globalSetting) {
        if (globalSetting.getName().equals(GlobalSettingsCodes.STATISTICS_IS_PUBLIC.name())) {
            globalSetting.setValue(sipValue);
        } else {
            globalSetting.setValue(modStatValue);
        }
        globalSettingsRepository.save(globalSetting);
    }

    private void saveUser(int isModerator) {
        user = userService.findUserByLogin("test@test.test");
        if (user != null) deleteUser();
        user = new User("test@test.test", "test", password);
        user.setIsModerator(isModerator);
        usersRepository.save(user);
        user = userService.findUserByLogin("test@test.test");
    }

    private void authUser(int isModerator) {
        saveUser(isModerator);
        userService.authUser(user.getEmail(), password);
    }

    private void deleteUser() {
        deletePost();
        SecurityContextHolder.clearContext();
        userService.deleteUser(user);
    }

    private void initPost(int userId) {
        deletePost();
        post = new Post();
        post.setIsActive(1);
        post.setModerationStatus(ModerationStatuses.ACCEPTED);
        post.setUserId(userId);
        post.setTime(System.currentTimeMillis());
        post.setTitle(title);
        post.setText(
            "testText testText testText testText testText testText testText testText testText testText");
        post.setViewCount(0);
        postsRepository.save(post);
        post = postsRepository.findByTitle(title);
    }

    private void deletePost() {
        post = postService.findPostByTitle(title);
        if (post != null) postService.deletePost(post);
    }

    private void saveTag() { tagService.saveTag(tagName, post.getId()); }

    @Test
    @Transactional
    public void testPostApiPostOK() {
        authUser(0);

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

        deleteUser();
    }


    @Test
    @Transactional
    public void testPutApiPostUnAuth() {
        saveUser(0);
        initPost(user.getId());

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

        deleteUser();
    }

    @Test
    @Transactional
    public void testPutApiPostBadTitle() {
        authUser(0);
        initPost(user.getId());

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

        deleteUser();
    }

    @Test
    @Transactional
    public void testPutApiPostShortText() {
        authUser(0);
        initPost(user.getId());

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

        deleteUser();
    }

    @Test
    @Transactional
    public void testPutApiPostLongText() {
        authUser(0);
        initPost(user.getId());

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

        deleteUser();
    }

    @Test
    @Transactional
    public void testPutApiPostBadPostId() {
        authUser(0);
        initPost(user.getId());

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

        deleteUser();
    }

    @Test
    @Transactional
    public void testPutApiPostOK() {
        authUser(0);
        initPost(user.getId());

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

        deleteUser();
    }


    @Test
    @SneakyThrows
    @Transactional
    public void testGetApiPostMode1() {
        ResponseEntity<?> response = postService.getApiPost(0, 10, "");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());
    }

    @Test
    @Transactional
    public void testGetApiPostMode2() {
        ResponseEntity<?> response = postService.getApiPost(0, 10, "popular");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());
    }

    @Test
    @Transactional
    public void testGetApiPostMode3() {
        ResponseEntity<?> response = postService.getApiPost(0, 10, "best");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());
    }

    @Test
    @Transactional
    public void testGetApiPostMode4() {
        ResponseEntity<?> response = postService.getApiPost(0, 10, "early");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());
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
        ResponseEntity<?> response = postService.getApiPostSearch(0, 10, "");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());
    }

    @Test
    public void testGetApiPostSearchTitleQuery() {
        authUser(0);
        initPost(user.getId());

        ResponseEntity<?> response = postService.getApiPostSearch(0, 10, "Title");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deleteUser();
    }

    @Test
    public void testGetApiPostSearchTextQuery() {
        authUser(0);
        initPost(user.getId());

        ResponseEntity<?> response = postService.getApiPostSearch(0, 10, "Text");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deleteUser();
    }


    @Test
    @Transactional
    public void testGetApiPostIdPostNotFound() {
        try {
            postService.getApiPostId(0);
        }
        catch (Exception e) {
            Assert.assertEquals(
                e.getMessage(),
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Post id=" + 0 + " not found").getMessage());
        }
    }

    @Test
    @Transactional
    public void testGetApiPostIdUnAuth() {
        saveUser(0);
        initPost(user.getId());
        SecurityContextHolder.clearContext();

        ResponseEntity<?> response = postService.getApiPostId(post.getId());
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());

        deleteUser();
    }

    @Test
    public void testGetApiPostIdAuthor() {
        authUser(0);
        initPost(user.getId());

        ResponseEntity<?> response = postService.getApiPostId(post.getId());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        PostByIdResponse pbir = (PostByIdResponse) response.getBody();
        Assert.assertNotNull(pbir);
        Assert.assertEquals(postService.postByIdToJSON(post), pbir);

        deleteUser();
    }

    @Test
    public void testGetApiPostIdModerator() {
        authUser(1);
        initPost(user.getId());

        ResponseEntity<?> response = postService.getApiPostId(post.getId());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        PostByIdResponse pbir = (PostByIdResponse) response.getBody();
        Assert.assertNotNull(pbir);
        Assert.assertEquals(postService.postByIdToJSON(post), pbir);

        deleteUser();
    }

    @Test
    public void testGetApiPostIdAnonymous() {
        authUser(0);
        initPost(user.getId());
        SecurityContextHolder.getContext()
            .setAuthentication(new AnonymousAuthenticationToken("anonymous",
                "anonymous", Collections.singleton(new SimpleGrantedAuthority("anonymous"))));

        ResponseEntity<?> response = postService.getApiPostId(post.getId());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        PostByIdResponse pbir = (PostByIdResponse) response.getBody();
        Assert.assertNotNull(pbir);
        Assert.assertNotEquals(postService.postByIdToJSON(post), pbir);
        Assert.assertNotEquals(0, postsRepository.findById(post.getId()).get().getViewCount());

        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiPostIdAnonymousNonActivePost() {
        SecurityContextHolder.getContext()
            .setAuthentication(new AnonymousAuthenticationToken("anonymous",
                "anonymous", Collections.singleton(new SimpleGrantedAuthority("anonymous"))));
        saveUser(0);
        initPost(user.getId());
        post.setIsActive(0);
        postsRepository.save(post);

        ResponseEntity<?> response = postService.getApiPostId(post.getId());
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assert.assertNull(response.getBody());

        deleteUser();
    }

    @Test
    public void testGetApiPostByDateToday() {
        authUser(0);
        initPost(user.getId());

        date = dateFormat.format(post.getTime());

        ResponseEntity<?> response = postService.getApiPostByDate(0, 10, date);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deleteUser();
    }

    @Test
    public void testGetApiPostByDateZeroDate() {
        authUser(0);
        initPost(user.getId());

        date = dateFormat.format(new Date(0));
        ResponseEntity<?> response = postService.getApiPostByDate(0, 10, date);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertEquals(0, apr.getCount());

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
    public void testGetApiPostByTag200() {
        authUser(0);
        initPost(user.getId());
        saveTag();

        ResponseEntity<?> response = postService.getApiPostByTag(0, 10, tagName);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

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
    public void testGetApiPostModerationNotModerator() {
        authUser(0);

        ResponseEntity<?> response = postService.getApiPostModeration(0, 10, "");
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());

        deleteUser();
    }

    @Test
    public void testGetApiPostModeration200New() {
        authUser(1);
        initPost(user.getId());
        ModerationStatuses status = ModerationStatuses.NEW;
        post.setModerationStatus(status);
        postsRepository.save(post);

        ResponseEntity<?> response = postService.getApiPostModeration(0, 10, status.toString());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

        deleteUser();
    }

    @Test
    public void testGetApiPostModeration200Declined() {
        authUser(1);
        initPost(user.getId());
        post.setModeratorId(user.getId());
        ModerationStatuses status = ModerationStatuses.DECLINED;
        post.setModerationStatus(status);
        postsRepository.save(post);

        ResponseEntity<?> response = postService.getApiPostModeration(0, 10, status.toString());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertNotEquals(0, apr.getCount());

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
    public void testGetApiPostMyBadStatus() {
        authUser(0);
        initPost(user.getId());

        ResponseEntity<?> response = postService.getApiPostMy(0, 10, "");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertEquals(0, apr.getCount());

        deleteUser();
    }

    @Test
    public void testGetApiPostMy200() {
        authUser(0);
        setNoValue(moderationStatus);
        initPost(user.getId());
        restoreValue(moderationStatus);

        ResponseEntity<?> response = postService.getApiPostMy(0, 10, "published");
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiPostResponse apr = (ApiPostResponse) response.getBody();
        Assert.assertNotNull(apr);
        Assert.assertEquals(1, apr.getCount());

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
        authUser(1);

        RequestApiModeration request = new RequestApiModeration();
        ResponseEntity<?> response = postService.postApiModeration(request);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertNull(response.getBody());

        deleteUser();
    }

    @Test
    @Transactional
    public void testPostApiModerationBadStatus() {
        authUser(1);

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
        authUser(0);
        initPost(user.getId());

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
    public void testPostApiModerationNullPostId() {
        authUser(1);

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
        authUser(1);
        initPost(user.getId());

        RequestApiModeration request = new RequestApiModeration();
        request.setPostId(post.getId());
        request.setDecision("accept");
        ResponseEntity<?> response = postService.postApiModeration(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ResultResponse result = (ResultResponse) response.getBody();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isResult());

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
        authUser(0);

        ResponseEntity<?> response = postService.getApiStatisticsMy();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());

        deleteUser();
    }

    @Test
    @Transactional
    public void testGetApiStatisticsAll401() {
        setNoValue(sip);

        ResponseEntity<?> response = postService.getApiStatisticsAll();
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());

        restoreValue(sip);
    }

    @Test
    @Transactional
    public void testGetApiStatisticsAll200Anonymous() {
        setYesValue(sip);

        ResponseEntity<?> response = postService.getApiStatisticsAll();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());

        restoreValue(sip);
    }

    @Test
    @Transactional
    public void testGetApiStatisticsAll200User() {
        setNoValue(sip);
        authUser(0);

        ResponseEntity<?> response = postService.getApiStatisticsAll();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());

        restoreValue(sip);
        deleteUser();
    }
}