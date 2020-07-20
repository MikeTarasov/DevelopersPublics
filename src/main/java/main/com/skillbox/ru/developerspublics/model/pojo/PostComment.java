package main.com.skillbox.ru.developerspublics.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
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
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post commentPost;

    @Transient
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User commentUser;

//    public String toString() {
//        return "{\"id\": " + id +
//                ",\"time\": " + time + //TODO "Вчера, 17:32"
//                ", \"text\": " + text + //TODO "Текст комментария в формате HTML"
//                ", \"user\": " + getCommentUser().toStringIdNamePhoto() + "}";
//    }
    // "id": 776,
    // "time": "Вчера, 17:32",
    // "text": "Текст комментария в формате HTML",
    // "user":
    // {
    // "id": 88,
    // "name": "Дмитрий Петров",
    // "photo": "/avatars/ab/cd/ef/52461.jpg"
    // }
}
