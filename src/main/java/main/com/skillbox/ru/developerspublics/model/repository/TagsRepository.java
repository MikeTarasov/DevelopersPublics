package main.com.skillbox.ru.developerspublics.model.repository;


import java.util.Date;
import java.util.HashSet;
import main.com.skillbox.ru.developerspublics.model.entity.Tag;
import main.com.skillbox.ru.developerspublics.model.projections.TagResponseProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TagsRepository extends JpaRepository<Tag, Integer> {

  Tag findByName(String name);

  @Query(value =
      "SELECT tags.name FROM tags JOIN tag2post ON tags.id=tag2post.tag_id JOIN posts ON " +
          "tag2post.post_id=posts.id WHERE posts.is_active=1 AND posts.moderation_status='ACCEPTED' "
          + "AND posts.time<= :time", nativeQuery = true)
  HashSet<String> findActiveTags(@Param("time") Date time);

  @Query(value = "SELECT name, (SELECT COUNT(*) FROM tag2post t2p WHERE t.id=t2p.tag_id) AS weight "
      + "FROM tags t JOIN tag2post ttp ON t.id=ttp.tag_id JOIN posts p ON ttp.post_id=p.id "
      + "WHERE p.is_active=1 AND p.moderation_status='ACCEPTED' AND p.time<= :time",
      nativeQuery = true)
  HashSet<TagResponseProjection> getTagResponseSet(@Param("time") Date time);
}
