package main.com.skillbox.ru.developerspublics.model;

import lombok.*;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "posts")
@Entity
public class Post
{
    //размер анонса
    private static final int announceSize = 1000;

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
    private Enum<ModerationStatuses> moderationStatus;

    //id модератора, установившего статус или null
    @Column(name = "moderator_id")
    private Integer moderatorId;

    //id автора поста
    @Column(name = "user_id", nullable = false)
    private int userId;

    //дата создания поста
    @Column(nullable = false)
    private Date time;

    //заголовок поста
    @Column(nullable = false)
    private String title;

    //содержание поста
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    //кол-во просмотров
    @Column(nullable = false)
    private int viewCount;

    //привязанный модератор поста
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id", insertable = false, updatable = false)
    private User moderatorPost;

    //привязанный автор поста
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User userPost;

    //привязанный список лайков/дислайков
    @OneToMany(mappedBy = "postVote", fetch = FetchType.LAZY)
    private List<PostVote> postVotes;

    //привязанный список комментариев к посту
    @OneToMany(mappedBy = "commentPost", fetch = FetchType.LAZY)
    private List<PostComment> postComments;

    //привязанный список тэг-пост
    @OneToMany(mappedBy = "postTag", fetch = FetchType.LAZY)
    private List<TagToPost> tagToPosts;


    public String getAnnounce() {
        return ((text.length() > announceSize) ? text.substring(0, announceSize) : text);
    }

    public int getLikesDislikesCount(int value) {
        //value = +1 -> like
        //value = -1 -> dislike
        int count = 0;
        for (PostVote postVote : postVotes) {
            if (postVote.getValue() == value) {
                count++;
            }
        }
        return count;
    }

    public int getCommentsCount() {
        return postComments.size();
    }

    public String toString() {
        return "{\"id\": " + id +
                ",\"time\": " + time +  //TODO GET /api/post/ -> "time": "Вчера, 17:32"
                ",\"user\":" + userPost +
                ",\"title\": " + title +
                ",\"announce\": " + getAnnounce() +
                ",\"likeCount\": " + getLikesDislikesCount(1) +
                ",\"dislikeCount\": " + getLikesDislikesCount(-1) +
                ",\"commentCount\": " + getCommentsCount() +
                ",\"viewCount\": " + viewCount + "}";
    }
}
