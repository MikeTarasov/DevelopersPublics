package main.com.skillbox.ru.developerspublics.service;


import main.com.skillbox.ru.developerspublics.model.entity.Tag;
import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.repository.TagToPostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class TagToPostService {
    @Autowired
    private TagToPostsRepository tagToPostsRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private TagService tagService;

    public List<TagToPost> getInitTagToPosts() {
        List<TagToPost> tagToPosts = new ArrayList<>();
        for (TagToPost tagToPostDB : tagToPostsRepository.findAll()) {
            initTagToPost(tagToPostDB);
            tagToPosts.add(tagToPostDB);
        }
        return tagToPosts;
    }

    public List<TagToPost> getTagToPosts() {
        return new ArrayList<>(tagToPostsRepository.findAll());
    }

    public TagToPost getInitTagToPostById(int id) {
        Optional<TagToPost> optionalTagToPost = tagToPostsRepository.findById(id);
        if (optionalTagToPost.isPresent()) {
            TagToPost tagToPost = optionalTagToPost.get();
            initTagToPost(tagToPost);
            return tagToPost;
        }
        return null;
    }

    public TagToPost getTagToPostById(int id) {
        return tagToPostsRepository.findById(id).orElseThrow();
    }

    private void initTagToPost(@NonNull TagToPost tagToPost) {
        tagToPost.setPostTag(getPostTag(tagToPost.getPostId()));
        tagToPost.setTagPost(getTagPost(tagToPost.getTagId()));
    }

    private Post getPostTag(int postId) {
        return postService.getPostById(postId);
    }

    private Tag getTagPost(int tagId) {
        return tagService.getTagById(tagId);
    }

    public void saveTagToPost(int postId, int tagId) {
        //проверим на уникальность
        TagToPost tagToPost = null;
        for (TagToPost tagToPostDB : tagToPostsRepository.findAll()) {
            if (tagToPostDB.getPostId() == postId && tagToPostDB.getTagId() == tagId) tagToPost = tagToPostDB;
        }

        if (tagToPost == null) {
            tagToPost = new TagToPost();
            tagToPost.setPostId(postId);
            tagToPost.setTagId(tagId);
            tagToPostsRepository.save(tagToPost);
        }
    }
}
