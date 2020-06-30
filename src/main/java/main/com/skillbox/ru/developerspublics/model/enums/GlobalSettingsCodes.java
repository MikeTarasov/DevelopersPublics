package main.com.skillbox.ru.developerspublics.model.enums;

public enum GlobalSettingsCodes {
    MULTI_USER_MODE("Многопользовательский режим"),
    POST_PREMODERATION("Премодерация постов"),
    STATISTICS_IS_PUBLIC("Показывать всем статистику блога");

    private String desc;

    GlobalSettingsCodes(String desc) {
        this.desc = desc;
    }

    public String getDesc() {return desc;};
}
