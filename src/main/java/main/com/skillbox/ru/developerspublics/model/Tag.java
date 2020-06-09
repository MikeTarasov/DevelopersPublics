package main.com.skillbox.ru.developerspublics.model;

import lombok.*;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.repository.PostsRepository;
import main.com.skillbox.ru.developerspublics.repository.TagsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.sql.Date;
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


    private float getWeight(PostsRepository postsRepository, TagsRepository tagsRepository) {
        int count = 0;
        for (Post post : postsRepository.findAll()) {
            if (post.getIsActive() == 1 && post.getModerationStatus() == ModerationStatuses.ACCEPTED
                    && !post.getTime().after(new Date(System.currentTimeMillis()))) {
                count++;
            }
        }


        float maxWeight = 0.0F;

        for (Tag tag : tagsRepository.findAll()) {
            float tagWeight = (float) tag.getTagToPosts().size() / count;
            if (tagWeight > maxWeight) {
                maxWeight = tagWeight;
            }
        }

        if (count == 0 || maxWeight == 0) return 0.0F;

        return (float) (tagToPosts.size() / count) / maxWeight;
    }

    public String toString(PostsRepository postsRepository, TagsRepository tagsRepository) {
        return "{\"name\": \"" + name + "\", \"weight\": " + getWeight(postsRepository, tagsRepository) + "}";
    }
}
