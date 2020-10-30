package main.com.skillbox.ru.developerspublics.model.entity;

import java.time.Instant;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "captcha_codes")
@Entity
public class CaptchaCode {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false)
  private int id;

  @Column(columnDefinition = "TIMESTAMP", nullable = false)
  private Date time;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String code;

  @Column(name = "secret_code", columnDefinition = "TEXT", nullable = false)
  private String secretCode;

  //get timestamp
  public long getTimestamp() {
    return time.getTime();
  }

  //timestamp in milliseconds to java.util.Date
  public void setTime(long timestamp) {
    time = Date.from(Instant.ofEpochMilli(timestamp));
  }
}
