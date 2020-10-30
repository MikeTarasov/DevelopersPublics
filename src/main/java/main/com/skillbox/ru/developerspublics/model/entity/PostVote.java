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
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "post_votes")
@Entity
public class PostVote {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "user_id", nullable = false)
  private int userId;

  @Column(name = "post_id", nullable = false)
  private int postId;

  @Column(columnDefinition = "TIMESTAMP", nullable = false)
  private Date time;

  @Column(columnDefinition = "INT", nullable = false)
  private int value;  // +1 -> like, -1 -> dislike

  @Transient
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @LazyCollection(LazyCollectionOption.EXTRA)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User userVote;

  @Transient
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @LazyCollection(LazyCollectionOption.EXTRA)
  @JoinColumn(name = "post_id", insertable = false, updatable = false)
  private Post postVote;

  //get timestamp in seconds
  public long getTimestamp() {
    return time.getTime() / 1000;
  }

  //timestamp in milliseconds to java.util.Date
  public void setTime(long timestamp) {
    time = Date.from(Instant.ofEpochMilli(timestamp));
  }
}
