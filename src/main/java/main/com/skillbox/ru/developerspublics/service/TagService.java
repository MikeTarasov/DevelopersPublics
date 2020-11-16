package main.com.skillbox.ru.developerspublics.service;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import main.com.skillbox.ru.developerspublics.api.response.TagResponse;
import main.com.skillbox.ru.developerspublics.api.response.TagsListResponse;
import main.com.skillbox.ru.developerspublics.model.entity.Tag;
import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;
import main.com.skillbox.ru.developerspublics.model.projections.TagResponseProjection;
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

  private final TagsRepository tagsRepository;
  private final TagToPostsRepository tagToPostsRepository;
  private final TagToPostService tagToPostService;

  @Value("${tag.min.weight}")
  private float tagMinWeight;

  @Autowired
  public TagService(
      TagsRepository tagsRepository,
      TagToPostsRepository tagToPostsRepository,
      TagToPostService tagToPostService) {
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


  private List<TagResponse> getTagResponseList(List<String> tagNameList) {
    List<TagResponse> list = new ArrayList<>();
    HashMap<String, Float> weightMap = new HashMap<>();

    //получим мапу из списка в аргументе
    for (TagResponseProjection projection :
        tagsRepository.getTagResponseSet(new Date(System.currentTimeMillis()))) {
      TagResponse tagResponse = new TagResponse(projection);
      if (tagNameList.contains(tagResponse.getName())) {
        weightMap.put(tagResponse.getName(), tagResponse.getWeight());
      }
    }

    //получаем мах вес
    float maxWeight = weightMap.values().stream().max(Float::compareTo).orElse(0F);

    //на ноль не делим и в список не добавляем
    if (maxWeight != 0) {
      //переводим в удельный вес
      for (String name : weightMap.keySet()) {
        float weight = weightMap.get(name) / maxWeight;
        //ограничиваем мин вес
        if (weight >= tagMinWeight) {
          list.add(new TagResponse(name, weight));
        }
      }
    }
    return list;
  }


  public void deleteTag(Tag tag) { //TODO
    Tag tagDB = tagsRepository.findByName(tag.getName());
    if (tagDB != null) {
      tagsRepository.delete(tagDB);
    }
  }


  @Transactional
  public void saveTag(String tagName, int postId) {
    tagName = tagName.toUpperCase().replaceAll("[^(0-9A-ZА-ЯЁ\\s)]", "_");
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
  }


  public ResponseEntity<?> getApiTag(String query) {
    List<String> tagNames = new ArrayList<>();

    //тэг не задан - выводим все
    if (query == null || query.equals("")) {
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