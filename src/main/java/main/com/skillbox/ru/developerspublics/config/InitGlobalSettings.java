package main.com.skillbox.ru.developerspublics.config;

import main.com.skillbox.ru.developerspublics.model.GlobalSetting;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
import main.com.skillbox.ru.developerspublics.repository.GlobalSettingsRepository;

public class InitGlobalSettings
{
    public static void init(GlobalSettingsRepository repository) {
        if (repository.count() != 3) {
            repository.deleteAll();
            repository.save(new GlobalSetting(
                    1,
                    GlobalSettingsCodes.MULTI_USER_MODE.toString(),
                    "Многопользовательский режим",
                    GlobalSettingsValues.NO.toString()));

            repository.save(new GlobalSetting(
                    2,
                    GlobalSettingsCodes.POST_PREMODERATION.toString(),
                    "Премодерация постов",
                    GlobalSettingsValues.YES.toString()));

            repository.save(new GlobalSetting(
                    3,
                    GlobalSettingsCodes.STATISTICS_IS_PUBLIC.toString(),
                    "Показывать всем статистику блога",
                    GlobalSettingsValues.YES.toString()));
        }
    }
}
