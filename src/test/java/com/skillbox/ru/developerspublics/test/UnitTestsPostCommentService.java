package com.skillbox.ru.developerspublics.test;

import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiComment;
import main.com.skillbox.ru.developerspublics.service.PostCommentService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsPostCommentService {
    private PostCommentService postCommentService;

    @Autowired
    public void UnitTestsPostVoteService(PostCommentService postCommentService) {
        this.postCommentService = postCommentService;
    }

    @Test
    @Transactional
    public void testPostApiCommentNoName() {
        RequestApiComment request = new RequestApiComment(0, 0, "");
        ResponseEntity<?> response = postCommentService.postApiComment(request);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
        Assert.assertNull(response.getBody());
    }
}
