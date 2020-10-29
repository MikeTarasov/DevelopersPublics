package main.com.skillbox.ru.developerspublics.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeleteOldCaptcha {

  private final CaptchaCodeService captchaCodeService;

  @Value("${captcha.life.time}")
  private long lifeTime;

  @Autowired
  public DeleteOldCaptcha(
      CaptchaCodeService captchaCodeService) {
    this.captchaCodeService = captchaCodeService;
  }

  @Scheduled(fixedRateString = "${captcha.life.time}")
  public void autoDeleteOldCaptcha() {
    new Thread(() -> {
      captchaCodeService.deleteOldCaptcha(lifeTime);
    }).start();
  }
}