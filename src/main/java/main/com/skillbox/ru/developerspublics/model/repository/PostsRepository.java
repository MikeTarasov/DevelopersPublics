package main.com.skillbox.ru.developerspublics.model.repository;

import main.com.skillbox.ru.developerspublics.model.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PostsRepository extends PagingAndSortingRepository<Post, Integer> {

    Post findByTitle(String title);

    List<Post> findByIsActiveAndModerationStatusAndTimeBefore(
            int isActive, String moderationStatus, Date time, Pageable pageable);


    List<Post> findByIsActiveAndModerationStatusAndTimeAfterAndTimeBefore(
            int isActive, String moderationStatus, Date dayBefore, Date dayAfter);

    @Query(value = "SELECT * FROM posts WHERE is_active= :isActive AND moderation_status= :modStatus AND " +
            "time<= :date AND (text LIKE :query OR title LIKE :query)", nativeQuery = true)
    List<Post> findActivePostsByQuery(@Param("isActive") int isActive,
                                      @Param("modStatus") String moderationStatus,
                                      @Param("date") Date time,
                                      @Param("query") String query,
                                      Pageable pageable);

    List<Post> findByUserId(int userId);

    List<Post> findByModerationStatus(String moderationStatus);

    @Query(value = "SELECT COUNT(*) FROM posts WHERE is_active=1 AND moderation_status='ACCEPTED'" +
            "AND time<= :time", nativeQuery = true)
    int countActivePosts(@Param("time") Date time);

    @Query(value = "SELECT COUNT(*) FROM posts WHERE moderation_status='NEW'", nativeQuery = true)
    int getModerationCount();

    @Query(value = "SELECT * FROM posts p WHERE is_active= :isActive AND moderation_status= :modStatus AND " +
            "time<= :date ORDER BY (SELECT COUNT(*) FROM post_votes pv WHERE pv.post_id=p.id AND pv.value=1)" +
            " DESC, (SELECT COUNT(*) FROM post_votes pv WHERE pv.post_id=p.id AND pv.value=-1), p.time DESC",
            nativeQuery = true)
    List<Post> getBestPosts(@Param("isActive") int isActive,
                            @Param("modStatus") String moderationStatus,
                            @Param("date") Date time,
                            Pageable pageable);

    @Query(value = "SELECT * FROM posts p WHERE is_active= :isActive AND moderation_status= :modStatus AND " +
            "time<= :date ORDER BY (SELECT COUNT(*) FROM post_comments pc WHERE pc.post_id=p.id)" +
            " DESC, p.view_count DESC, p.time DESC",
            nativeQuery = true)
    List<Post> getPopularPosts(@Param("isActive") int isActive,
                            @Param("modStatus") String moderationStatus,
                            @Param("date") Date time,
                            Pageable pageable);
}
