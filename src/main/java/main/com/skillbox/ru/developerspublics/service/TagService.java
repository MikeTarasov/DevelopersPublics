package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.Tag;
import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;

import main.com.skillbox.ru.developerspublics.repository.TagsRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class TagService {
    private float maxWeight;

    private HashMap<Integer, Float> weightMap;  //TODO загнать вес в сет!!!!!!!!!!!!!!!!!!!!!

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private TagToPostService tagToPostService;

    public Tag getInitTagById(int tagId) {
        Optional<Tag> optionalTag = tagsRepository.findById(tagId);
        if (optionalTag.isPresent()) {
            Tag tag = optionalTag.get();
            initTag(tag);
            return tag;
        }
        return null;
    }

    public Tag getTagById(int tagId) {
        return tagsRepository.findById(tagId).orElseThrow();
    }

    private Tag getTagByName(String name) {
        for (Tag tagDB : tagsRepository.findAll()) {
            if (tagDB.getName().equals(name)) {
                return tagDB;
            }
        }
        return null;
    }

    public List<Tag> getInitTags() {
        List<Tag> tags = new ArrayList<>();
        for (Tag tagDB : tagsRepository.findAll()) {
            tagDB.setTagToPosts(getTagToPost(tagDB));
            tags.add(tagDB);
        }
        if (weightMap == null) setWeights();
        return tags;
    }

    public List<Tag> getTags() {
        return new ArrayList<>(tagsRepository.findAll());
    }

    private void initTag(Tag tag) {
        tag.setTagToPosts(getTagToPost(tag));
        tag.setTagWeight(getWeight(tag));
    }

    private List<TagToPost> getTagToPost(Tag tag) {
        List<TagToPost> tagToPosts = new ArrayList<>();
        for (TagToPost tagToPostDB : tagToPostService.getInitTagToPosts()) {
            if (tagToPostDB.getTagId() == tag.getId()) {
                tagToPosts.add(tagToPostDB);
            }
        }
        return tagToPosts;
    }

    private float getWeight(Tag tag) {
        if (weightMap == null) setWeights();
        return weightMap.get(tag.getId());
    }

    private void setWeights() {
        //считаем кол-во активных постов
        List<Post> activePosts = postService.getActivePosts();
        int count = activePosts.size();
        weightMap = new HashMap<>();

        //считаем вес тэгов  и ищем мах
        maxWeight = 0.0F;


        for (Tag tag : tagsRepository.findAll()) {
            //если активных постов нет - присваиваем 0
            if (count == 0) {
                weightMap.put(tag.getId(), 0F);
            } //иначе заполняем абс. значениями и запоминаем мах
            else {
                float tagWeight = (float) getTagToPost(tag).size() / count;
                weightMap.put(tag.getId(), tagWeight);
                if (tagWeight > maxWeight) {
                    maxWeight = tagWeight;
                }
            }
        }

        //если есть акт. посты и мах вес - приводим вес к удельному
        if (count != 0 && maxWeight != 0) {
            weightMap.replaceAll((k, v) -> weightMap.get(k) / maxWeight);
        }
    }

    public void saveTag(String tagName, int postId) {
        tagName = tagName.toUpperCase();
        //пробуем найти тэг в БД
        Tag tag = getTagByName(tagName);
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

    public JSONObject tagToJSON(Tag tag) {  //TODO 5 sec!!!!!!!
        if (weightMap == null) setWeights();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", tag.getName());
        jsonObject.put("weight", getWeight(tag));
        return jsonObject;
    }
}
