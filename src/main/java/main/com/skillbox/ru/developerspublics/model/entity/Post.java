package main.com.skillbox.ru.developerspublics.model.entity;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "posts")
@Entity
public class Post
{
    //id в БД
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    //признак активности
    //0 -> hidden, 1 -> active
    @Column(columnDefinition = "TINYINT", nullable = false)
    private int isActive;

    //статус проверки модератором
    @Column(columnDefinition = "ENUM('NEW','ACCEPTED','DECLINED')", nullable = false)
    @ColumnDefault("'NEW'")
    private String moderationStatus;

    //id модератора, установившего статус или null
    @Column(name = "moderator_id")
    private Integer moderatorId;

    //id автора поста
    @Column(name = "user_id", nullable = false)
    private int userId;

    //дата создания поста
    @Column(columnDefinition = "DATETIME", nullable = false)
    private Date time;

    //заголовок поста
    @Column(nullable = false)
    private String title;

    //содержание поста
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String text;

    //кол-во просмотров
    @Column(nullable = false)
    private int viewCount;

    //привязанный модератор поста
    @Transient
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "moderator_id")
    private User moderatorPost;

    //привязанный автор поста
    @Transient
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "user_id")
    private User userPost;

    //привязанный список лайков/дислайков
    @Transient
    @OneToMany(mappedBy = "postVote", fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<PostVote> postVotes;

    //привязанный список комментариев к посту
    @Transient
    @OneToMany(mappedBy = "commentPost", fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<PostComment> postComments;

    //привязанный список тэг-пост
    @Transient
    @OneToMany(mappedBy = "postTag", fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<TagToPost> tagToPosts;

    //get timestamp in seconds
    public long getTimestamp() {
        return time.getTime() / 1000;
    }

    //timestamp in milliseconds to java.util.Date
    public void setTime(long timestamp) {
        time = Date.from(Instant.ofEpochMilli(timestamp));
    }
}
