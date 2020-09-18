package main.com.skillbox.ru.developerspublics.service;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiSettings;
import main.com.skillbox.ru.developerspublics.api.response.BlogInfo;
import main.com.skillbox.ru.developerspublics.api.response.MessageResponse;
import main.com.skillbox.ru.developerspublics.api.response.ResultResponse;
import main.com.skillbox.ru.developerspublics.model.entity.GlobalSetting;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
import main.com.skillbox.ru.developerspublics.model.repository.GlobalSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


@Service
public class GlobalSettingService {
    private final GlobalSettingsRepository globalSettingsRepository;


    @Autowired
    public GlobalSettingService(GlobalSettingsRepository globalSettingsRepository) {
        this.globalSettingsRepository = globalSettingsRepository;
    }


    public List<GlobalSetting> getAllGlobalSettings() {
        return new ArrayList<>(globalSettingsRepository.findAll());
    }


    public GlobalSetting findGlobalSettingByCode(String code) {
        return globalSettingsRepository.findGlobalSettingByCode(code);
    }


    public void initGlobalSettings() {
        //проверяем, что все настройки есть
        if (globalSettingsRepository.count() != GlobalSettingsCodes.values().length) {
            //если нет - перезаполняем таблицу БД настройками по умолчанию
            globalSettingsRepository.deleteAll();
            int id = 1;
            for (GlobalSettingsCodes code : GlobalSettingsCodes.values()) {
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


    public boolean setGlobalSettings(Boolean multiUserMode, Boolean postPremoderation, Boolean statisticsIsPublic) {
        boolean hasErrors = false;
        for (GlobalSetting gs : getAllGlobalSettings()) {
            if (gs.getCode().equals(GlobalSettingsCodes.MULTIUSER_MODE.toString()) && multiUserMode != null) {
                gs.setValue(multiUserMode ? GlobalSettingsValues.YES.toString() : GlobalSettingsValues.NO.toString());
                globalSettingsRepository.save(gs);
            }
            else if (gs.getCode().equals(GlobalSettingsCodes.POST_PREMODERATION.toString()) && postPremoderation != null) {
                gs.setValue(postPremoderation ? GlobalSettingsValues.YES.toString() : GlobalSettingsValues.NO.toString());
                globalSettingsRepository.save(gs);
            }
            else if (gs.getCode().equals(GlobalSettingsCodes.STATISTICS_IS_PUBLIC.toString()) && statisticsIsPublic != null) {
                gs.setValue(statisticsIsPublic ? GlobalSettingsValues.YES.toString() : GlobalSettingsValues.NO.toString());
                globalSettingsRepository.save(gs);
            }
            else hasErrors = true;
        }
        return !hasErrors;
    }


    public ResponseEntity<BlogInfo> getApiInit() {
        //при запуске проверяем заполнены ли глобальные настройки
        initGlobalSettings();
        //и возвращаем инфо о блоге
        return ResponseEntity.status(HttpStatus.OK).body(new BlogInfo());
    }


    public ResponseEntity<?> getApiSettings() {
        //init response
        TreeMap<String, Boolean> response = new TreeMap<>();
        //перебираем все настройки
        for (GlobalSetting globalSetting : getAllGlobalSettings()) {
            //и запоминаем их в ответе -> сразу переводим value String в boolean
            response.put(globalSetting.getCode(),
                    globalSetting.getValue().equals(GlobalSettingsValues.YES.toString()));
        }
        //и возвращаем его
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @SneakyThrows
    public ResponseEntity<?> postApiSettings(RequestApiSettings requestBody) {
        //Неверный параметр на входе - ответ с кодом 400 (Bad request)
        if (!setGlobalSettings(
                requestBody.isMultiUserMode(),
                requestBody.isPostPremoderation(),
                requestBody.isStatisticsIsPublic()))
            return ResponseEntity.status(400).body(new MessageResponse("Глобальная настройка не найдена!"));

        return ResponseEntity.status(200).body(new ResultResponse(true));
    }
}
