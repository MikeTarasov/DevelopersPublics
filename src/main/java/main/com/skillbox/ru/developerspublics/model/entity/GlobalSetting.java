package main.com.skillbox.ru.developerspublics.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "global_settings")
@Entity
public class GlobalSetting {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private GlobalSettingsCodes code;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private GlobalSettingsValues value;
}
