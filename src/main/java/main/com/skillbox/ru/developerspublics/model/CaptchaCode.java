package main.com.skillbox.ru.developerspublics.model;

import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
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

    @Transient
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public CaptchaCode(String code) {
        time = new Date(System.currentTimeMillis());
        this.code = code;
        secretCode = bCryptPasswordEncoder.encode(code);
    }
}
