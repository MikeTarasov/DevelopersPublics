package main.com.skillbox.ru.developerspublics.model.pojo;

import lombok.*;
import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tag2post")
@Entity
public class TagToPost
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "post_id", nullable = false)
    private int postId;

    @Column(name = "tag_id", nullable = false)
    private int tagId;

    @Transient
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post postTag;

    @Transient
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private Tag tagPost;
}
