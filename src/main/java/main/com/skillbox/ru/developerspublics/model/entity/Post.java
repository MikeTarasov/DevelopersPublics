package main.com.skillbox.ru.developerspublics.model.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "posts")
@Entity
public class Post {

  //id в БД
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  //признак активности
  //0 -> hidden, 1 -> active
  @Column(columnDefinition = "INT", nullable = false)
  private int isActive;

  //статус проверки модератором
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ModerationStatuses moderationStatus;

  //id модератора, установившего статус или null
  @Column(name = "moderator_id")
  private Integer moderatorId;

  //id автора поста
  @Column(name = "user_id", nullable = false)
  private int userId;

  //дата создания поста
  @Column(columnDefinition = "TIMESTAMP", nullable = false)
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

  //привязанный автор поста
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User userPost;

  //привязанный список лайков/дислайков
  @OneToMany(mappedBy = "postVote", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @Fetch(value = FetchMode.SUBSELECT)
  private List<PostVote> postVotes;

  //привязанный список комментариев к посту
  @OneToMany(mappedBy = "commentPost", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @Fetch(value = FetchMode.SUBSELECT)
  private List<PostComment> postComments;

  //привязанный список тэг-пост
  @OneToMany(mappedBy = "postTag", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @Fetch(value = FetchMode.SUBSELECT)
  private List<TagToPost> tagToPosts;

  //get timestamp in seconds
  public long getTimestamp() {
    return time.getTime() / 1000;
  }

  //timestamp in milliseconds to java.util.Date
  public void setTime(long timestamp) {
    time = Date.from(Instant.ofEpochMilli(timestamp));
  }

  public List<PostVote> getPostVotes() {
    if (postVotes == null) postVotes = new ArrayList<>();
    return postVotes;
  }

  public List<PostComment> getPostComments() {
    if (postComments == null) postComments = new ArrayList<>();
    return postComments;
  }

  public List<TagToPost> getTagToPosts() {
    if (tagToPosts == null) tagToPosts = new ArrayList<>();
    return tagToPosts;
  }
}
