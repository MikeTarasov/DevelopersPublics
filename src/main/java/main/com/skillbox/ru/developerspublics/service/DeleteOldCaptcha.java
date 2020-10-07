package main.com.skillbox.ru.developerspublics.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeleteOldCaptcha {
    @Autowired
    private CaptchaCodeService captchaCodeService;

    @Value("${captcha.life.time}")
    private long lifeTime;

    @Scheduled(fixedRateString = "${captcha.life.time}")
    public void autoDeleteOldCaptcha() {
        new Thread(() -> {
            captchaCodeService.deleteOldCaptcha(lifeTime);
        }).start();
    }
}
