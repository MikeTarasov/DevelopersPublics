package main.com.skillbox.ru.developerspublics.model.repository;

import main.com.skillbox.ru.developerspublics.model.entity.GlobalSetting;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalSettingsRepository extends JpaRepository<GlobalSetting, Integer> {

  GlobalSetting findGlobalSettingByCode(GlobalSettingsCodes code);
}
