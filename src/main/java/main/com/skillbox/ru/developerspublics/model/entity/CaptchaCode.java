package main.com.skillbox.ru.developerspublics.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "captcha_codes")
@Entity
public class CaptchaCode
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @Column(columnDefinition = "DATETIME", nullable = false)
    private Date time;

    @Column(columnDefinition = "TINYTEXT", nullable = false)
    private String code;

    @Column(name = "secret_code", columnDefinition = "TINYTEXT", nullable = false)
    private String secretCode;

    //get timestamp in seconds
    public long getTimestamp() {
        return time.getTime() / 1000;
    }

    //timestamp in milliseconds to java.util.Date
    public void setTime(long timestamp) {
        time = Date.from(Instant.ofEpochMilli(timestamp));
    }
}
