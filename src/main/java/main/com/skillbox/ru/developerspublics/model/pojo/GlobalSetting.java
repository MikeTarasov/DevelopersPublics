package main.com.skillbox.ru.developerspublics.model.pojo;

import lombok.*;
import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "global_settings")
@Entity
public class GlobalSetting
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String value;

//    public String toString() {
//        System.out.println("\"" + code + "\": " + ((value.equals(GlobalSettingsValues.YES.toString())) ? "true" : "false"));
//        return "" + code + ": " + ((value.equals(GlobalSettingsValues.YES.toString())) ? "true" : "false");
//    }
}
