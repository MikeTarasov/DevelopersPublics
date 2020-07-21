package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.model.entity.Tag;
import main.com.skillbox.ru.developerspublics.model.entity.TagToPost;

import main.com.skillbox.ru.developerspublics.repository.TagsRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class TagService {
    private float maxWeight;

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
        setWeights(tags);
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
        float tagWeight = 0F;
        int activePosts = postService.getActivePosts().size();
        if (maxWeight != 0 && activePosts != 0) {
            tagWeight =(float) tag.getTagToPosts().size() / activePosts / maxWeight;
        }
        return tagWeight;
    }

    private void setWeights(List<Tag> tagList) {
        //считаем кол-во активных постов
        int count = postService.getActivePosts().size();

        //считаем вес тэгов  и ищем мах
        maxWeight = 0.0F;

        for (Tag tag : tagList) {
            //если активных постов нет - присваиваем 0
            if (count == 0) {
                tag.setTagWeight(0F);
            } //иначе заполняем абс. значениями и запоминаем мах
            else {
                float tagWeight = (float) tag.getTagToPosts().size() / count;
                tag.setTagWeight(tagWeight);
                if (tagWeight > maxWeight) {
                    maxWeight = tagWeight;
                }
            }
        }

        //если есть акт. посты и мах вес - приводим вес к удельному
        if (count != 0 && maxWeight != 0) {
            for (Tag tag : tagList) {
                tag.setTagWeight(tag.getTagWeight() / maxWeight);
            }
        }
    }

    public void saveTag(String tagName, int postId) {
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
    }

    public JSONObject tagToJSON(Tag tag) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", tag.getName());
        jsonObject.put("weight", tag.getTagWeight());
        return jsonObject;
    }
}
