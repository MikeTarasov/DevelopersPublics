package main.com.skillbox.ru.developerspublics.repository;

import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TagToPostsRepository extends JpaRepository<TagToPost, Integer> {
    TagToPost findByPostIdAndTagId(int postId, int tagId);

    List<TagToPost> findByTagId(int tagId);

    List<TagToPost> findByPostId(int postId);
}
