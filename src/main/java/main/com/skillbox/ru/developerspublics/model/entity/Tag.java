package main.com.skillbox.ru.developerspublics.model.entity;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tags")
@Entity
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(nullable = false)
  private String name;

  @OneToMany(mappedBy = "tagPost")
  private List<TagToPost> tagToPosts;
}