package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.model.entity.GlobalSetting;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
import main.com.skillbox.ru.developerspublics.repository.GlobalSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class GlobalSettingService {
    @Autowired
    private GlobalSettingsRepository globalSettingsRepository;

    public List<GlobalSetting> getAllGlobalSettings() {
        List<GlobalSetting> globalSettings = new ArrayList<>();
        for (GlobalSetting globalSettingDB : globalSettingsRepository.findAll()) {
            globalSettings.add(globalSettingDB);
        }
        return globalSettings;
    }

    public GlobalSetting findGlobalSettingByCode(String code) {
        for (GlobalSetting globalSetting : globalSettingsRepository.findAll()) {
            if (globalSetting.getCode().equals(code)) {
                return globalSetting;
            }
        }
        return null;
    }

    public void initGlobalSettings() {
        //проверяем, что все настройки есть
        if (globalSettingsRepository.count() != GlobalSettingsCodes.values().length) {
            //если нет - перезаполняем таблицу БД настройками по умолчанию
            globalSettingsRepository.deleteAll();
            int id = 1;
            for (GlobalSettingsCodes code : GlobalSettingsCodes.values()) {
                System.out.println(code.name() + " <=> " + code.getDesc());
                globalSettingsRepository.save(new GlobalSetting(
                        id,
                        code.name(),
                        code.getDesc(),
                        GlobalSettingsValues.NO.toString())
                );
                id++;
            }
        }
    }

    public boolean setGlobalSettings(boolean multiUserMode, boolean postPremoderation, boolean statisticsIsPublic) {
        boolean hasErrors = false;
        for (GlobalSetting gs : getAllGlobalSettings()) {
            if (gs.getCode().equals(GlobalSettingsCodes.MULTI_USER_MODE.toString()))
                gs.setValue(multiUserMode ? GlobalSettingsValues.YES.toString() : GlobalSettingsValues.NO.toString());
            else if (gs.getCode().equals(GlobalSettingsCodes.POST_PREMODERATION.toString()))
                gs.setValue(postPremoderation ? GlobalSettingsValues.YES.toString() : GlobalSettingsValues.NO.toString());
            else if (gs.getCode().equals(GlobalSettingsCodes.STATISTICS_IS_PUBLIC.toString()))
                gs.setValue(statisticsIsPublic ? GlobalSettingsValues.YES.toString() : GlobalSettingsValues.NO.toString());
            else hasErrors = true;
        }
        return !hasErrors;
    }
}
