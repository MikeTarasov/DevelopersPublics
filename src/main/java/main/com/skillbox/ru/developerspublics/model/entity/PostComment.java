package main.com.skillbox.ru.developerspublics.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "post_comments")
@Entity
public class PostComment
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    //id комментария PostComment, который комментируем или null (если комментируем пост)
    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "post_id", nullable = false)
    private int postId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(columnDefinition = "DATETIME", nullable = false)
    private Date time;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Transient
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post commentPost;

    @Transient
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User commentUser;

    //get timestamp in seconds
    public long getTimestamp() {
        return time.getTime() / 1000;
    }

    //timestamp in milliseconds to java.util.Date
    public void setTime(long timestamp) {
        time = Date.from(Instant.ofEpochMilli(timestamp));
    }
}
