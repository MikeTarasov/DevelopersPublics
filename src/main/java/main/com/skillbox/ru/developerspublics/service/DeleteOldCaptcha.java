package main.com.skillbox.ru.developerspublics.service;


import main.com.skillbox.ru.developerspublics.service.CaptchaCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeleteOldCaptcha {
    @Autowired
    private CaptchaCodeService captchaCodeService;

    @Value("${captcha.life.time}") //TODO нужна КОНСТАНТА!!!!!
    private String lifeTime;

    private final long captchaLifeTime = 60 * 60 * 1000; //1 час -> milliseconds

    @Scheduled(fixedRate = captchaLifeTime)
    public void autoDeleteOldCaptcha() {
        new Thread(() -> {
            captchaCodeService.deleteOldCaptcha(captchaLifeTime);
        }).start();
    }
}
