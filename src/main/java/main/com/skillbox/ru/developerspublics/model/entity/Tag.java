package main.com.skillbox.ru.developerspublics.model.entity;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

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

    @Transient
    @OneToMany(mappedBy = "tagPost", fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<TagToPost> tagToPosts;

    @Transient
    private float tagWeight;
}
