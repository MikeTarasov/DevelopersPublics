package main.com.skillbox.ru.developerspublics.model.repository;

import main.com.skillbox.ru.developerspublics.model.entity.Uploads;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadsRepository extends JpaRepository<Uploads, Integer> {
    Uploads findByPath(String path);
}
