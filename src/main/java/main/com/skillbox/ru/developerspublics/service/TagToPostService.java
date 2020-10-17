package main.com.skillbox.ru.developerspublics.service;


import java.util.List;
import java.util.Optional;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.Tag;
import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;
import main.com.skillbox.ru.developerspublics.model.repository.PostsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.TagToPostsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.TagsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TagToPostService {

    private final PostsRepository postsRepository;
    private final TagsRepository tagsRepository;
    private final TagToPostsRepository tagToPostsRepository;

    @Autowired
    public TagToPostService(
        PostsRepository postsRepository,
        TagsRepository tagsRepository,
        TagToPostsRepository tagToPostsRepository) {
        this.postsRepository = postsRepository;
        this.tagsRepository = tagsRepository;
        this.tagToPostsRepository = tagToPostsRepository;
    }


    public List<TagToPost> getTagToPostsByPostId(int postId) {
        return tagToPostsRepository.findByPostId(postId);
    }


    public List<TagToPost> getTagToPostByTagId(int tagId) {
        return tagToPostsRepository.findByTagId(tagId);
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


    private Post getPostTag(int postId) {
        return postsRepository.findById(postId).orElse(null);
    }


    private Tag getTagPost(int tagId) {
        return tagsRepository.findById(tagId).orElse(null);
    }


    private void initTagToPost(@NonNull TagToPost tagToPost) {
        tagToPost.setPostTag(getPostTag(tagToPost.getPostId()));
        tagToPost.setTagPost(getTagPost(tagToPost.getTagId()));
    }


    @Transactional
    public void saveTagToPost(int postId, int tagId) {
        //проверим на уникальность
        TagToPost tagToPost = tagToPostsRepository.findByPostIdAndTagId(postId, tagId);

        if (tagToPost == null) {
            tagToPost = new TagToPost();
            tagToPost.setPostId(postId);
            tagToPost.setTagId(tagId);
            tagToPostsRepository.save(tagToPost);
        }
    }


    public void deleteTagToPost(TagToPost tagToPost) {
        Optional<TagToPost> tagToPostDB = tagToPostsRepository.findById(tagToPost.getId());
        tagToPostDB.ifPresent(tagToPostsRepository::delete);
    }
}