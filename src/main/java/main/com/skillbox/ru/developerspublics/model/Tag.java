package main.com.skillbox.ru.developerspublics.model;

import lombok.*;
import javax.persistence.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tags")
@Entity
public class Tag
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "tagPost", fetch = FetchType.LAZY)
    private List<TagToPost> tagToPosts;
}
