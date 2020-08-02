package main.com.skillbox.ru.developerspublics.init;


import main.com.skillbox.ru.developerspublics.service.CaptchaCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeleteOldCaptcha {
    @Autowired
    private CaptchaCodeService captchaCodeService;

    private static final long captchaLifeTime = 60 * 60 * 1000; //1 час -> milliseconds

    @Scheduled(fixedRate = captchaLifeTime)
    public void autoDeleteOldCaptcha() {
        new Thread(() -> {
            System.out.println("autoDeleteOldCaptcha ");
            captchaCodeService.deleteOldCaptcha(captchaLifeTime);
        }).start();
    }
}
