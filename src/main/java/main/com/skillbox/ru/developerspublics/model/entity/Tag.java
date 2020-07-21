package main.com.skillbox.ru.developerspublics.model.entity;

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

    @Transient
    @OneToMany(mappedBy = "tagPost", fetch = FetchType.LAZY)
    private List<TagToPost> tagToPosts;

    @Transient
    private float tagWeight;

//    public String toString(PostsRepository postsRepository, TagsRepository tagsRepository) {
//        return "{\"name\": \"" + name + "\", \"weight\": " + getWeight(postsRepository, tagsRepository) + "}";
//    }
}
