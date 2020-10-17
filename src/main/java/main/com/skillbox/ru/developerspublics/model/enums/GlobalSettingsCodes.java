package main.com.skillbox.ru.developerspublics.model.enums;

public enum GlobalSettingsCodes {
  MULTIUSER_MODE("Многопользовательский режим"),
  POST_PREMODERATION("Премодерация постов"),
  STATISTICS_IS_PUBLIC("Показывать всем статистику блога");

  private final String desc;

  GlobalSettingsCodes(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }

}
