package main.com.skillbox.ru.developerspublics.repository;

import main.com.skillbox.ru.developerspublics.model.entity.CaptchaCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaptchaCodesRepository extends CrudRepository<CaptchaCode, Integer> {
}
