package main.com.skillbox.ru.developerspublics.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Uploads {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private String path;

    @Column(columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] bytes;

    public Uploads(String path, byte[] bytes) {
        this.path = path;
        this.bytes = bytes;
    }
}
