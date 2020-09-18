package main.com.skillbox.ru.developerspublics.model.repository;


import main.com.skillbox.ru.developerspublics.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.HashSet;


@Repository
public interface TagsRepository extends JpaRepository<Tag, Integer> {
    Tag findByName(String name);

    @Query(value = "SELECT tags.name FROM tags JOIN tag2post ON tags.id=tag2post.tag_id JOIN posts ON " +
            "tag2post.post_id=posts.id WHERE posts.is_active=1 AND posts.moderation_status='ACCEPTED' " +
            "AND posts.time<= :time", nativeQuery = true)
    HashSet<String> findActiveTags(@Param("time") Date time);



}
