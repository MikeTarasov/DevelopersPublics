//package main.com.skillbox.ru.developerspublics.init;
//
//import main.com.skillbox.ru.developerspublics.model.GlobalSetting;
//import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
//import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
//import main.com.skillbox.ru.developerspublics.repository.GlobalSettingsRepository;
//
//public class InitGlobalSettings
//{
//    public static void init(GlobalSettingsRepository repository) {
//        System.out.println("init");
//        //проверяем, что все настройки есть
//        if (repository.count() != GlobalSettingsCodes.values().length) {
//            //если нет - перезаполняем таблицу БД настройками по умолчанию
//            repository.deleteAll();
//            int id = 1;
//            for (GlobalSettingsCodes code : GlobalSettingsCodes.values()) {
//                System.out.println(code.name() + " <=> " + code.getDesc());
//                repository.save(new GlobalSetting(
//                        id,
//                        code.name(),
//                        code.getDesc(),
//                        GlobalSettingsValues.NO.toString())
//                );
//                id++;
//            }
//        }
//    }
//}
