package main.com.skillbox.ru.developerspublics.model.repository;


import java.util.List;
import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TagToPostsRepository extends JpaRepository<TagToPost, Integer> {

  TagToPost findByPostIdAndTagId(int postId, int tagId);

  List<TagToPost> findByTagId(int tagId);

  List<TagToPost> findByPostId(int postId);

  @Query(value = "SELECT COUNT(*) FROM tag2post WHERE tag_id= :tagId", nativeQuery = true)
  int countTagsToPost(@Param("tagId") int tagId);

  @Query(value = "SELECT * FROM tag2post JOIN tags ON tag2post.tag_id=tags.id WHERE tags.name= :tagName",
      nativeQuery = true)
  List<TagToPost> getTagToPostsByTagName(@Param("tagName") String tagName);
}
