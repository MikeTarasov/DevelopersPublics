package main.com.skillbox.ru.developerspublics.model;

import lombok.*;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.sql.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "posts")
@Entity
public class Post
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(columnDefinition = "TINYINT", nullable = false)
    private int isActive;   //0 -> hidden, 1 -> active

    @Column(columnDefinition = "ENUM('NEW','ACCEPTED','DECLINED')", nullable = false)
    @ColumnDefault("'NEW'")
    private Enum<ModerationStatuses> moderationStatus;

    @Column(name = "moderator_id")
    private Integer moderatorId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(nullable = false)
    private Date time;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(nullable = false)
    private int viewCount;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id", insertable = false, updatable = false)
    private User moderatorPost;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User userPost;

    @OneToMany(mappedBy = "postVote", fetch = FetchType.LAZY)
    private List<PostVote> postVotes;

    @OneToMany(mappedBy = "commentPost", fetch = FetchType.LAZY)
    private List<PostComment> postComments;

    @OneToMany(mappedBy = "postTag", fetch = FetchType.LAZY)
    private List<TagToPost> tagToPosts;


    private String getAnnounce(int size) {
        return ((text.length() > size) ? text.substring(0, size) : text);
    }

    private int getLikesDislikesCount(int value) {
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

    private int getCommentsCount() {
        return postComments.size();
    }

    public String toString() {
        return "{\"id\": " + id +
                ",\"time\": " + time +
                ",\"user\":" + userPost + //TODO
                ",\"title\": " + title +
                ",\"announce\": " + getAnnounce(1000) +
                ",\"likeCount\": " + getLikesDislikesCount(1) +
                ",\"dislikeCount\": " + getLikesDislikesCount(-1) +
                ",\"commentCount\": " + getCommentsCount() +
                ",\"viewCount\": " + viewCount + "}";
    }
}
