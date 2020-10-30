package main.com.skillbox.ru.developerspublics.service;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.response.TagResponse;
import main.com.skillbox.ru.developerspublics.api.response.TagsListResponse;
import main.com.skillbox.ru.developerspublics.model.entity.Tag;
import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;
import main.com.skillbox.ru.developerspublics.model.repository.PostsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.TagToPostsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.TagsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TagService {

  private final PostsRepository postsRepository;
  private final TagsRepository tagsRepository;
  private final TagToPostsRepository tagToPostsRepository;
  private final TagToPostService tagToPostService;
  private float maxWeight;
  private HashMap<Integer, Float> weightMap;

  @Value("${tag.min.weight}")
  private float tagMinWeight;

  @Autowired
  public TagService(
      PostsRepository postsRepository,
      TagsRepository tagsRepository,
      TagToPostsRepository tagToPostsRepository,
      TagToPostService tagToPostService) {
    this.postsRepository = postsRepository;
    this.tagsRepository = tagsRepository;
    this.tagToPostsRepository = tagToPostsRepository;
    this.tagToPostService = tagToPostService;
  }


  public Tag findTagByName(String tagName) {
    return tagsRepository.findByName(tagName);
  }


  public Tag findTagById(int tagId) {
    return tagsRepository.findById(tagId).orElseThrow();
  }


  private HashSet<String> getActiveTags() {
    return new HashSet<>(tagsRepository.findActiveTags(new Date(System.currentTimeMillis())));
  }


  public List<TagToPost> getTagToPost(String tagName) {
    return tagToPostsRepository.getTagToPostsByTagName(tagName);
  }


  private float getWeight(Tag tag) {
    if (weightMap == null) {
      setWeights();
    }
    return weightMap.get(tag.getId());
  }


  @SneakyThrows
  private List<TagResponse> getTagResponseList(List<String> tagNameList) {
    List<TagResponse> list = new ArrayList<>();
    for (String tagName : tagNameList) {
      list.add(new TagResponse(tagName, getWeight(findTagByName(tagName))));
    }
    return list;
  }


  public void deleteTag(Tag tag) {
    Tag tagDB = tagsRepository.findByName(tag.getName());
    if (tagDB != null) {
      tagsRepository.delete(tagDB);
    }
    setWeights();
  }


  public void setWeights() {
    //считаем кол-во активных постов
    int count = postsRepository.countActivePosts(new Date(System.currentTimeMillis()));
    weightMap = new HashMap<>();

    //считаем вес тэгов  и ищем мах
    maxWeight = 0.0F;

    for (Tag tag : tagsRepository.findAll()) {
      //если активных постов нет - присваиваем 0
      if (count == 0) {
        weightMap.put(tag.getId(), 0F);
      } //иначе заполняем абс. значениями и запоминаем мах
      else {
        float tagWeight = (float) tagToPostsRepository.countTagsToPost(tag.getId()) / count;
        weightMap.put(tag.getId(), tagWeight);
        if (tagWeight > maxWeight) {
          maxWeight = tagWeight;
        }
      }
    }

    //если есть акт. посты и мах вес - приводим вес к удельному
    if (count != 0 && maxWeight != 0) {
//      //без ограничений на минимальный шрифт тэгов
//      weightMap.replaceAll((k, v) -> weightMap.get(k) / maxWeight);
      //с ограничением на минимальный шрифт тегов
      for (Integer key : weightMap.keySet()) {
        float weight = weightMap.get(key) / maxWeight;
        if (weight < tagMinWeight) {
          weight = tagMinWeight;
        }
        weightMap.replace(key, weight);
      }
    }
  }


  @Transactional
  public void saveTag(String tagName, int postId) {
    tagName = tagName.toUpperCase().replaceAll("[^\\w]", "_");
    //пробуем найти тэг в БД
    Tag tag = findTagByName(tagName);
    //нет такого в БД - создадим новый тэг
    if (tag == null) {
      tag = new Tag();
      //заполним обязательные поля
      tag.setName(tagName);
      //заносим в репозиторий
      tagsRepository.save(tag);
    }

    //привяжем тэг к посту
    tagToPostService.saveTagToPost(postId, tag.getId());

    //пересчитаем удельный вес
    setWeights();
  }


  public ResponseEntity<?> getApiTag(String query) {
    List<String> tagNames = new ArrayList<>();

    //тэг не задан - выводим все
    if (query == null) {
      tagNames.addAll(getActiveTags());
    } else {
      //перебираем все активные тэги и ищем совпадения
      for (String tagName : getActiveTags()) {
        if (tagName.equals(query)) {
          //все совпадения заносим в список по шаблону
          tagNames.add(tagName);
        }
      }
    }

    //собираем ответ
    return ResponseEntity.status(HttpStatus.OK)
        .body(new TagsListResponse(getTagResponseList(tagNames)));
  }
}