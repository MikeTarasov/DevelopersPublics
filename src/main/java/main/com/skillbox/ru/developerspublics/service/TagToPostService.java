package main.com.skillbox.ru.developerspublics.service;


import java.util.List;
import java.util.Optional;
import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;
import main.com.skillbox.ru.developerspublics.model.repository.TagToPostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TagToPostService {

  private final TagToPostsRepository tagToPostsRepository;

  @Autowired
  public TagToPostService(TagToPostsRepository tagToPostsRepository) {
    this.tagToPostsRepository = tagToPostsRepository;
  }


  public List<TagToPost> getTagToPostsByPostId(int postId) {
    return tagToPostsRepository.findByPostId(postId);
  }


  public List<TagToPost> getTagToPostsByTagId(int tagId) {
    return tagToPostsRepository.findByTagId(tagId);
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