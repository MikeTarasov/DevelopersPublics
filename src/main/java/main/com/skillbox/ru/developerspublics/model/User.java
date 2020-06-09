package main.com.skillbox.ru.developerspublics.model;

import lombok.*;
import javax.persistence.*;
import java.sql.Date;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Entity
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "is_moderator", columnDefinition = "TINYINT", nullable = false)
    private int isModerator;

    @Column(name = "reg_time", nullable = false)
    private Date regTime;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String code;

    @Column(columnDefinition = "TEXT")
    private String photo;

    @OneToMany(mappedBy = "moderatorPost", fetch = FetchType.LAZY)
    private List<Post> moderatorPosts;

    @OneToMany(mappedBy = "userPost", fetch = FetchType.LAZY)
    private List<Post> userPosts;

    @OneToMany(mappedBy = "userVote", fetch = FetchType.LAZY)
    private List<PostVote> userPostVotes;

    @OneToMany(mappedBy = "commentUser", fetch = FetchType.LAZY)
    private List<PostComment> userPostComments;

    public String toString() {
        return "{\"id\": " + id + ", \"name\": " + name + "}";
    }

    public String toStringIdNamePhoto() {
        return "{\"id\": " + id + ", \"name\": " + name + ", \"photo\": " + photo + "}";
    }
}
