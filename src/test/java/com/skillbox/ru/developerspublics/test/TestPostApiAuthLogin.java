//package com.skillbox.ru.developerspublics.test;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.SneakyThrows;
//import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthLogin;
//import main.com.skillbox.ru.developerspublics.config.WebMVCConfig;
//import main.com.skillbox.ru.developerspublics.controllers.ApiAuthController;
//import main.com.skillbox.ru.developerspublics.model.entity.User;
//import main.com.skillbox.ru.developerspublics.service.*;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.RequestBuilder;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import java.awt.*;
//
//
//@RunWith(SpringRunner.class)
//@WebMvcTest(ApiAuthController.class)
////@ComponentScan("main.com.skillbox.ru.developerspublics")
////@ContextConfiguration(classes = WebMVCConfig.class)
//public class TestPostApiAuthLogin {
//    private final String email = "test@test.test";
//    private final String name = "Test";
//    private final String password = "test123";
//    private final User testUser = new User(email, name, password);
//
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private UserService userService;
//    @MockBean
//    private CaptchaCodeService captchaCodeService;
//    @MockBean
//    private GlobalSettingService globalSettingService;
//    @MockBean
//    private PostCommentService postCommentService;
//    @MockBean
//    private PostService postService;
//    @MockBean
//    private PostVoteService postVoteService;
//    @MockBean
//    private TagService tagService;
//    @MockBean
//    private TagToPostService tagToPostService;
//
////    @Before
////    public void init() {
////        this.testUser = new User(email, email, password);
////        userService.saveUser(testUser);
////    }
////
////    @After
////    public void cleanDB() {
////        userService.deleteUser(testUser);
////    }
//
//    @Test
//    @SneakyThrows
//    public void whenLoginPasswordCorrect_thenReturnContext() {
////        ResponseEntity<RequestApiAuthLogin> response = restTemplate.postForEntity("/api/auth/login", new RequestApiAuthLogin(), RequestApiAuthLogin.class);
//
////        Mockito.when(userService.postApiAuthLogin(Mockito.any())).thenReturn(ResponseEntity.status(200).body(userService.getResultUserResponse(testUser)));
////        RequestBuilder requestBuilder = MockMvcRequestBuilders
////                .post("/api/auth/login")
////                .accept(MediaType.APPLICATION_JSON)
////                .content()
////        mockMvc.perform()
////        RequestBuilder requestBuilder = servletContext -> {
////            MockHttpServletRequest request = new MockHttpServletRequest();
////            RequestApiAuthLogin requestApiAuthLogin = new RequestApiAuthLogin(email, password);
//////            request.addParameter("e_mail", email);
//////            request.addParameter("password", password);
////            request.setMethod("POST");
////            request.setRequestURI("/api/auth/login");
////            request.addHeader("body", requestApiAuthLogin);
////            return request;
////        };
////
////
////        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
////
//////        result.getResponse().getStatus();
//////        result.getResponse().getContentAsString();
////
////        System.out.println(result.getResponse().getStatus());
////        System.out.println(result.getResponse().getContentAsString());
//
//
////        given
////        mockMvc.perform(get("").)
//
//
//    }
//}
