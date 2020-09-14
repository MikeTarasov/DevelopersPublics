package main.com.skillbox.ru.developerspublics.model.repository;

import main.com.skillbox.ru.developerspublics.model.entity.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CaptchaCodesRepository extends JpaRepository<CaptchaCode, Integer> {
    List<CaptchaCode> findByTimeLessThan(Date time);

    CaptchaCode findByCodeAndSecretCode(String code, String secretCode);

    CaptchaCode findBySecretCode(String secretCode);
}
