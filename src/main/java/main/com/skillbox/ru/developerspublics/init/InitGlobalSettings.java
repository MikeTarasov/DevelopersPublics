package main.com.skillbox.ru.developerspublics.init;

import main.com.skillbox.ru.developerspublics.model.GlobalSetting;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
import main.com.skillbox.ru.developerspublics.repository.GlobalSettingsRepository;

public class InitGlobalSettings
{
    public static void init(GlobalSettingsRepository repository) {
        //проверяем, что все настройки есть
        if (repository.count() != GlobalSettingsCodes.values().length) {
            //если нет - перезаполняем таблицу БД настройками по умолчанию
            repository.deleteAll();
            int id = 1;
            for (GlobalSettingsCodes code : GlobalSettingsCodes.values()) {
                repository.save(new GlobalSetting(
                        id,
                        code.toString(),
                        code.name(),
                        GlobalSettingsValues.NO.toString())
                );
                id++;
            }
        }
    }
}
