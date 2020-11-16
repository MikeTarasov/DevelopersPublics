package main.com.skillbox.ru.developerspublics.model.entity;

import java.time.Instant;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "post_comments")
@Entity
public class PostComment {

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

  @Column(columnDefinition = "TIMESTAMP", nullable = false)
  private Date time;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String text;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "post_id", insertable = false, updatable = false)
  private Post commentPost;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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
